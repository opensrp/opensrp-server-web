package org.opensrp.web.config.security;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

public class OpenMRSJdbcTokenStore extends JdbcTokenStore {
    public OpenMRSJdbcTokenStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void storeAccessToken(final OAuth2AccessToken token, final OAuth2Authentication authentication) {
        final String key = authenticationKeyGenerator.extractKey(authentication);
        jdbcTemplate.update("delete from oauth_access_token where authentication_id = ?", key);
        super.storeAccessToken(token, authentication);
    }

}
