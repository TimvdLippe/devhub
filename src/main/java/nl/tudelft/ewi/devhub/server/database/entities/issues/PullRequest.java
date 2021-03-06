package nl.tudelft.ewi.devhub.server.database.entities.issues;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;

@Data
@Entity
@DiscriminatorValue("PULL_REQUEST")
@EqualsAndHashCode(callSuper = true)
public class PullRequest extends AbstractIssue {

	@NotNull
	@Size(max = 255)
	@Column(name="branch_name", length = 255)
	private String branchName;

	@Column(name="merged")
	private boolean merged;

	/*
	 * @JoinColumn(name = "merge_commit_id", referencedColumnName = "commit_id", nullable = false)
	 * @PrimaryKeyJoinColumn(name = "repository_id", referencedColumnName = "repository_id")
	 * Should have been enough according to
	 *    https://en.wikibooks.org/wiki/Java_Persistence/OneToOne#Example_of_cascaded_primary_keys_and_mixed_OneToOne_and_ManyToOne_mapping_annotations_using_PrimaryKeyJoinColumn
	 *
	 * But it wouldn't work:
	 *    Caused by: org.hibernate.AnnotationException: referencedColumnNames(commit_id) of
	 *    nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest.destination
	 *    referencing nl.tudelft.ewi.devhub.server.database.entities.Commit not mapped to a single property
	 */
	@NotNull
	@ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumnsOrFormulas({
		@JoinColumnOrFormula(formula = @JoinFormula(value = "repository_id", referencedColumnName = "repository_id")),
		@JoinColumnOrFormula(column = @JoinColumn(name = "merge_commit_id", referencedColumnName = "commit_id"))
	})
	private Commit mergeBase;

	@NotNull
	@ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumnsOrFormulas({
		@JoinColumnOrFormula(formula = @JoinFormula(value = "repository_id", referencedColumnName = "repository_id")),
		@JoinColumnOrFormula(column = @JoinColumn(name = "destination_commit_id", referencedColumnName = "commit_id"))
	})
	private Commit destination;

	@Column(name="ahead")
	private Integer ahead;

	@Column(name="behind")
	private Integer behind;

	@Override
	public URI getURI() {
		return getRepository().getURI().resolve("pull/" + getIssueId() + "/");
	}

}
