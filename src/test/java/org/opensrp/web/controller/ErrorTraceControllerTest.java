package org.opensrp.web.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.domain.ErrorTrace;
import org.opensrp.service.ErrorTraceService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorTraceControllerTest {

	@InjectMocks
	private ErrorTraceController errorTraceController;

	@Mock
	private ErrorTraceService errorTraceService;

	private final String BASE_URL = "/errorhandler";

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(errorTraceController)
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
	}

	@Test
	public void testAllErrorsShouldReturnListIfExists() throws Exception {
		ErrorTrace errorTrace = new ErrorTrace();
		errorTrace.setErrorType("error");
		errorTrace.setId("1");
		List<ErrorTrace> errorTraceList = Collections.singletonList(errorTrace);
		doReturn(errorTraceList).when(errorTraceService).getAllErrors();
		MvcResult result = mockMvc.perform(get(BASE_URL + "/errortrace"))
							.andExpect(status().isOk())
							.andReturn();

		List<ErrorTrace> responseResult = new Gson().fromJson(result.getResponse().getContentAsString(),  new TypeToken<List<ErrorTrace>>(){}.getType());
		assertNotNull(responseResult);
		assertEquals(1, responseResult.size());
	}

}
