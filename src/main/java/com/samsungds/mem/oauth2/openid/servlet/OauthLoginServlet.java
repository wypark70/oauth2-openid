package com.samsungds.mem.oauth2.openid.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.samsungds.mem.oauth2.openid.auth.AuthenticationHandler;
import com.samsungds.mem.oauth2.openid.auth.AuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@JiraComponent
public class OauthLoginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(OauthLoginServlet.class);

    private static final String INIT_PARAM_CALLBACK_PATH = "callbackPath";

    private final AuthenticationProvider authenticationProvider;

    private String callbackPath;

    public OauthLoginServlet(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void init() throws ServletException {
        log.debug("Initializing {} servlet", this.getClass().getName());

        super.init();
        callbackPath = getServletConfig().getInitParameter(INIT_PARAM_CALLBACK_PATH);
        log.info("Callback servlet path init: {}", callbackPath);

        log.debug("Initialization of {} servlet finished", this.getClass().getName());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String redirectUri = req.getScheme() + "://" + req.getServerName() + ":"
                + req.getServerPort() + callbackPath;
        log.info("Redirect uri: {}", redirectUri);

        AuthenticationHandler authHandler = authenticationProvider.getInstance();
        String authorizeUrl = authHandler.authorizeUrl(redirectUri)
                .withAudience(String.format("%s/userinfo", authHandler.getDomain()))
                .withScope("openid profile email")
                .build();

        log.info("Redirect on {} for authorization", authorizeUrl);
        resp.sendRedirect(authorizeUrl);
    }
}