package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.viewconfiguration.ViewConfiguration;
import org.opensrp.service.ViewConfigurationService;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class ViewConfigurationResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private ViewConfigurationService viewConfigurationService;

	@InjectMocks
	private ViewConfigurationResource viewConfigurationResource;

	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/rest/viewconfiguration";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(viewConfigurationResource)
				.build();
	}

	@Test
	public void testFindViewConfigurationsByVersion() throws Exception {
		List<ViewConfiguration> expected = new ArrayList<>();
		expected.add(createViewConfiguration());

		when(viewConfigurationService.findViewConfigurationsByVersion(any(Long.class))).thenReturn(expected);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "15421904649873"))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);

		assertEquals(actualObj.get(0).get("identifier").asText(), "123");
		assertEquals(actualObj.get(0).get("_id").asText(), "TEST-ID");
		assertEquals(actualObj.get(0).get("type").asText(), "Test");
		assertEquals(actualObj.size(), 1);

	}

	private ViewConfiguration createViewConfiguration() {
		ViewConfiguration viewConfiguration = new ViewConfiguration();
		viewConfiguration.setIdentifier("123");
		viewConfiguration.setType("Test");
		viewConfiguration.setId("TEST-ID");
		return viewConfiguration;
	}

}
