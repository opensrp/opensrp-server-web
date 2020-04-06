package org.opensrp.web.config.security.filter;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class XssPreventionRequestWrapper extends HttpServletRequestWrapper {

	private static Logger logger = LoggerFactory.getLogger(XssPreventionRequestWrapper.class);

	public XssPreventionRequestWrapper(HttpServletRequest servletRequest) {
		super(servletRequest);
	}

	@Override
	public String[] getParameterValues(String parameter) {
		logger.info("Inside getParameterValues() method");
		String[] values = super.getParameterValues(parameter);
		if (values == null) {
			return null;
		}
		int count = values.length;
		String[] encodedValues = new String[count];
		for (int i = 0; i < count; i++) {
			encodedValues[i] = sanitizeRequestParam(values[i]);
		}
		return encodedValues;
	}

	@Override
	public String getParameter(String parameter) {
		logger.info("Inside getParameter() method");
		String value = super.getParameter(parameter);
		if (value == null) {
			return null;
		}
		return sanitizeRequestParam(value);
	}

	@Override
	public String getHeader(String name) {
		logger.info("Inside getHeader() Method");
		String value = super.getHeader(name);
		if (value == null)
			return null;
		return sanitizeRequestParam(value);
	}

	private String sanitizeRequestParam(String value) {
		logger.info("Sanitizing request param : " + value);
		return Encode.forUriComponent(value);
	}
}
