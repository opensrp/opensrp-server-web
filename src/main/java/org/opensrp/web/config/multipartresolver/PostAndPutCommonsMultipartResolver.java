package org.opensrp.web.config.multipartresolver;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * This class is added to provide support for Multipart Request for PUT method.
 *
 * By defaut only POST is checked
 * Therefore, the method is overridden to provide support for both POST and PUT
 */

public class PostAndPutCommonsMultipartResolver extends CommonsMultipartResolver {

	private static final String POST_METHOD = "POST";
	private static final String PUT_METHOD = "PUT";

	@Override
	public boolean isMultipart(HttpServletRequest request) {

		boolean isMultipartRequest = false;
		if (request != null && (POST_METHOD.equalsIgnoreCase(request.getMethod()) || PUT_METHOD
				.equalsIgnoreCase(request.getMethod()))) {
			isMultipartRequest = FileUploadBase.isMultipartContent(new ServletRequestContext(request));
		}

		return isMultipartRequest;
	}
}
