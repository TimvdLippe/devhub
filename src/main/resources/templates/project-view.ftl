[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>${group.getGroupName()}</h2>
			<h4>Git clone URL</h4>
			<div class="well well-sm">
[#if repository?? && repository?has_content]
				<code>${repository.getUrl()}</code>
[#else]
				<code>Could not connect to the Git server!</code>
[/#if]
			</div>
			<h4>Recent commits</h4>
			<table class="table table-bordered">
				<tbody>
[#if repository?? && repository?has_content]
	[#if repository.getRecentCommits()?has_content]
		[#list repository.getRecentCommits() as commit]
					<tr>
			[#if states.hasStarted(commit.getCommit())]
				[#if states.hasFinished(commit.getCommit())]
					[#if states.hasSucceeded(commit.getCommit())]
						<td class="commit succeeded">
							<span class="state glyphicon glyphicon-ok-circle" title="Build succeeded!"></span>
							<a href="${path}/diff/${commit.getCommit()}" class="btn btn-primary pull-right"><span class="glyphicon glyphicon-indent-left"></span> Diff</a>
							<a href="${path}/commits/${commit.getCommit()}">
								<div class="comment">${commit.getMessage()}</div>
								<div class="committer">${commit.getAuthor()}</div>
							</a>
						</td>
					[#else]
						<td class="commit failed">
							<span class="state glyphicon glyphicon-remove-circle" title="Build failed!"></span>
							<a href="${path}/diff/${commit.getCommit()}" class="btn btn-primary pull-right"><span class="glyphicon glyphicon-indent-left"></span> Diff</a>
							<a href="${path}/commits/${commit.getCommit()}">
								<div class="comment">${commit.getMessage()}</div>
								<div class="committer">${commit.getAuthor()}</div>
							</a>
						</td>
					[/#if]
				[#else]
						<td class="commit running">
							<span class="state glyphicon glyphicon-align-justify" title="Build queued..."></span>
							<a href="${path}/diff/${commit.getCommit()}" class="btn btn-primary pull-right"><span class="glyphicon glyphicon-indent-left"></span> Diff</a>
							<span>
								<div class="comment">${commit.getMessage()}</div>
								<div class="committer">${commit.getAuthor()}</div>
							</span>
						</td>
				[/#if]
			[#else]
						<td class="commit">
							<span class="state"></span>
							<a href="${path}/diff/${commit.getCommit()}" class="btn btn-primary pull-right"><span class="glyphicon glyphicon-indent-left"></span> Diff</a>
							<span>
								<div class="comment">${commit.getMessage()}</div>
								<div class="committer">${commit.getAuthor()}</div>
							</span>
						</td>
			[/#if]
					</tr>
		[/#list]
	[#else]
						<tr>
							<td class="muted">
								There are no commits in this repository yet!
							</td>
						</tr>
	[/#if]
[#else]
						<tr>
							<td class="muted">
								Could not connect to the Git server!
							</td>
						</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
