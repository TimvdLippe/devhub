[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    [@projectFrameset.renderBreadcrumb i18n group repositoryEntity/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">Assignments</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
            [@projectFrameset.renderSidemenu "assignments" i18n group repository/]
        </div>
        <div class="col-md-10">
        [#assign assignments = course.getAssignments()]
            <table class="table table-bordered">
                <colgroup>
                    <col span="1" width="5%"/>
                    <col span="1" width="65%"/>
                    <col span="1" width="10%"/>
                    <col span="1" width="10%"/>
                    <col span="1" width="10%"/>
                </colgroup>
            [#if assignments?? && assignments?has_content]
                <thead>
                <tr>
                    <th>#</th>
                    <th>${i18n.translate("course.control.assignment")}</th>
                    <th>${i18n.translate("course.control.due-date")}</th>
                    <th>${i18n.translate("delivery.grade")}</th>
                    <th>${i18n.translate("delivery.status")}</th>
                </tr>
                </thead>
            [/#if]
                <tbody>
                [#if assignments?? && assignments?has_content]
                    [#list assignments as assignment]
                    [#assign showGrade = assignment.isGradesReleased() || user.isAdmin() || user.isAssisting(course)]
                    [#if group.members?seq_contains(user)]
                        [#assign delivery = deliveries.getLastDeliveryForStudent(assignment, user).orElse(null)!]
                    [#else]
                        [#assign delivery = deliveries.getLastDelivery(assignment, group).orElse(null)!]
                    [/#if]
                    <tr>
                        <td>
                            <a href="${group.getURI()}assignments/${assignment.getAssignmentId()}">
                            ${assignment_index + 1}
                            </a>
                        </td>
                        <td>
                            <a href="${group.getURI()}assignments/${assignment.getAssignmentId()}">
                            ${assignment.getName()!"-"}
                            </a>
                        </td>
                        <td>
                            [#if assignment.getDueDate()??]
                                <a href="${group.getURI()}assignments/${assignment.getAssignmentId()}">
                                ${assignment.getDueDate()?string["EEE, d MMM yyyy HH:mm"]}
                                </a>
                            [/#if]
                        </td>
                        <td>
                            [#if delivery?has_content && delivery.getReview()?? && showGrade && delivery.getReview().grade?? && delivery.getReview().grade?has_content]
                            [#assign review = delivery.getReview()]
                              ${review.grade?string["0.#"]}
                            [#else]
                                -
                            [/#if]
                        </td>
                        <td>
                            [#if delivery?has_content]
                                [#assign commit = delivery.getCommit()!]
                                [#if commit?? && commit?has_content && commit.buildResult?? && commit.buildResult?has_content]
                                    <a href="${repositoryEntity.getURI()}commits/${commit.commitId}/diff">
                                    [#if commit.buildResult.hasFinished()]
                                        [#if commit.buildResult.hasSucceeded()]
                                            <span class="label label-success">${i18n.translate("build.state.succeeded")}</span>
                                        [#else]
                                            <span class="label label-danger">${i18n.translate("build.state.failed")}</span>
                                        [/#if]
                                    [#else]
                                            <span class="label label-info">${commit.commitId}</span>
                                    [/#if]
                                    </a>
                                [/#if]

                                [#if delivery.isLate()]
                                    <span class="label label-danger">${i18n.translate("assignment.handed-in-late")}</span>
                                [/#if]

                                [#assign state = delivery.getState()]
                                [#if !showGrade]
                                    [#assign state = submittedState]
                                [/#if]
	                            <span class="label label-${state.style}" data-toggle="tooltip" title="${i18n.translate(state.messageTranslationKey)}">
                                    ${i18n.translate(state.translationKey)}
	                            </span>
                            [#else]
                                <span class="label label-default">
                                    ${i18n.translate("assignment.not-submitted")}
                                </span>
                            [/#if]
                        </td>
                    </tr>
                    [/#list]
                [#else]
                <tr>
                    <td class="muted" colspan="5">${i18n.translate("course.no-assignments")}</td>
                </tr>
                [/#if]
                </tbody>
            </table>
        </div>
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
