package org.opensrp.web;

import org.opensrp.common.AllConstants;
import org.springframework.http.HttpHeaders;

public class HttpHeaderFactory {

	private static final String ACCESS_CONTROL_ALLOW_ORIGIN_METHODS = "Access-Control-Allow-Methods";
	private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADERS = "Access-Control-Allow-Header";
	private static final String ACCESS_CONTROL_ALLOW_MAX_AGE = "Access-Control-Max-Age";

	public static HttpHeaders allowOrigin(String origin) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(AllConstants.HTTP.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
		headers.add(ACCESS_CONTROL_ALLOW_ORIGIN_METHODS, "GET, POST");
		headers.add(ACCESS_CONTROL_ALLOW_ORIGIN_HEADERS, "origin, content-type, accept, x-requested-with");
		headers.add(ACCESS_CONTROL_ALLOW_MAX_AGE, "60");// 1 min
		return headers;
	}
}
