package nl.tudelft.ewi.devhub.server.web.resources.repository;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;
import nl.tudelft.ewi.devhub.server.backend.CommentBackend;
import nl.tudelft.ewi.devhub.server.backend.IssueBackend;
import nl.tudelft.ewi.devhub.server.backend.mail.CommentMailer;
import nl.tudelft.ewi.devhub.server.database.controllers.IssueComments;
import nl.tudelft.ewi.devhub.server.database.controllers.Issues;
import nl.tudelft.ewi.devhub.server.database.controllers.Users;
import nl.tudelft.ewi.devhub.server.database.entities.Group;
import nl.tudelft.ewi.devhub.server.database.entities.RepositoryEntity;
import nl.tudelft.ewi.devhub.server.database.entities.User;
import nl.tudelft.ewi.devhub.server.database.entities.issues.Issue;
import nl.tudelft.ewi.devhub.server.database.entities.issues.PullRequest;
import nl.tudelft.ewi.devhub.server.web.errors.UnauthorizedException;
import nl.tudelft.ewi.devhub.server.web.resources.Resource;
import nl.tudelft.ewi.devhub.server.web.templating.TemplateEngine;
import nl.tudelft.ewi.git.models.RepositoryModel;
import nl.tudelft.ewi.git.web.api.RepositoriesApi;
import nl.tudelft.ewi.git.web.api.RepositoryApi;

/**
 * 
 * @author Aron Zwaan
 *
 */
@Slf4j
@Produces(MediaType.TEXT_HTML + Resource.UTF8_CHARSET)
public abstract class AbstractProjectIssueResource extends AbstractIssueResource<Issue> {

	protected Issues issues;
	protected IssueBackend issueBackend;
	protected IssueComments issueComments;
	
	
	public AbstractProjectIssueResource( final TemplateEngine templateEngine, 
			final User currentUser, 
			final CommentBackend commentBackend,
			final CommentMailer commentMailer, 
			final RepositoriesApi repositoriesApi, 
			final Issues issues, 
			final IssueBackend issueBackend,
			final Users users,
			final IssueComments issueComments) {
		
		super(templateEngine, currentUser, commentBackend, commentMailer, repositoriesApi, users);
		
		this.issues = issues;
		this.issueBackend = issueBackend;
		this.issueComments = issueComments;
	}
	
	@GET
	@Transactional
	@Path("/issues")
	public Response getIssues(@Context HttpServletRequest request) throws IOException{
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();

		List<Issue> openIssues = issues.findOpenIssues(repositoryEntity);
		List<Issue> closedIssues = issues.findClosedIssues(repositoryEntity);

		Map<String, Object> parameters = getBaseParameters();
		
		parameters.put("repository", repository);
		parameters.put("openIssues", openIssues);
		parameters.put("closedIssues", closedIssues);

		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issues.ftl", locales, parameters));
	
	}
	
	@GET
	@Transactional
	@Path("/issues/create")
	public Response openCreateIssuePage(@Context HttpServletRequest request) throws IOException{
		
		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();
		
		Map<String, Object> parameters = getBaseParameters();		
		parameters.put("repository", repository);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issue-edit.ftl", locales, parameters));
	}

	@POST
	@Transactional
	@Path("/issues/create")
	public Response createIssue(@Context HttpServletRequest request,
			@FormParam("title") String title,
			@FormParam("description") String description,
			@FormParam("assignee") String assigneeNetID) throws IOException{
		
		Issue issue = new Issue();
		
		issue.setTitle(title);
		issue.setDescription(description);
		issue.setOpen(true);
		
		User assignee = users.findByNetId(assigneeNetID);
		checkCollaborator(assignee);
		
		issue.setAssignee(assignee);
		issue.setTimestamp(new Date());
		issue.setRepository(getRepositoryEntity());
		
		issueBackend.createIssue(getRepositoryApi(getRepositoryEntity()), issue);
		
		return redirect(issue.getURI().toString());
	}
	@GET
	@Transactional
	@Path("/issue/{issueId}/edit")
	public Response editIssue(@Context HttpServletRequest request, 
			@PathParam("issueId") long issueId) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();
		
		Issue issue = issues.findIssueById(getRepositoryEntity(), issueId).get(0);
		
		Map<String, Object> parameters = getBaseParameters();		
		parameters.put("repository", repository);		
		parameters.put("issue", issue);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issue-edit.ftl", locales, parameters));
	}

	@GET
	@Transactional
	@Path("/issue/{issueId}")
	public Response viewIssue(@Context HttpServletRequest request, 
			@PathParam("issueId") long issueId) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();
		
		Issue issue = issues.findIssueById(getRepositoryEntity(), issueId).get(0);
		
		Map<String, Object> parameters = getBaseParameters();		
		parameters.put("repository", repository);		
		parameters.put("issue", issue);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issue-view.ftl", locales, parameters));
	}

	@POST
	@Transactional
	@Path("/issue/{issueId}/edit")
	public Response updateIssue(@Context HttpServletRequest request, 
			@PathParam("issueId") long issueId,
			@FormParam("title") String title,
			@FormParam("description") String description,
			@FormParam("assignee") String assigneeNetID,
			@FormParam("status") String status) throws IOException {

		RepositoryEntity repositoryEntity = getRepositoryEntity();
		RepositoryApi repositoryApi = getRepositoryApi(repositoryEntity);
		RepositoryModel repository = repositoryApi.getRepositoryModel();
		
		Issue issue = issues.findIssueById(getRepositoryEntity(), issueId).get(0);
		
		issue.setTitle(title);
		issue.setDescription(description);
		
		User assignee = users.findByNetId(assigneeNetID);
		checkCollaborator(assignee);
		issue.setAssignee(assignee);

		if(status.equals("open")){
			issue.setOpen(true);
		} else if (status.equals("closed")) {
			issue.setOpen(false);
			issue.setClosed(new Date());
		}
		
		issues.merge(issue);
		
		Map<String, Object> parameters = getBaseParameters();		
		parameters.put("repository", repository);		
		parameters.put("issue", issue);
		
		List<Locale> locales = Collections.list(request.getLocales());
		return display(templateEngine.process("courses/assignments/group-issue-edit.ftl", locales, parameters));
	}

	private void checkCollaborator(User user) {
		if (! getRepositoryEntity().getCollaborators().contains(user)){
			throw new UnauthorizedException();
		}
		
	}
	
	
}
