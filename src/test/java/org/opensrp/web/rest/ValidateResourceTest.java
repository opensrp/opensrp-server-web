package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
public class ValidateResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private ClientService clientService;

	@Mock
	private EventService eventService;

	@InjectMocks
	private ValidateResource validateResource;

	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/rest/validate/";

	private String INVALID_JSON = "{\n"
			+ "  \"client\" : {\n"
			+ "  \"firstName\" : \"Test\" \n"
			+ "  }\n"
			+ "}";
	
	private String SYNC_REQUEST_PAYLOAD = "{\n"
			+ "\t\"clients\": [ 1 , 2 ],\n"
			+ "\t\"events\": [ 1 , 2]\n"
			+ "}";
	
	private String SYNC_REQUEST_STRING_PAYLOAD = "{\n"
			+ "\t\"clients\": \"[ 1 , 2 ]\",\n"
			+ "\t\"events\": \"[ 1 , 2]\"\n"
			+ "}";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(validateResource)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
				.build();
	}

	@Test
	public void testValidateSyncWithBlankData() throws Exception {
		when(clientService.getByBaseEntityId(any(String.class))).thenReturn(createClient());
		when(eventService.findByFormSubmissionId(any(String.class))).thenReturn(createEvent());
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content("".getBytes()))
				.andExpect(status().isBadRequest()).andReturn();
	}

	@Test
	public void testValidateSyncWithWrongData() throws Exception {
		when(clientService.getByBaseEntityId(any(String.class))).thenReturn(createClient());
		when(eventService.findByFormSubmissionId(any(String.class))).thenReturn(createEvent());
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content(INVALID_JSON.getBytes()))
				.andExpect(status().isBadRequest()).andReturn();
	}

	@Test
	public void testValidateSync() throws Exception {
		String expected = "{\"clients\":[\"1\",\"2\"],\"events\":[\"1\",\"2\"]}";
		when(clientService.getByBaseEntityId(any(String.class))).thenReturn(null);
		when(eventService.findByFormSubmissionId(any(String.class))).thenReturn(null);
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content(SYNC_REQUEST_PAYLOAD.getBytes()))
				.andExpect(status().isOk()).andReturn();
		
		assertEquals(result.getResponse().getContentAsString(), expected);
	}
	
	@Test
	public void testValidateSyncWithStringArray() throws Exception {
		String expected = "{\"clients\":[\"1\",\"2\"],\"events\":[\"1\",\"2\"]}";
		when(clientService.getByBaseEntityId(any(String.class))).thenReturn(null);
		when(eventService.findByFormSubmissionId(any(String.class))).thenReturn(null);
		MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
				.content(SYNC_REQUEST_STRING_PAYLOAD.getBytes()))
				.andExpect(status().isOk()).andReturn();
		
		assertEquals(result.getResponse().getContentAsString(), expected);
	}

	private Client createClient() {
		Client client = new Client("Base-entity-id");
		client.setFirstName("test");
		client.setLastName("user");
		return client;
	}

	private Event createEvent() {
		Event event = new Event();
		event.setBaseEntityId("Base-entity-id");
		event.setId("ID-123");
		return event;
	}


}
