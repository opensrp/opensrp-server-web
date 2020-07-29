/**
 * 
 */
package org.opensrp.web.config.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpointHandlerMapping;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @author Samuel Githengi created on 07/28/20
 */
public class NotOAuthRequestMatcher implements RequestMatcher {
	
	private FrameworkEndpointHandlerMapping mapping;
	
	public NotOAuthRequestMatcher(FrameworkEndpointHandlerMapping mapping) {
		this.mapping = mapping;
	}
	
	@Override
	public boolean matches(HttpServletRequest request) {
		String requestPath = getRequestPath(request);
		for (String path : mapping.getPaths()) {
			if (requestPath.startsWith(mapping.getPath(path))) {
				return false;
			}
		}
		return true;
	}
	
	private String getRequestPath(HttpServletRequest request) {
		String url = request.getServletPath();
		
		if (request.getPathInfo() != null) {
			url += request.getPathInfo();
		}
		
		return url;
	}
	
}
