package org.opensrp.web.config;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.web.AuthenticationFilter;
import org.opensrp.web.GZipCompressionFilter;
import org.opensrp.web.GzipBodyDecompressFilter;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vincent Karuri on 26/11/2020
 */

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean gzipCompressionFilter() {
		return createFilterRegistration(new GZipCompressionFilter(), new HashMap<>(), null);
	}

	@Bean
	public FilterRegistrationBean gzipBodyDecompressFilter() {
		return createFilterRegistration(new GzipBodyDecompressFilter(), new HashMap<>(), null);
	}

	@Bean
	public FilterRegistrationBean crossSiteScriptingPreventionFilter() {
		return createFilterRegistration(new CrossSiteScriptingPreventionFilter(), new HashMap<>(), null);
	}

	@Bean
	public FilterRegistrationBean springSessionRepositoryFilter() {
		return createFilterRegistration(new DelegatingFilterProxy(), new HashMap<>(), null);
	}

	@Bean
	public FilterRegistrationBean httpMethodFilter() {
		return createFilterRegistration(new HiddenHttpMethodFilter(), new HashMap<>(), null);
	}

	@Bean
	public FilterRegistrationBean characterEncodingFilter() {
		return createFilterRegistration(new CharacterEncodingFilter(), new HashMap<>(){{
			put("encoding", "UTF-8");
			put("forceEncoding", "true");
		}}, null);
	}

	@Bean
	public FilterRegistrationBean authenticationFilter() {
		return createFilterRegistration(new AuthenticationFilter(), new HashMap<>(), "/authenticate-user/");
	}

	@Bean
	public FilterRegistrationBean springSecurityFilterChain() {
		return createFilterRegistration(new DelegatingFilterProxy(), new HashMap<>(), null);
	}

	private FilterRegistrationBean createFilterRegistration(Filter filter, Map<String, String> initParams, String urlPattern) {
		FilterRegistrationBean registration = new FilterRegistrationBean(filter);
		if (StringUtils.isNotBlank(urlPattern)) {
			registration.addUrlPatterns(urlPattern);
		} else {
			registration.addUrlPatterns("/*");
		}
		for (Map.Entry<String, String> keyValPairs : initParams.entrySet()) {
			registration.addInitParameter(keyValPairs.getKey(), keyValPairs.getValue());
		}
		return registration;
	}
}
