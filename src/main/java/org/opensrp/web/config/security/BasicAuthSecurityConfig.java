/**
 * 
 */
package org.opensrp.web.config.security;

import org.opensrp.web.config.Role;
import org.opensrp.web.security.DrishtiAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author Samuel Githengi created on 03/09/20
 */

@EnableWebSecurity
@Configuration
@Profile("basic_auth")
public class BasicAuthSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private DrishtiAuthenticationProvider opensrpAuthenticationProvider;
	
	@Autowired
	public void setOpensrpAuthenticationProvider(DrishtiAuthenticationProvider opensrpAuthenticationProvider) {
		this.opensrpAuthenticationProvider = opensrpAuthenticationProvider;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		configureOpenSRPBasicSecurity(http).mvcMatchers("/**").hasRole(Role.OPENMRS);
		applyBasicAndStateless(http);
	}
	
	
	protected ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry configureOpenSRPBasicSecurity(HttpSecurity http) throws Exception {
		return http.authorizeRequests()
		.mvcMatchers("/index.html").anonymous()
		.mvcMatchers("/").anonymous()
		.mvcMatchers("/multimedia/download/**").anonymous()
		.mvcMatchers("/multimedia/profileimage/**").anonymous()
		.mvcMatchers("/multimedia/media/**").anonymous()
		.mvcMatchers("/rest/viewconfiguration/**").anonymous()
		.mvcMatchers("/rest/viewconfiguration/**").anonymous()
		.mvcMatchers("/rest/*/getAll").hasRole(Role.ALL_EVENTS)
		.mvcMatchers(HttpMethod.OPTIONS,"/**").anonymous();
		
	}
	
	protected HttpSecurity applyBasicAndStateless(HttpSecurity http) throws Exception {
		return http.httpBasic().and()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();
		
	}
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(opensrpAuthenticationProvider);
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().mvcMatchers("/js/**")
			.and().ignoring().mvcMatchers("/css/**")
			.and().ignoring().mvcMatchers("/images/**");
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
}
