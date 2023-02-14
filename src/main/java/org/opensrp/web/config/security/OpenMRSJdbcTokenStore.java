package org.opensrp.web.config.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.lang.reflect.Field;

public class OpenMRSJdbcTokenStore extends JdbcTokenStore {
    public OpenMRSJdbcTokenStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void storeAccessToken(final OAuth2AccessToken token, final OAuth2Authentication authentication) {
        try {

            // use reflection to access private fields in superclass
            Field jdbcTemplateField = JdbcTokenStore.class.getDeclaredField("jdbcTemplate");
            jdbcTemplateField.setAccessible(true);
            JdbcTemplate template = (JdbcTemplate) jdbcTemplateField.get(null);

            Field authKeyGeneratorField = JdbcTokenStore.class.getDeclaredField("authenticationKeyGenerator");
            jdbcTemplateField.setAccessible(true);
            AuthenticationKeyGenerator authKeyGenerator = (AuthenticationKeyGenerator) authKeyGeneratorField.get(null);

            final String key = authKeyGenerator.extractKey(authentication);
            template.update("delete from oauth_access_token where authentication_id = ?", key);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            super.storeAccessToken(token, authentication);
        }

    }

}
