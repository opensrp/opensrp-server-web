/**
 *
 */
package org.opensrp.web.config.security;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.web.config.Role;
import org.opensrp.web.security.OauthAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.net.InetAddress;

import static org.springframework.http.HttpMethod.OPTIONS;

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
	private CorsConfigurationSource corsConfigurationSource;

	@Autowired
	private OauthAuthenticationProvider opensrpAuthenticationProvider;

	@Autowired(required = false)
	private AuthorizationServerEndpointsConfiguration endpoints;

	@Value("#{opensrp['metrics.additional_ip_allowed'] ?: '' }")
	private String metricsAdditionalIpAllowed;

	@Value("#{opensrp['metrics.permitAll'] ?: false }")
	private boolean metricsPermitAll;

	@Override
	public void configure(ResourceServerSecurityConfigurer security) throws Exception {
		/* @formatter:off */
		security
			.resourceId(RESOURCE_ID)
			.tokenStore(jdbcTokenStore)
			.stateless(false)
			.authenticationEntryPoint(auth2AuthenticationEntryPoint());
		/* @formatter:on */
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		/* @formatter:off */
		http
			.requestMatcher(new AndRequestMatcher(new AntPathRequestMatcher("/**"),
		        new NotOAuthRequestMatcher(endpoints.oauth2EndpointHandlerMapping()),
		        new NegatedRequestMatcher(new AntPathRequestMatcher("/index.html")),
		        new NegatedRequestMatcher(new AntPathRequestMatcher("/")),
				new NegatedRequestMatcher(new AntPathRequestMatcher("/health")),
				new NegatedRequestMatcher(new AntPathRequestMatcher("/login*")),
		        new NegatedRequestMatcher(new AntPathRequestMatcher("/logout.do")),
		        new NegatedRequestMatcher(new AntPathRequestMatcher("rest/viewconfiguration/**"))))
			.authorizeRequests()
				.mvcMatchers(OPTIONS,"/**").permitAll()
				.mvcMatchers("/metrics")
				.access(metricsPermitAll ? "permitAll()" :
						" ( isAuthenticated()"
								+ " or hasIpAddress('127.0.0.1') "
								+ " or hasIpAddress('"+ InetAddress.getLocalHost().getHostAddress() +"') "
								+ (StringUtils.isBlank(metricsAdditionalIpAllowed) ? "" : String.format(" or hasIpAddress('%s')",metricsAdditionalIpAllowed)) + ")")
				.mvcMatchers("/rest/*/getAll").hasRole(Role.ALL_EVENTS)
				.mvcMatchers("/rest/plans/user/**").hasRole(Role.PLANS_FOR_USER)
				.anyRequest().hasRole(Role.OPENMRS)
		.and()
			.exceptionHandling().accessDeniedHandler(accessDeniedHandler())
		.and()
			.httpBasic()
		.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
		.and()
			.cors().configurationSource(corsConfigurationSource)
		.and()
			.authenticationProvider(opensrpAuthenticationProvider);
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
