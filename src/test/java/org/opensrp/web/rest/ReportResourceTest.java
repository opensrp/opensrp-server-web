package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.domain.Report;
import org.opensrp.service.ReportService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = { "jedis", "postgres", "basic_auth" })
public class ReportResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/rest/report/";
	private final String expectedReport = "{\n"
		+ "\t\"reports\": \"[{\\\"baseEntityId\\\":\\\"22\\\",\\\"locationId\\\":\\\"testLocationId\\\",\\\"reportType\\\":\\\"testReportType\\\",\\\"formSubmissionId\\\":\\\"testFormSubmissionId\\\",\\\"providerId\\\":\\\"testProviderId\\\",\\\"status\\\":\\\"test\\\"}]\"\n"
		+ "}";
	private final String INVALID_JSON = "{\n"
			+ "\t\"reports\": {\n"
			+ "\t\t\"baseEntityId\": \"22\",\n"
			+ "\t\t\"locationId\": \"testLocationId\",\n"
			+ "\t\t\"reportType\": \"testReportType\",\n"
			+ "\t\t\"formSubmissionId\": \"testFormSubmissionId\",\n"
			+ "\t\t\"providerId\": \"testProviderId\",\n"
			+ "\t\t\"status\": \"test\"\n"
			+ "\t}\n"
			+ "}";
	
	@InjectMocks
	private ReportResource reportResource;

	@Mock
	private ReportService reportService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(reportResource)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
				.build();
	}

	@Test
	public void testSaveWithException() throws Exception {
		MvcResult result = mockMvc.perform(post(BASE_URL + "/add").content("".getBytes()))
				.andExpect(status().isBadRequest()).andReturn();
	}

	@Test
	public void testSave() throws Exception {
		when(reportService.addorUpdateReport(any(Report.class))).thenReturn(createReport());
		MvcResult result = mockMvc.perform(post(BASE_URL + "/add").content(expectedReport.getBytes()))
				.andExpect(status().isCreated()).andReturn();
		String responseString = result.getResponse().getContentAsString();
		assertEquals(responseString, "");
	}

	@Test
	public void testSaveWithInvalidJson() throws Exception {
		MvcResult result = mockMvc.perform(post(BASE_URL + "/add").content(INVALID_JSON.getBytes()))
				.andExpect(status().isInternalServerError()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		assertEquals(responseString, "");
	}
	
	private Report createReport() {
		Report report = new Report();
         report.setId("Test-ID");
         report.setLocationId("locationId");
         report.setProviderId("providerId");
         report.setStatus("test");
         report.setReportType("reportType");
         report.setFormSubmissionId("formSubmissionId");
         return report;
	}
	
}
