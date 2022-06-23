package org.opensrp.web.config.security.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CrossSiteScriptingPreventionFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(CrossSiteScriptingPreventionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        logger.debug("Inside CrossSiteScriptingPreventionFilter  ...............");
        XssPreventionRequestWrapper wrappedRequest = new XssPreventionRequestWrapper(
                (HttpServletRequest) request);

        if ((wrappedRequest.getMethod().equals(HttpMethod.POST.name()) || wrappedRequest.getMethod()
                .equals(HttpMethod.PUT.name()))
                && request.getContentType() != null && !request.getContentType()
                .contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {
        // do nothing
    }

}


