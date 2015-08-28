package nl.tudelft.ewi.devhub.server.database.controllers;

import com.google.inject.AbstractModule;
import lombok.SneakyThrows;
import nl.tudelft.ewi.devhub.server.database.embeddables.Source;
import nl.tudelft.ewi.devhub.server.database.embeddables.TimeSpan;
import nl.tudelft.ewi.devhub.server.database.entities.Commit;
import nl.tudelft.ewi.devhub.server.database.entities.Course;
import nl.tudelft.ewi.devhub.server.database.entities.GroupRepository;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.comments.CommitComment;
import nl.tudelft.ewi.devhub.server.database.entities.CourseEdition;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.git.client.Repositories;
import nl.tudelft.ewi.git.client.Repository;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JukitoRunner.class)
@UseModules({TestDatabaseModule.class, CommitsTest.CommitsTestModule.class})
public class CommitsTest {

	public static class CommitsTestModule extends AbstractModule {

		@Override
		@SneakyThrows
		protected void configure() {
			Repositories repositories = Mockito.mock(Repositories.class);
			Repository repository = Mockito.mock(Repository.class);
			nl.tudelft.ewi.git.client.Commit commit = Mockito.mock(nl.tudelft.ewi.git.client.Commit.class);

			bind(Repositories.class).toInstance(repositories);
			Mockito.when(repositories.retrieve(Mockito.anyString())).thenReturn(repository);
			Mockito.when(repository.retrieveCommit(Mockito.anyString())).thenReturn(commit);
			Mockito.when(commit.getParents()).thenReturn(new String[] {});
		}

	}
	
	@Inject
	private Random random;
	
	@Inject
	private Groups groups;
	
	@Inject
	private Courses courses;

	@Inject
	private Users users;

	@Inject
	private EntityManager entityManager;

	@Inject
	private Repositories repositories;
	@Inject
	private Commits commits;
	
	@Test
	public void testEnsureCommitInRepository() {
		Group group = createGroup();
		Commit commit = createCommit(group.getRepository());
		assertEquals(group.getRepository(), commit.getRepository());
	}

	@Test
	public void testEnsureCommentInCommit() {
		Group group = createGroup();
		Commit commit = createCommit(group.getRepository());
		CommitComment expected = createCommitComment(commit);
		List<CommitComment> comments = commit.getComments();
		assertEquals("Expected size 1 for list of comments", 1, comments.size());
		
		CommitComment actual = comments.get(0);
		assertEquals(expected.getCommit(), actual.getCommit());
		assertEquals(expected.getContent(), actual.getContent());

		assertEquals(expected.getSource(), actual.getSource());
		assertNotNull(actual.getTimestamp());
		assertEquals(expected.getUser(), actual.getUser());
	}
	
	protected CommitComment createCommitComment(Commit commit) {
		CommitComment comment = new CommitComment();
		comment.setCommit(commit);
		comment.setContent("This is a comment");

		Source source = new Source();
		source.setSourceCommit(commit);
		source.setSourceFilePath(".gitignore");
		source.setSourceLineNumber(1);

		comment.setSource(source);
		comment.setUser(student1());
		commit.getComments().add(comment);
		commits.merge(commit);
		return comment;
	}
	
	protected Commit createCommit(RepositoryEntity repository) {
		return commits.ensureExists(repository, UUID.randomUUID().toString());
	}
	
	protected Group createGroup() {
		Group group = new Group();
		CourseEdition course = getTestCourse();
		group.setCourseEdition(course);
		group = groups.persist(group);

		GroupRepository groupRepository = new GroupRepository();
		groupRepository.setRepositoryName(course.createRepositoryName(group).toString());
		group.setRepository(groupRepository);
		return groups.merge(group);
	}
	
	protected CourseEdition getTestCourse() {
		try {
			return courses.find("TI1705");
		}
		catch (EntityNotFoundException e) {
			Course course = new Course();
			course.setName("Software Quality & Testing");
			course.setCode("TI1705");
			CourseEdition courseEdition = new CourseEdition();
			courseEdition.setTimeSpan(new TimeSpan(new Date(), null));
			courseEdition.setCourse(course);
			courseEdition.setMinGroupSize(2);
			courseEdition.setMaxGroupSize(2);
			return courses.persist(courseEdition);
		}
	}
	
	protected User student1() {
		return users.find(1);
	}
	
}
