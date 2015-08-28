package nl.tudelft.ewi.devhub.server.backend.warnings;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.warnings.IllegalFileWarning;
import nl.tudelft.ewi.git.client.Branch;
import nl.tudelft.ewi.git.client.Commit;
import nl.tudelft.ewi.git.client.GitServerClient;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import nl.tudelft.ewi.git.models.CommitModel;
import nl.tudelft.ewi.git.models.EntryType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author Jan-Willem Gmelig Meyling
 */
@RunWith(MockitoJUnitRunner.class)
public class IllegalFileWarningTest {

    private final static String COMMIT_ID = "abcd";
    private final Map<String,EntryType> directory1 = new HashMap<>();



    private Group group;
	private GroupRepository groupRepository;
    private CourseEdition course;
    private Commit commit;
	private nl.tudelft.ewi.devhub.server.database.entities.Commit commitEntity;

    @Mock private Repository repository;
    @Mock private Repositories repositories;
    @Mock private GitServerClient gitServerClient;
    @Mock private Branch branch;
    @Mock private CommitModel commitModel;
    @InjectMocks  private IllegalFileWarningGenerator generator;

    private IllegalFileWarning warning;

    @Before
    public void beforeTest() throws Exception {
		course = new CourseEdition();
		group = new Group();
		group.setCourseEdition(course);
		groupRepository = new GroupRepository();
		groupRepository.setRepositoryName("");
		groupRepository.setGroup(group);
		group.setRepository(groupRepository);
		commitEntity = new nl.tudelft.ewi.devhub.server.database.entities.Commit();
		commitEntity.setCommitId(COMMIT_ID);
		commitEntity.setRepository(groupRepository);

        when(gitServerClient.repositories()).thenReturn(repositories);
        when(repositories.retrieve(anyString())).thenReturn(repository);
        when(repository.listDirectoryEntries(COMMIT_ID, "")).thenReturn(directory1);

        directory1.clear();

        warning = new IllegalFileWarning();
        warning.setCommit(commitEntity);
    }

    @Test
    public void TestIngoredFile(){
        directory1.put("types.class",EntryType.TEXT);
        Collection<IllegalFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        warning.setFileName("types.class");
        assertEquals(warning,warnings.stream().findFirst().get());
    }

    @Test
    public void TestNotIngoredFile(){
        directory1.put("types.java",EntryType.TEXT);
        Collection<IllegalFileWarning> warnings = generator.generateWarnings(commitEntity, null);
        IllegalFileWarning warning = new IllegalFileWarning();
        warning.setFileName("types.java");
        assertEquals(Collections.emptySet(), warnings);
    }

    @Test
    public void TestIgnoredFolder(){
        directory1.put("bin/",EntryType.FOLDER);
        Collection<IllegalFileWarning> warnings = generator.generateWarnings(commitEntity,null);
        warning.setFileName("bin/");
        assertEquals(warning ,warnings.stream().findFirst().get());
    }

}
