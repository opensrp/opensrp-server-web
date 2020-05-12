package org.opensrp.web.config.security.filter;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import java.io.IOException;

public class CrossSiteScriptingPreventionFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(CrossSiteScriptingPreventionFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
     // do nothing
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("Inside CrossSiteScriptingPreventionFilter  ...............");
		XssPreventionRequestWrapper wrappedRequest = new XssPreventionRequestWrapper(
				(HttpServletRequest) request);
		
		if((wrappedRequest.getMethod().equals(HttpMethod.POST) || wrappedRequest.getMethod().equals(HttpMethod.PUT))
				&& !request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
			String body = IOUtils.toString(wrappedRequest.getReader());
			wrappedRequest.resetInputStream();
			chain.doFilter(wrappedRequest, response);
		}
		else {
			chain.doFilter(request,response);
		}
		
	}

	@Override
	public void destroy() {
		// do nothing
	}

}


