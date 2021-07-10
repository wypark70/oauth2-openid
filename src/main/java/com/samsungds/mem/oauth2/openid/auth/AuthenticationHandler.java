package com.samsungds.mem.oauth2.openid.auth;

import com.samsungds.mem.oauth2.openid.config.AuthenticationInfo;
import com.samsungds.mem.oauth2.openid.model.Tokens;
import com.auth0.client.auth.AuthAPI;
import com.auth0.client.auth.AuthorizeUrlBuilder;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.auth.UserInfo;
import com.samsungds.mem.oauth2.openid.servlet.CallbackAuthorizeServlet;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class AuthenticationHandler {
    private static final String KEY_SUB = "sub";
    private static final String KEY_STATE = "state";
    private static final String KEY_ERROR = "error";
    private static final String KEY_ERROR_DESCRIPTION = "error_description";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ID_TOKEN = "id_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_CODE = "code";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_RESPONSE_MODE = "response_mode";
    private static final String KEY_FORM_POST = "form_post";

    private final AuthAPI authAPI;
    private final AuthenticationInfo authInfo;

    public AuthenticationHandler(AuthAPI authAPI, AuthenticationInfo authInfo) {
        this.authAPI = authAPI;
        this.authInfo = authInfo;
    }

    public Tokens handle(HttpServletRequest req) {
        String authorizationCode = req.getParameter(KEY_CODE);
        String redirectUri = req.getRequestURL().toString();
        log.debug("authorizationCode: {}", authorizationCode);
        log.debug("redirectUri: {}", redirectUri);

        UserInfo userInfo;
        TokenHolder tokenHolder;
        try {
            tokenHolder = getTokenHolder(authorizationCode, redirectUri);
            log.debug("tokenHolder: {}", tokenHolder);
            userInfo = getUserInfo(tokenHolder.getAccessToken());
        } catch (Auth0Exception e) {
            e.printStackTrace();
            throw new AuthenticationException("An error occurred while exchanging the Authorization Code for Auth0 Tokens.", e);
        }
        if (!userInfo.getValues().containsKey(KEY_SUB)) {
            throw new AuthenticationException("An error occurred while trying to verify the user identity: The 'sub' claim contained in the token was null.");
        }
        return new Tokens(tokenHolder.getAccessToken(), tokenHolder.getIdToken(),
                            tokenHolder.getRefreshToken(), tokenHolder.getTokenType(), tokenHolder.getExpiresIn());
    }

    private TokenHolder getTokenHolder(String authorizationCode, String redirectUri) throws Auth0Exception {
        return authAPI
                .exchangeCode(authorizationCode, redirectUri)
                .execute();
    }

    public UserInfo getUserInfo(String accessToken) throws Auth0Exception {
        return authAPI
                .userInfo(accessToken)
                .execute();
    }

    public AuthorizeUrlBuilder authorizeUrl(String redirectUri) {
        return authAPI.authorizeUrl(redirectUri);
    }

    public String getDomain() {
        return authInfo.getDomain();
    }

}
