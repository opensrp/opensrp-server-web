/**
 * 
 */
package org.opensrp;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Samuel Githengi created on 05/13/20
 */
@Configuration
public class TestSecurityConfig {
	
	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public KeycloakRestTemplate keycloakRestTemplate() {
		return mock(KeycloakRestTemplate.class);
	}
	
	@Bean
	public KeycloakDeployment keycloakDeployment() throws IOException {
		return mock(KeycloakDeployment.class);
	}
	
}
