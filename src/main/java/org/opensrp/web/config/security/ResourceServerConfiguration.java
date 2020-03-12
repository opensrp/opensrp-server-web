/**
 * 
 */
package org.opensrp.web.config.security;

import org.opensrp.web.config.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

/**
 * @author Samuel Githengi created on 03/11/20
 */

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
	
	private static final String RESOURCE_ID = "OpenSRP2";
	
	@Autowired
	private OAuth2AccessDeniedHandler oauthAccessDeniedHandler;
	
	@Autowired
	private JdbcTokenStore jdbcTokenStore;
	
	@Autowired
	private OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint;
	
	@Autowired
	@Qualifier("authenticationManager")
	private AuthenticationManager authenticationManager;

	@Override
	public void configure(ResourceServerSecurityConfigurer security) throws Exception {
		security
			.resourceId(RESOURCE_ID)
			.tokenStore(jdbcTokenStore)
			.stateless(false)
			.authenticationEntryPoint(oauthAuthenticationEntryPoint)
			.authenticationManager(authenticationManager);
	}
	
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
		.anonymous()
        .disable()
		.requestMatchers().mvcMatchers("/rest/**")
		.and()
			.authorizeRequests()
			.mvcMatchers("/rest/**").hasRole(Role.OPENMRS)
		.and()
			.exceptionHandling().accessDeniedHandler(oauthAccessDeniedHandler)
		.and()
		.httpBasic();
	}
}
