package org.opensrp.web.utils;

import org.springframework.http.HttpHeaders;

public class HttpHeaderFactory {
	
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	
	public static HttpHeaders allowOrigin(String origin) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
		
		return headers;
	}
}
