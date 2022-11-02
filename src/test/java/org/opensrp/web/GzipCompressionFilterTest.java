package org.opensrp.web;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.ServletException;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "oauth2"})
public class GzipCompressionFilterTest {
    private GZipCompressionFilter gZipCompressionFilter;
    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;
    private MockFilterChain filterChain;

    @Before
    public void setUp() {
        gZipCompressionFilter = Mockito.mock(GZipCompressionFilter.class);
        httpServletRequest = new MockHttpServletRequest();
        httpServletResponse = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        httpServletRequest.addHeader("Content-Encoding", "application/gzip");
        httpServletRequest.addHeader("accept-encoding", "application/gzip");
        httpServletRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");

    }

    @Test
    public void testDoFilter() throws ServletException, IOException {
        GZipCompressionFilter zipCompressionFilter = new GZipCompressionFilter();
        Mockito.doNothing().when(gZipCompressionFilter).doFilter(httpServletRequest, httpServletResponse, filterChain);
        zipCompressionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);
        Assert.assertNotNull(filterChain.getRequest());

    }
}