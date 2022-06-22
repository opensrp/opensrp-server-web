/**
 *
 */
package org.opensrp;

import static org.mockito.Mockito.mock;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Samuel Githengi created on 05/13/20
 */
@Configuration
public class TestSecurityConfig {

    @Bean
    public KeycloakRestTemplate keycloakRestTemplate() {
        return mock(KeycloakRestTemplate.class);
    }

    @Bean
    public KeycloakDeployment keycloakDeployment() throws IOException {
        return mock(KeycloakDeployment.class);
    }

}
