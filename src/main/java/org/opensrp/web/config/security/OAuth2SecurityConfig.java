/**
 *
 */
package org.opensrp.web.config.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.config.Role;
import org.opensrp.web.security.OauthAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

/**
 * @author Samuel Githengi created on 03/10/20
 */
@EnableWebSecurity
@Configuration
@Profile("oauth2")
public class OAuth2SecurityConfig extends BasicAuthSecurityConfig {
	
	@Autowired
	private OauthAuthenticationProvider opensrpAuthenticationProvider;
	
	@Autowired
	private ClientDetailsService clientDetailsService;
	
	@Qualifier(value = "openSRPDataSource")
	@Autowired
	private DataSource dataSource;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		/* @formatter:off */
		configureOpenSRPBasicSecurity(http)
				.mvcMatchers("/login**").permitAll()
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
		        	.httpBasic()
		        .and()
		        	.exceptionHandling().accessDeniedPage("/login.jsp?authentication_error=true")
		        .and()
		        	.csrf()
		        	.ignoringAntMatchers("/rest/**","/actions/**")
		        .and().authenticationProvider(opensrpAuthenticationProvider);
		/* @formatter:on */
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(opensrpAuthenticationProvider).eraseCredentials(false);
	}
	
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices tokenServices = new DefaultTokenServices();
		tokenServices.setTokenStore(tokenStore());
		tokenServices.setSupportRefreshToken(true);
		tokenServices.setClientDetailsService(clientDetailsService);
		return tokenServices;
	}
	
	@Bean
	public JdbcTokenStore tokenStore() {
		final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		final AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
		Logger logger = LogManager.getLogger(JdbcTokenStore.class.toString());
		return new JdbcTokenStore(dataSource) {
			
			@Override
			public void storeAccessToken(final OAuth2AccessToken token, final OAuth2Authentication authentication) {
				logger.info("Invoking store access token method");
				if (authentication != null) {
					final String key = authenticationKeyGenerator.extractKey(authentication);
					int rowsAffected = jdbcTemplate.update("delete from oauth_access_token where authentication_id = ?",
							key);
					String isSuccess = (rowsAffected > 0) ? "Success" : "Failure";
					logger.info("Attempt to delete authentication_id {} from oauth_access_token table was a {}", key,
							isSuccess);
				}
				
				super.storeAccessToken(token, authentication);
			}
			
		};
	}
	
	
	
}
