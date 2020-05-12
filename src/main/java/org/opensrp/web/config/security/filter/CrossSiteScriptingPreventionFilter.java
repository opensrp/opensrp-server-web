package org.opensrp.web.config.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.opensrp.common.audit.Auditor;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class CrossSiteScriptingPreventionFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(CrossSiteScriptingPreventionFilter.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private Auditor auditor;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
     // do nothing
	}
	
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		logger.debug("Inside CrossSiteScriptingPreventionFilter  ...............");
		XssPreventionRequestWrapper wrappedRequest = new XssPreventionRequestWrapper(
				(HttpServletRequest) request);
		String body = IOUtils.toString(wrappedRequest.getReader());
		wrappedRequest.resetInputStream();
		chain.doFilter(wrappedRequest, response);
	}

	@Override
	public void destroy() {
		// do nothing
	}

}


