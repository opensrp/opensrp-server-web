package org.opensrp.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.web.Constants;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.serviceimpl.HealthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HealthControllerTest {

	@InjectMocks
	private HealthController healthController;

	@Mock
	private HealthServiceImpl healthService;

	private MockMvc mockMvc;

	private final String baseEndpoint = "/health";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(healthController)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		ReflectionTestUtils.setField(healthController, "healthService", healthService);
	}

	@Test
	public void testIndexShouldReturnOk() throws Exception {
		ModelMap modelMap = new ModelMap();
		modelMap.put(Constants.HealthIndicator.PROBLEMS, new ModelMap());
		modelMap.put(Constants.HealthIndicator.TIME, "-");
		modelMap.put(Constants.HealthIndicator.SERVICES, new ModelMap());

		doReturn(modelMap).when(healthService).aggregateHealthCheck();
		MvcResult result = mockMvc.perform(get(baseEndpoint))
				.andExpect(status().isOk()).andReturn();

		assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
	}
}
