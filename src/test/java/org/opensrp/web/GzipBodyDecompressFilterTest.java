package org.opensrp.web;

import org.apache.http.HttpHeaders;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

public class GzipBodyDecompressFilterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();


    @Test
    public void testDoFilterWithWrongHttpMethodThrowsIllegalStateException() throws IOException, ServletException {

        GzipBodyDecompressFilter gzipBodyDecompressFilter = new GzipBodyDecompressFilter();

        MockFilterChain mockChain = new MockFilterChain();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", "/rest/upload");
        mockRequest.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        exception.expect(IllegalStateException.class);
        gzipBodyDecompressFilter.doFilter(mockRequest, mockResponse, mockChain);

    }
}