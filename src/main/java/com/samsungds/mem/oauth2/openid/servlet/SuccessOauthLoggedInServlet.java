package com.samsungds.mem.oauth2.openid.servlet;

import com.atlassian.jira.bc.user.search.UserSearchService;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.samsungds.mem.oauth2.openid.util.SessionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@JiraComponent
public class SuccessOauthLoggedInServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(SuccessOauthLoggedInServlet.class);
    private static final String SUCCESS_LOGIN_PAGE = "/templates/success-login.vm";
    private static final String JIRA_SOFTWARE_USERS_GROUP = "jira-software-users";

    @JiraImport
    private final TemplateRenderer templateRenderer;

    @JiraImport
    private final UserManager userManager;

    @JiraImport
    private final UserSearchService userSearchService;

    @JiraImport
    private final JiraAuthenticationContext authContext;

    @JiraImport
    private final UserUtil userUtil;

    @JiraImport
    private final GroupManager groupManager;

    public SuccessOauthLoggedInServlet(TemplateRenderer templateRenderer, UserManager userManager,
                                       UserSearchService userSearchService, JiraAuthenticationContext authContext,
                                       UserUtil userUtil, GroupManager groupManager) {
        this.templateRenderer = templateRenderer;
        this.userManager = userManager;
        this.userSearchService = userSearchService;
        this.authContext = authContext;
        this.userUtil = userUtil;
        this.groupManager = groupManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idToken = (String) req.getSession().getAttribute(SessionConstants.ID_TOKEN);
        // todo: remove tokens from logs
        log.debug("Token {}: {}", SessionConstants.ID_TOKEN, idToken);

        String accessToken = (String) req.getSession().getAttribute(SessionConstants.ACCESS_TOKEN);
        log.debug("Token {}: {}", SessionConstants.ACCESS_TOKEN, accessToken);

        Map<String, Object> userInfoValues = (Map<String, Object>) req.getSession().getAttribute(SessionConstants.USER_INFO);

        Map<String, Object> context = new HashMap<>();
        context.put("userInfo", userInfoValues);
        context.put("userName", userInfoValues.get("nickname"));

        log.info("User info: {}", userInfoValues);

        Iterable<ApplicationUser> usersByEmail = userSearchService.findUsersByEmail((String) userInfoValues.get("email"));
        Iterator<ApplicationUser> iterator = usersByEmail.iterator();
        ApplicationUser appUser;

        if (iterator.hasNext()) {
            appUser = iterator.next();
            log.info("Found user in the system: {}", appUser);
        } else {
            log.info("User with email {} not found", userInfoValues.get("email"));
            appUser = createApplicationUser(userInfoValues);
            if (appUser == null) {
                log.error("User was not created!");
                templateRenderer.render(SUCCESS_LOGIN_PAGE, resp.getWriter());
                return;
            }
        }
        try {
            loginUser(req, appUser);
        } catch (AddException | PermissionException e) {
            log.error("Cannot log in user! {}", e.getMessage());
            e.printStackTrace();
        }

        log.info("Render template: {}", SUCCESS_LOGIN_PAGE);
        templateRenderer.render(SUCCESS_LOGIN_PAGE, context, resp.getWriter());
    }

    private ApplicationUser createApplicationUser(Map<String, Object> userInfoValues) {
        log.info("Creating new user with email {}", userInfoValues.get("email"));
        UserDetails userDetails = new UserDetails((String) userInfoValues.get("nickname"), (String) userInfoValues.get("name"))
                .withEmail((String) userInfoValues.get("email"))
                .withPassword("1");

        ApplicationUser appUser;
        try {
            appUser = userManager.createUser(userDetails);
            log.info("User created: {}", appUser);
        } catch (CreateException e) {
            log.error("Cannot create user! {}", e.getMessage());
            e.printStackTrace();
            return null;
        } catch (PermissionException e) {
            log.error("Do not have enough permissions to create user! {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
        return appUser;
    }

    private void loginUser(HttpServletRequest req, ApplicationUser appUser)
            throws AddException, PermissionException {
        authContext.setLoggedInUser(appUser);
        log.info("Log in retrieved user {}", appUser);

        userUtil.addUserToGroup(groupManager.getGroup(JIRA_SOFTWARE_USERS_GROUP), appUser);

        HttpSession session = req.getSession();
        session.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, appUser);
        session.setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
    }

}
