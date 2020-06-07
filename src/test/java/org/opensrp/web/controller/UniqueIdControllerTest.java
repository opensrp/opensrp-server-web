package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.api.domain.User;
import org.opensrp.domain.IdentifierSource;
import org.opensrp.service.IdentifierSourceService;
import org.opensrp.service.OpenmrsIDService;
import org.opensrp.service.UniqueIdentifierService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.nullable;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })

public class UniqueIdControllerTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@InjectMocks
	private UniqueIdController uniqueIdController;

	@Mock
	private OpenmrsIDService openmrsIdService;

	@Mock
	private IdentifierSourceService identifierSourceService;

	@Mock
	private UniqueIdentifierService uniqueIdentifierService;
	
	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/uniqueids";
	private final String ERROR_MESSAGE = "\"Sorry, an error occured when generating the qr code pdf\"";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(uniqueIdController)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		ReflectionTestUtils.setField(uniqueIdController, "qrCodesDir", "");

	}

	@Test
	public void testGetOpenMRSIdentifiers() throws Exception {
		List<String> mocked_expected_ids = new ArrayList<>();
		mocked_expected_ids.add("1");
		Authentication authentication = mock(Authentication.class);
		authentication.setAuthenticated(Boolean.TRUE);
		User user = new User("Base-entity-id");
		user.setUsername("admin");
		user.setPassword("admin");
		SecurityContext securityContext = mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);

		when(identifierSourceService.findByIdentifier(anyString())).thenReturn(null);
		when(openmrsIdService
				.getOpenMRSIdentifiers(any(String.class), any(String.class), nullable(String.class), nullable(String.class)))
				.thenReturn(mocked_expected_ids);
		when(securityContext.getAuthentication()).thenReturn(spy(getMockedAuthentication()));
		when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/get").param("numberToGenerate", "10")
				.param("source", "test"))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);

		assertEquals(actualObj.get("identifiers").get(0).asText(), "1");
		assertEquals(actualObj.get("identifiers").size(), 1);
	}

	@Test
	public void testGetIdentifiersByIdSource() throws Exception {
		List<String> mocked_expected_ids = new ArrayList<>();
		mocked_expected_ids.add("BA21-4");
		IdentifierSource identifierSource = createIdentifierSource();

		when(identifierSourceService.findByIdentifier(anyString())).thenReturn(identifierSource);
		when(uniqueIdentifierService.generateIdentifiers(any(IdentifierSource.class),any(int.class),anyString()))
				.thenReturn(mocked_expected_ids);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/get").param("numberToGenerate", "10")
				.param("source", "test")
		.param("usedBy", "admin"))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);

		assertEquals(actualObj.get("identifiers").get(0).asText(), "BA21-4");
		assertEquals(actualObj.get("identifiers").size(), 1);
	}

	@Test
	public void testThisMonthDataSendTODHIS2ThrowsException() throws Exception {
		List<String> mocked_expected_ids = new ArrayList<>();
		mocked_expected_ids.add("1");
		Authentication authentication = mock(Authentication.class);
		authentication.setAuthenticated(Boolean.TRUE);
		SecurityContext securityContext = mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());
		when(openmrsIdService.getNotUsedIdsAsString(1)).thenReturn(mocked_expected_ids);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/print").param("batchSize", "10"))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		assertEquals(responseString,ERROR_MESSAGE);
	}

	private Authentication getMockedAuthentication() {
		Authentication authentication = new Authentication() {

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return "";
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return "Test User";
			}

			@Override
			public boolean isAuthenticated() {
				return false;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

			}

			@Override
			public String getName() {
				return "admin";
			}
		};

		return authentication;
	}

	private IdentifierSource createIdentifierSource() {
		IdentifierSource identifierSource = new IdentifierSource();
		identifierSource.setId(1l);
		identifierSource.setMinLength(4);
		identifierSource.setMaxLength(4);
		identifierSource.setBaseCharacterSet("AB12");
		identifierSource.setIdentifier("testIdentifier");
		return identifierSource;
	}
}
