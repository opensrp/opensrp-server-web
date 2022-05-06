/**
 * 
 */
package org.opensrp.web.config.security;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.web.config.Role;
import org.opensrp.web.security.DrishtiAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.InetAddress;
import java.util.Arrays;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * @author Samuel Githengi created on 03/09/20
 */

@EnableWebSecurity
@Configuration
@Profile("basic_auth")
public class BasicAuthSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private DrishtiAuthenticationProvider opensrpAuthenticationProvider;
	
	@Value("#{opensrp['opensrp.cors.allowed.source']}")
	private String opensrpAllowedSources;
	
	@Value("#{opensrp['opensrp.cors.max.age']}")
	private long corsMaxAge;
	
	private static final String CORS_ALLOWED_HEADERS = "origin,content-type,accept,x-requested-with,Authorization";

	@Value("#{opensrp['metrics.additional_ip_allowed'] ?: '' }")
	private String metricsAdditionalIpAllowed;

	@Value("#{opensrp['metrics.permitAll'] ?: false }")
	private boolean metricsPermitAll;

	@Autowired
	public void setOpensrpAuthenticationProvider(DrishtiAuthenticationProvider opensrpAuthenticationProvider) {
		this.opensrpAuthenticationProvider = opensrpAuthenticationProvider;
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		/* @formatter:off */
		http.cors().configurationSource(corsConfigurationSource());
		configureOpenSRPBasicSecurity(http)
		.mvcMatchers("/**").hasRole(Role.OPENMRS)
		.and()
    		.csrf()
    		.ignoringAntMatchers("/rest/**","/location/**", "/multimedia/upload","/actions/**");
		applyBasicAndStateless(http);
		/* @formatter:on */
	}
	
	protected ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry configureOpenSRPBasicSecurity(
	        HttpSecurity http) throws Exception {
		/* @formatter:off */
		return http.authorizeRequests()
		.mvcMatchers("/index.html").permitAll()
		.mvcMatchers("/").permitAll()
		.mvcMatchers("/health").permitAll()
				.mvcMatchers("/metrics")
				.access(metricsPermitAll ? "permitAll()" :
						" ( isAuthenticated()"
								+ " or hasIpAddress('127.0.0.1') "
								+ " or hasIpAddress('"+ InetAddress.getLocalHost().getHostAddress() +"') "
								+ (StringUtils.isBlank(metricsAdditionalIpAllowed) ? "" : String.format(" or hasIpAddress('%s')",metricsAdditionalIpAllowed)) + ")")
		.mvcMatchers("/multimedia/download/**").permitAll()
		.mvcMatchers("/multimedia/profileimage/**").permitAll()
		.mvcMatchers("/multimedia/media/**").permitAll()
		.mvcMatchers("/rest/viewconfiguration/**").permitAll()
		.mvcMatchers("/rest/viewconfiguration/**").permitAll()
		.mvcMatchers("/rest/*/getAll").hasRole(Role.ALL_EVENTS)
		.mvcMatchers("/rest/plans/user/**").hasRole(Role.PLANS_FOR_USER)
		.mvcMatchers(OPTIONS,"/**").permitAll();
		/* @formatter:on */
		
	}
	
	protected HttpSecurity applyBasicAndStateless(HttpSecurity http) throws Exception {
		/* @formatter:off */
		return http.httpBasic().and()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();
		/* @formatter:on */
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(opensrpAuthenticationProvider);
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		/* @formatter:off */
		web.ignoring().mvcMatchers("/js/**")
			.and().ignoring().mvcMatchers("/css/**")
			.and().ignoring().mvcMatchers("/images/**");
		/* @formatter:on */
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(opensrpAllowedSources.split(",")));
		configuration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), DELETE.name()));
		configuration.setAllowedHeaders(Arrays.asList(CORS_ALLOWED_HEADERS.split(",")));
		configuration.setMaxAge(corsMaxAge);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
}
