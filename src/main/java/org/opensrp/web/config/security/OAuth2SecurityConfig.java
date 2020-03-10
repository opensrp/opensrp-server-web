/**
 * 
 */
package org.opensrp.web.config.security;

import org.opensrp.web.security.OauthAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * @author Samuel Githengi created on 03/10/20
 */
@EnableWebSecurity
@Configuration
@Profile("oauth2")
public class OAuth2SecurityConfig extends BasicAuthSecurityConfig{
	
	@Autowired
	public void setOpensrpAuthenticationProvider(OauthAuthenticationProvider opensrpAuthenticationProvider) {
		super.setOpensrpAuthenticationProvider(opensrpAuthenticationProvider);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
	}
	
}
