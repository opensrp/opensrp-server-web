package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.OpenmrsIDService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class XlsDataImportControllerTest {
	@Mock
	ClientService clientService;
	
	@Mock
	EventService eventService;
	
	@Mock
	OpenmrsIDService openmrsIDService;

	@InjectMocks
	private XlsDataImportController xlsDataImportController;

	@Mock
	private Authentication authentication;

	private MockMvc mockMvc;

	private final String allowedMimeTypes = "application/octet-stream,image/jpeg,image/gif,image/png";
	
	@Before
	public void setUp() {
		initMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(xlsDataImportController).
				addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		ReflectionTestUtils.setField(xlsDataImportController, "allowedMimeTypes", allowedMimeTypes);
	}

	@Test
	public void shouldCreateClientsFromCSVFile() throws IOException, SQLException, JSONException {
		File csvFile = new File("src/test/java/org/opensrp/fixtures/csv_to_import.csv");
		FileInputStream fileInputStream = new FileInputStream(csvFile);
		MockMultipartFile file = new MockMultipartFile("file", "originalFileName", "image/png", IOUtils.toByteArray(fileInputStream));
		List<String> openmrsIds = new ArrayList<String>();
		openmrsIds.add("12345-1");
		openmrsIds.add("12345-2");
		//mothers' id
		openmrsIds.add("12345-3");
		openmrsIds.add("12345-4");

		when(openmrsIDService.downloadOpenmrsIds(openmrsIds.size())).thenReturn(openmrsIds);

		ResponseEntity<String> response = xlsDataImportController.importXlsData(file, authentication);
		String responseBody = response.getBody();
		JSONObject responseJson = new JSONObject(responseBody);
		
		int summaryClientCount = responseJson.getInt("summary_client_count");
		int summaryEventCount = responseJson.getInt("summary_event_count");
		
		assertEquals(summaryClientCount, 4);
		assertEquals(summaryEventCount, 28);
		verify(clientService, times(4)).addorUpdate(any(Client.class));
		verify(eventService, times(28)).addEvent(any(Event.class),anyString());
	}
}
