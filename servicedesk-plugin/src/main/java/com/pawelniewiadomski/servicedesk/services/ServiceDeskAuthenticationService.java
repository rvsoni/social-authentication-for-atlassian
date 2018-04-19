package com.pawelniewiadomski.servicedesk.services;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.service.rememberme.RememberMeService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pawelniewiadomski.jira.openid.authentication.activeobjects.OpenIdProvider;
import com.pawelniewiadomski.jira.openid.authentication.services.AuthenticationService;
import com.pawelniewiadomski.jira.openid.authentication.services.ExternalUserManagementService;
import com.pawelniewiadomski.jira.openid.authentication.services.GlobalSettings;
import com.pawelniewiadomski.jira.openid.authentication.services.ProvidedUserDetails;
import com.pawelniewiadomski.jira.openid.authentication.services.RedirectionService;
import com.pawelniewiadomski.jira.openid.authentication.services.TemplateHelper;
import com.pawelniewiadomski.servicedesk.querydsl.ServiceDeskTables;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import static com.atlassian.jira.component.ComponentAccessor.getComponentOfType;
import static com.google.common.collect.ImmutableList.of;
import static com.pawelniewiadomski.AllowedDomains.isEmailFromAllowedDomain;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.lowerCase;
import static org.apache.commons.lang.StringUtils.replaceChars;

@Slf4j
@Component
public class ServiceDeskAuthenticationService implements AuthenticationService {
    @Autowired
    protected CrowdService crowdService;

    @Autowired
    protected GlobalSettings globalSettings;

    @Autowired
    protected TemplateHelper templateHelper;

    @Autowired
    protected ExternalUserManagementService externalUserManagementService;

    @Autowired
    protected RedirectionService redirectionService;

    @Autowired
    protected DatabaseAccessor databaseAccessor;

    @ComponentImport
    @Autowired
    protected ProjectRoleManager projectRoleManager;

    @ComponentImport
    @Autowired
    protected ProjectManager projectManager;

    @ComponentImport
    @Autowired
    protected RoleActorFactory roleActorFactory;

    @ComponentImport
    @Autowired
    protected UserManager userManager;

    @ComponentImport
    @Autowired
    protected DirectoryManager directoryManager;

    public void showAuthentication(final HttpServletRequest request, final HttpServletResponse response,
                                   final OpenIdProvider provider, final ProvidedUserDetails userDetails) throws IOException, ServletException {
        if (isBlank(userDetails.getEmail())) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }

        if (isNotBlank(provider.getAllowedDomains())) {
            if (!isEmailFromAllowedDomain(provider, userDetails.getEmail())) {
                templateHelper.render(request, response, "OpenId.Templates.domainMismatch");
                return;
            }
        }

        ApplicationUser user = getUserByEmail(userDetails);
        if (user == null && !externalUserManagementService.isExternalUserManagement() && globalSettings.isCreatingUsers()) {
            final String userName = lowerCase(replaceChars(userDetails.getIdentity(), " '()", ""));

            try {
                for(int i = 0; i < 10 && user == null; ++i) {
                    try {
                        val tryUserName = userName + (i == 0 ? "" : i);
                        user = userManager.createUser(
                                new UserDetails(tryUserName, userDetails.getIdentity())
                                        .withPassword(randomUUID().toString())
                                        .withEmail(userDetails.getEmail())
                        );
                    } catch (CreateException e) {
                        if (!(e.getCause() instanceof UserAlreadyExistsException || e.getCause() instanceof InvalidUserException)) {
                            throw e;
                        }
                    }
                }
            } catch (Exception e) {
                log.error(String.format("Cannot create an account for %s %s", userDetails.getIdentity(), userDetails.getEmail()), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            }
        }

        if (user != null) {
            ApplicationUser finalUser = user;
            getServiceDeskProjectIds(request).forEach((projectId) -> addToServiceDeskUserRole(finalUser, projectId));

            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
            getComponentOfType(LoginManager.class).onLoginAttempt(request, user.getName(), true);

            getComponentOfType(RememberMeService.class).addRememberMeCookie(request, response, user.getUsername());

            redirectionService.redirectToReturnUrlOrHome(request, response);
        } else {
            templateHelper.render(request, response, "OpenId.Templates.noUserMatched");
        }
    }

    private ApplicationUser getUserByEmail(ProvidedUserDetails userDetails) {
        return ApplicationUsers.from((User) Iterables.getFirst(crowdService.search(new UserQuery(
                User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
                StringUtils.stripToEmpty(userDetails.getEmail()).toLowerCase()), 0, 1)), null));
    }

    private void addToServiceDeskUserRole(final ApplicationUser appUser, final Long projectId) {
        final Project project = projectManager.getProjectObj(projectId);
        final ProjectRole serviceDeskCustomers = projectRoleManager.getProjectRole("Service Desk Customers");
        if (serviceDeskCustomers != null && !projectRoleManager.isUserInProjectRole(appUser, serviceDeskCustomers, project)) {
            addUserToRoleActors(appUser, project, serviceDeskCustomers);
        }
    }

    private List<Long> getServiceDeskProjectIds(final HttpServletRequest request) {
        try {
            final Integer portalId = Integer.valueOf((String) request.getSession().getAttribute(PORTAL_ID_SESSION));

            final Long run = databaseAccessor
                    .run(connection -> connection.select(ServiceDeskTables.VIEWPORT.PROJECT_ID)
                            .from(ServiceDeskTables.VIEWPORT)
                            .where(ServiceDeskTables.VIEWPORT.ID.eq(portalId))
                            .fetchOne());

            return Lists.newArrayList(run);
        } catch (NumberFormatException e) {
            final List<Long> run = databaseAccessor
                    .run(connection -> connection.select(ServiceDeskTables.VIEWPORT.PROJECT_ID)
                            .from(ServiceDeskTables.VIEWPORT)
                            .leftJoin(ServiceDeskTables.SERVICEDESK)
                            .on(ServiceDeskTables.VIEWPORT.PROJECT_ID.eq(ServiceDeskTables.SERVICEDESK.PROJECT_ID))
                            .where(ServiceDeskTables.SERVICEDESK.PUBLIC_SIGNUP.eq(1))
                            .fetch());

            return run;
        }
    }

    private void addUserToRoleActors(ApplicationUser appUser, Project project, ProjectRole serviceDeskCustomers) {
        final ProjectRoleActors actors = projectRoleManager.getProjectRoleActors(serviceDeskCustomers, project);
        try {
            projectRoleManager.updateProjectRoleActors(
                    (ProjectRoleActors) actors.addRoleActors(createRoleActors(of(appUser), serviceDeskCustomers, project)));
        } catch (RoleActorDoesNotExistException e) {
            throw new RuntimeException("Unable to add user to role", e);
        }
    }

    private List<ProjectRoleActor> createRoleActors(List<ApplicationUser> users, ProjectRole role, Project project) throws RoleActorDoesNotExistException {
        final ImmutableList.Builder<ProjectRoleActor> actors = ImmutableList.builder();
        for (ApplicationUser user : users) {
            actors.add(roleActorFactory.createRoleActor(null,
                    role.getId(), project.getId(), ProjectRoleActor.USER_ROLE_ACTOR_TYPE, user.getKey()));
        }
        return actors.build();
    }
}
