package com.pawelniewiadomski.servicedesk.services;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.jira.compatibility.factory.user.UserUtilBridgeFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.security.roles.*;
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
import com.pawelniewiadomski.jira.openid.authentication.services.*;
import com.pawelniewiadomski.servicedesk.querydsl.ServiceDeskTables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.atlassian.jira.component.ComponentAccessor.getComponentOfType;
import static com.google.common.collect.ImmutableList.of;
import static com.pawelniewiadomski.AllowedDomains.isEmailFromAllowedDomain;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
@Component
public class ServiceDeskAuthenticationService implements AuthenticationService {
    @Autowired
    protected UserUtilBridgeFactory userUtilBridgeFactory;

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

    public void showAuthentication(final HttpServletRequest request, HttpServletResponse response,
                                   final OpenIdProvider provider, String identity, String email) throws IOException, ServletException {
        if (isBlank(email)) {
            templateHelper.render(request, response, "OpenId.Templates.emptyEmail");
            return;
        }

        if (isNotBlank(provider.getAllowedDomains())) {
            if (!isEmailFromAllowedDomain(provider, email)) {
                templateHelper.render(request, response, "OpenId.Templates.domainMismatch");
                return;
            }
        }

        com.atlassian.crowd.embedded.api.User user = (com.atlassian.crowd.embedded.api.User) Iterables.getFirst(crowdService.search(new UserQuery(
                com.atlassian.crowd.embedded.api.User.class, new TermRestriction(UserTermKeys.EMAIL, MatchMode.EXACTLY_MATCHES,
                StringUtils.stripToEmpty(email).toLowerCase()), 0, 1)), null);

        if (user == null && !externalUserManagementService.isExternalUserManagement() && globalSettings.isCreatingUsers()) {
            try {
                user = createUser(identity, email).getDirectoryUser();
            } catch (Exception e) {
                log.error(String.format("Cannot create an account for %s %s", identity, email), e);
                templateHelper.render(request, response, "OpenId.Templates.error");
                return;
            }
        }

        if (user != null) {
            final ApplicationUser appUser = ApplicationUsers.from(user);

            getServiceDeskProjectIds(request).forEach((projectId) -> addToServiceDeskUserRole(appUser, projectId));

            final HttpSession httpSession = request.getSession();
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, appUser);
            httpSession.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
            getComponentOfType(LoginManager.class).onLoginAttempt(request, appUser.getName(), true);

            getComponentOfType(RememberMeService.class).addRememberMeCookie(request, response, appUser.getUsername());

            redirectionService.redirectToReturnUrlOrHome(request, response);
        } else {
            templateHelper.render(request, response, "OpenId.Templates.noUserMatched");
        }
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

    private ApplicationUser createUser(String identity, String email) throws Exception {
        Optional<Directory> directory = userManager.getWritableDirectories()
                .stream()
                .filter(dir -> userManager.canDirectoryUpdateUserPassword(dir))
                .findFirst();

        return userManager.createUser(new UserDetails(email, identity)
                .withEmail(email)
                .withDirectory(directory.get().getId()));
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
