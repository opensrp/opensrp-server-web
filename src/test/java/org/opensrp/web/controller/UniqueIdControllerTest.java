package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
import org.opensrp.domain.IdentifierSource;
import org.opensrp.service.IdentifierSourceService;
import org.opensrp.service.OpenmrsIDService;
import org.opensrp.service.UniqueIdentifierService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
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

	@Mock
	private Authentication authentication;

	@Mock
	private ObjectMapper objectMapper;
	
	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/uniqueids";
	private final String ERROR_MESSAGE = "\"Sorry, an error occured when generating the qr code pdf\"";

	private String EXPECTED_IDENTIFIER = "{\n"
			+ "    \"identifiers\": [\n"
			+ "        \"AAAB-9\",\n"
			+ "        \"AAA1-6\",\n"
			+ "        \"AAA2-4\",\n"
			+ "        \"AABA-0\",\n"
			+ "        \"AABB-8\",\n"
			+ "        \"AAB1-5\",\n"
			+ "        \"AAB2-3\",\n"
			+ "        \"AA1A-7\",\n"
			+ "        \"AA1B-5\",\n"
			+ "        \"AA11-2\"\n"
			+ "    ]\n"
			+ "}";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(uniqueIdController)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		ReflectionTestUtils.setField(uniqueIdController, "qrCodesDir", "");

	}

	@Test
	public void testGetIdentifiersByIdSource() throws Exception {
		List<String> mocked_expected_ids = new ArrayList<>();
		mocked_expected_ids.add("AAAB-9");
		IdentifierSource identifierSource = createIdentifierSource();

		when(identifierSourceService.findByIdentifier(anyString())).thenReturn(identifierSource);
		when(uniqueIdentifierService.generateIdentifiers(any(IdentifierSource.class), any(int.class), anyString()))
				.thenReturn(mocked_expected_ids);
		when(objectMapper.writeValueAsString(any(Object.class))).thenReturn(EXPECTED_IDENTIFIER);

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("numberToGenerate", "10");
		req.addParameter("source", "10");

		ResponseEntity<String> stringResponseEntity = uniqueIdController.get(req, authentication);

		String responseString = stringResponseEntity.getBody();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(actualObj.get("identifiers").get(0).asText(), "AAAB-9");
		assertEquals(actualObj.get("identifiers").size(), 10);
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
	
	@Test
	public void testCheckIfRoleExistsReturnsTrueIfRoleExists(){
		List<String> roles = new ArrayList<>();
		roles.add("role-1");
		assertEquals(true, uniqueIdController.checkRoleIfRoleExits(roles,"role-1"));
	}
	
	@Test
	public void testCheckIfRoleExistsReturnsFalseIfRoleDoesNotExist(){
		List<String> roles = new ArrayList<>();
		roles.add("role-1");
		assertEquals(false, uniqueIdController.checkRoleIfRoleExits(roles,"role-2"));
	}

	private Authentication getMockedAuthentication() {
		Authentication authentication = new Authentication() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
