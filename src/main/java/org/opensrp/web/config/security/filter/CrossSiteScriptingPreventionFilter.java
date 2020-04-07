package org.opensrp.web.config.security.filter;

import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CrossSiteScriptingPreventionFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(CrossSiteScriptingPreventionFilter.class);

	public void init(FilterConfig filterConfig) {
		// do nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.info("Inlter CrossScriptingFilter  ...............");
		chain.doFilter(new XssPreventionRequestWrapper((HttpServletRequest) request), response);
		logger.info("Outlter CrossScriptingFilter ...............");
	}

	public void destroy() {
		// do nothing
	}

}

