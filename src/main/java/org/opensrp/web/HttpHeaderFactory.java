package org.opensrp.web;

import org.springframework.http.HttpHeaders;

public class HttpHeaderFactory {

    public static HttpHeaders allowOrigin(String origin) {
        return new HttpHeaders();
    }
}
