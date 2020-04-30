/**
 * 
 */
package org.opensrp.web.config.security;

import org.opensrp.web.config.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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

@Profile("oauth2")
@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
	
	public static final String RESOURCE_ID = "OpenSRP2";
	
	@Autowired
	private JdbcTokenStore jdbcTokenStore;
	
	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	
	@Override
	public void configure(ResourceServerSecurityConfigurer security) throws Exception {
		/* @formatter:off */
		security
			.resourceId(RESOURCE_ID)
			.tokenStore(jdbcTokenStore)
			.stateless(false)
			.authenticationEntryPoint(auth2AuthenticationEntryPoint())
			.authenticationManager(authenticationManager);
		/* @formatter:on */
	}
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
		/* @formatter:off */
		http.cors()
		.and()
			.anonymous().disable()
		.requestMatchers().mvcMatchers("/rest/**","/user-details","/security/**", "/uniqueids/*")
		.and()
			.authorizeRequests()
				.mvcMatchers("/rest/*/getAll").hasRole(Role.ALL_EVENTS)
				.mvcMatchers("/rest/plans/findByUsername").hasRole(Role.PLANS_FOR_USER)
				.anyRequest().hasRole(Role.OPENMRS)
		.and()
			.exceptionHandling().accessDeniedHandler(accessDeniedHandler())
		.and()
			.httpBasic();
		/* @formatter:on */
	}
	
	@Bean
	public OAuth2AccessDeniedHandler accessDeniedHandler() {
		return new OAuth2AccessDeniedHandler();
	}
	
	@Bean
	public OAuth2AuthenticationEntryPoint auth2AuthenticationEntryPoint() {
		OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint();
		entryPoint.setRealmName(RESOURCE_ID);
		return entryPoint;
	}
}
