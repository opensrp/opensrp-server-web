/**
 * 
 */
package org.opensrp.web.config.security;

import org.opensrp.web.config.Role;
import org.opensrp.web.security.OauthAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @author Samuel Githengi created on 03/10/20
 */
@EnableWebSecurity(debug = true)
@Configuration
@Profile("oauth2")
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class OAuth2SecurityConfig extends BasicAuthSecurityConfig{
	
	
	@Autowired
	private AccessDecisionManager accessDecisionManager;
	
	/*@Autowired
	private AuthenticationEntryPoint authenticationEntryPoint;*/
	
	@Autowired
	@Qualifier("authenticationManager")
	private AuthenticationManager authenticationManager;
	
	@Qualifier("userAuthenticationProvider")
	@Autowired
	public void setOpensrpAuthenticationProvider(OauthAuthenticationProvider opensrpAuthenticationProvider) {
		super.setOpensrpAuthenticationProvider(opensrpAuthenticationProvider);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/oauth/token").permitAll();
		configureOpenSRPBasicSecurity(http)
				.mvcMatchers("/login**").permitAll()
				.antMatchers("/oauth/token").permitAll().accessDecisionManager(accessDecisionManager)
				.mvcMatchers("/**").hasRole(Role.OPENMRS)
		        .and()
		        	.formLogin()
		        	.usernameParameter("j_username")
		        	.passwordParameter("j_password")
		        	.loginProcessingUrl("/login.do")
		        	.loginPage("/login.jsp")
		        	.defaultSuccessUrl("/index.html")
		        	.failureUrl("/login.jsp?authentication_error=true")
		        .and()
		        	.logout()
		        	.logoutRequestMatcher(new AntPathRequestMatcher("/logout.do") )
		        	.logoutSuccessUrl("/index.html")
		        .and()
		        	.httpBasic()//.authenticationEntryPoint(authenticationEntryPoint)
		        .and()
		          	.sessionManagement()
		          	.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
		          	.enableSessionUrlRewriting(true)
		        .and()
		        	.exceptionHandling().accessDeniedPage("/login.jsp?authentication_error=true")
		        .and()
		        	.csrf()
		        	.ignoringAntMatchers("/rest/**");
	}
	
}
