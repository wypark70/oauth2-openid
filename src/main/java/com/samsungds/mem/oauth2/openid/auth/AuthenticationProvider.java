package com.samsungds.mem.oauth2.openid.auth;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.auth0.client.auth.AuthAPI;
import com.samsungds.mem.oauth2.openid.config.AuthenticationInfo;
import com.samsungds.mem.oauth2.openid.config.AuthenticationInfoException;
import com.samsungds.mem.oauth2.openid.util.AuthenticationInfoChecker;

@JiraComponent
public class AuthenticationProvider {

    private final TransactionTemplate transactionTemplate;

    private final PluginSettingsFactory pluginSettingsFactory;

    public AuthenticationProvider(@JiraImport TransactionTemplate transactionTemplate,
                                  @JiraImport PluginSettingsFactory pluginSettingsFactory) {
        this.transactionTemplate = transactionTemplate;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    public AuthenticationHandler getInstance() throws AuthenticationInfoException {
        return transactionTemplate.execute(() -> {
            PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            AuthenticationInfo config = new AuthenticationInfo();
            config.setDomain((String) settings.get(AuthenticationInfo.class.getName() + ".domain"));
            config.setClientId((String) settings.get(AuthenticationInfo.class.getName() + ".clientId"));
            config.setClientSecret((String) settings.get(AuthenticationInfo.class.getName() + ".clientSecret"));

            AuthenticationInfoChecker.checkAuthenticationInfo(config);
            AuthAPI authAPI = new AuthAPI(config.getDomain(), config.getClientId(), config.getClientSecret());
            return new AuthenticationHandler(authAPI, config);
        });
    }


}