package nl.tudelft.ewi.devhub.server.backend.warnings;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.database.Configurable;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.LargeCommitWarning;
import nl.tudelft.ewi.devhub.server.web.models.GitPush;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffContext;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffFile;
import nl.tudelft.ewi.git.models.AbstractDiffModel.DiffLine;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Liam Clark
 */
@Slf4j
public class LargeCommitWarningGenerator extends AbstractCommitWarningGenerator<LargeCommitWarning, GitPush>
implements CommitPushWarningGenerator<LargeCommitWarning> {

    private static final String[] DEFAULT_EXTENSIONS = {".java", ".c", ".cpp", ".h", ".scala", ".js", ".html", ".css", ".less"};
    private static final int MAX_AMOUNT_OF_FILES = 10;
    private static final int MAX_AMOUNT_OF_LINES_TOUCHED = 500;
    private static final String MAX_FILES_PROPERTY = "warnings.max-touched-files";
    private static final String MAX_LINE_TOUCHED_PROPERTY = "warnings.max-line-edits";
    private static final String COUNTED_EXTENSIONS_PROPERTY = "warnings.max-line-edits.types";

    @Inject
    public LargeCommitWarningGenerator(RepositoriesApi repositoriesApi) {
        super(repositoriesApi);
    }

    @Override
    public Set<LargeCommitWarning> generateWarnings(Commit commit, GitPush attachment) {
        log.debug("Start generating warnings for {} in {}", commit, this);
        List<DiffFile<DiffContext<DiffLine>>> diffs = getGitCommit(commit).diff().getDiffs();
        String[] extensions = commit.getRepository().getCommaSeparatedValues(COUNTED_EXTENSIONS_PROPERTY, DEFAULT_EXTENSIONS);

        if(!commit.isMerge() && (tooManyFiles(diffs, commit) || tooManyLineChanges(diffs, commit, extensions))) {
            LargeCommitWarning warning = new LargeCommitWarning();
            warning.setCommit(commit);
            log.debug("Finished generating warnings for {} in {}", commit, this);
            return Sets.newHashSet(warning);
        }

        log.debug("Finished generating warnings for {} in {}", commit, this);
        return Collections.emptySet();
    }

    private boolean tooManyFiles(List<DiffFile<DiffContext<DiffLine>>> diffFiles, Commit commit) {
        Configurable configurable = commit.getRepository();
        int maxAmountOfFiles = configurable.getIntegerProperty(MAX_FILES_PROPERTY, MAX_AMOUNT_OF_FILES);
        
        return diffFiles.size() > maxAmountOfFiles;
    }

    private boolean tooManyLineChanges(List<DiffFile<DiffContext<DiffLine>>> diffFiles, Commit commit, String[] extensions) {
        Configurable configurable = commit.getRepository();
        int maxCountOfFiles = configurable.getIntegerProperty(MAX_LINE_TOUCHED_PROPERTY, MAX_AMOUNT_OF_LINES_TOUCHED);

        long count = diffFiles.stream()
                .filter(file -> !file.isDeleted() && fileShouldBeConsideredForWarnings(extensions, file.getNewPath()))
                .flatMap(diffFile -> diffFile.getContexts().stream())
                .flatMap(context -> context.getLines().stream())
                .filter(line -> line.isAdded() || line.isRemoved())
                .count();

        return count > maxCountOfFiles;
    }

    private boolean fileShouldBeConsideredForWarnings(String[] extensions, final String path){
        return Stream.of(extensions).anyMatch(path::endsWith);
    }

}
