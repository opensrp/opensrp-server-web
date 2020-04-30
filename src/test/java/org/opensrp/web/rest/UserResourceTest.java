/**
 *
 */
package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Samuel Githengi created on 10/14/19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class UserResourceTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Mock
	private OpenmrsUserService userService;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	private String BASE_URL = "/rest/user/";

	@Before
	public void setUp() {
		UserResource userResource = webApplicationContext.getBean(UserResource.class);
		userResource.setUserService(userService);
		mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext).
				addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
	}

	@Test
	public void testGetAllUsers() throws Exception {
		String expected = "{\"person\":{\"display\":\"Reveal Tes Demo\"},\"display\":\"reveal\",\"uuid\":\"5e33cf03-2352nkh\"}";
		int limit = 10;
		int offset = 5;
		when(userService.getUsers(limit, offset)).thenReturn(new JSONObject(expected));
		MvcResult result = mockMvc.perform(get(BASE_URL + "?page_size=10&start_index=5")).andExpect(status().isOk())
		        .andReturn();
		verify(userService).getUsers(limit, offset);
		verifyNoMoreInteractions(userService);
		assertEquals(expected, result.getResponse().getContentAsString());

	}
}
