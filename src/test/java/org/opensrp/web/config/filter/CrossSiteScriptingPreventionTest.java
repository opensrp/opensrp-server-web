package org.opensrp.web.config.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.config.security.filter.XssPreventionRequestWrapper;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class CrossSiteScriptingPreventionTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@InjectMocks
	CrossSiteScriptingPreventionFilter crossSiteScriptingPreventionFilter;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup( context ).
				addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
	}


	@Test
	public void testGetParameter() {
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest(HttpMethod.GET.toString(),"/actions?anmIdentifier=123&timeStamp=123");
		mockHttpServletRequest.setParameter("anmIdentifier", "<script>hi</script>");
		String queryParam = "<script>hi</script>";
		queryParam = Encode.forUriComponent(queryParam);
		XssPreventionRequestWrapper xssPreventionRequestWrapper = new XssPreventionRequestWrapper(mockHttpServletRequest);
		String sanitizedParameterFromFilter = xssPreventionRequestWrapper.getParameter("anmIdentifier");
		assertEquals(queryParam, sanitizedParameterFromFilter);
	}

}
