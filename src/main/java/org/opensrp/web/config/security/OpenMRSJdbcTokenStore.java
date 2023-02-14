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
            Field jdbcTemplateField = Class.forName("org.springframework.security.oauth2.provider.token.store.JdbcTokenStore")
                    .getDeclaredField("jdbcTemplate");
            jdbcTemplateField.setAccessible(true);
            JdbcTemplate template = (JdbcTemplate) jdbcTemplateField.get(null);

            Field authenticationKeyGeneratorField = Class.forName("org.springframework.security.oauth2.provider.token.store.JdbcTokenStore")
                    .getDeclaredField("jdbcTemplate");
            jdbcTemplateField.setAccessible(true);
            AuthenticationKeyGenerator authKeyGenerator = (AuthenticationKeyGenerator) authenticationKeyGeneratorField.get(null);

            final String key = authKeyGenerator.extractKey(authentication);
            template.update("delete from oauth_access_token where authentication_id = ?", key);

        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            super.storeAccessToken(token, authentication);
        }

    }

}
