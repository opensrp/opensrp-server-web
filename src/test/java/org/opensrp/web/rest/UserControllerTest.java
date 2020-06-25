package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.api.domain.User;
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.domain.UserDetail;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.smartregister.domain.PhysicalLocation;
import org.opensrp.domain.Practitioner;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.controller.UserController;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.utils.TestData;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class UserControllerTest {
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	private MockMvc mockMvc;
	
	@Mock
	private Authentication authentication;
	
	@Mock
	private KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal;
	
	@Mock
	private RefreshableKeycloakSecurityContext securityContext;
	
	@Mock
	private AccessToken token;
	
	private UserController userController;
	
	@Mock
	private PractitionerService practitionerService;
	
	@Mock
	private OrganizationService organizationService;
	
	@Mock
	private PhysicalLocationService locationService;
	
	private List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
	
	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		userController = webApplicationContext.getBean(UserController.class);
		userController.setOrganizationService(organizationService);
		userController.setLocationService(locationService);
		userController.setPractitionerService(practitionerService);
		when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(securityContext);
		when(securityContext.getToken()).thenReturn(token);
		when(authentication.getAuthorities()).thenAnswer(a -> roles.stream().map(role -> new GrantedAuthority() {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			public String getAuthority() {
				return role;
			}
		}).collect(Collectors.toList()));
	}
	
	@Test
	public void testGetUserDetailsReturnsCorrectDetails() throws Exception {
		when(authentication.getPrincipal()).thenReturn(keycloakPrincipal);
		User user = new User(UUID.randomUUID().toString()).withRoles(roles).withUsername("test_user1");
		when(token.getPreferredUsername()).thenReturn(user.getUsername());
		when(authentication.getName()).thenReturn(user.getBaseEntityId());
		ResponseEntity<UserDetail> result = userController.getUserDetails(authentication);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		UserDetail userDetail = result.getBody();
		assertEquals(user.getUsername(), userDetail.getUserName());
		assertEquals(user.getBaseEntityId(), userDetail.getIdentifier());
		assertEquals(user.getRoles(), userDetail.getRoles());
		
	}
	
	@Test
	public void testGetUserDetailReturnsNull() throws Exception {
		ResponseEntity<UserDetail> result = userController.getUserDetails(authentication);
		assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
		UserDetail userDetail = result.getBody();
		assertNull(userDetail);
	}
	
	@Test
	@WithMockUser(username = "admin", roles = { "ADMIN" })
	public void testGetUserDetailsIntegrationTest() throws Exception {
		Pair<User, Authentication> authenticatedUser = TestData.getAuthentication(token, keycloakPrincipal);
		authentication=authenticatedUser.getSecond();
		MvcResult result = mockMvc
		        .perform(get("/user-details").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
		        .andExpect(status().isOk()).andReturn();
		UserDetail userDetail = new ObjectMapper().readValue(result.getResponse().getContentAsString(), UserDetail.class);
		
		User user=authenticatedUser.getFirst();
		assertEquals(user.getUsername(), userDetail.getUserName());
		assertEquals(user.getBaseEntityId(), userDetail.getIdentifier());
		assertEquals(user.getRoles(), userDetail.getRoles());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testAuthenticateIfUserIsNotMapped() throws Exception {
		when(authentication.getPrincipal()).thenReturn(keycloakPrincipal);
		when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(securityContext);
		when(securityContext.getToken()).thenReturn(token);
		Whitebox.setInternalState(userController, "useOpenSRPTeamModule", true);
		userController.authenticate(authentication);
	}
	
	@Test
	public void testAuthenticate() throws Exception {
		User user = new User(UUID.randomUUID().toString()).withRoles(roles).withUsername("test_user1");
		when(authentication.getPrincipal()).thenReturn(keycloakPrincipal);
		when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(securityContext);
		when(securityContext.getToken()).thenReturn(token);
		when(token.getPreferredUsername()).thenReturn(user.getUsername());
		when(authentication.getName()).thenReturn(user.getBaseEntityId());
		List<Long> ids = Collections.singletonList(12233l);

		when(organizationService.getOrganization(ids.get(0))).thenReturn(new Organization());
		
		Practitioner practitioner = new Practitioner();
		practitioner.setUserId(user.getBaseEntityId());
		when(practitionerService.getOrganizationsByUserId(user.getBaseEntityId()))
		        .thenReturn(new ImmutablePair<Practitioner, List<Long>>(practitioner, ids));
		String jurisdictionId = UUID.randomUUID().toString();
		List<AssignedLocations> assignedLocations = Collections
		        .singletonList(new AssignedLocations(jurisdictionId, UUID.randomUUID().toString()));
		when(organizationService.findAssignedLocationsAndPlans(ids)).thenReturn(assignedLocations);
		

		when(locationService.buildLocationHierachy(Collections.singleton(jurisdictionId))).thenReturn(new LocationTree());
		PhysicalLocation location = LocationResourceTest.createStructure();
		location.getProperties().getCustomProperties().put("OpenMRS_Id", "OpenMRS_Id1222");
		when(locationService.findLocationsByIds(false, Collections.singletonList(jurisdictionId)))
		        .thenReturn(Collections.singletonList(location));
		
		String[] locations = new String[5];
		locations[0] = "Test";
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("user", user);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("test", data);
		
		Whitebox.setInternalState(userController, "useOpenSRPTeamModule", true);
		ResponseEntity<String> result = userController.authenticate(authentication);
		String responseString = result.getBody();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = new ObjectMapper().readTree(responseString);
		assertEquals(result.getStatusCode(), HttpStatus.OK);
		assertEquals(actualObj.get("user").get("username").asText(), user.getUsername());
		assertTrue(actualObj.has("locations"));
		assertTrue(actualObj.has("team"));
		assertTrue(actualObj.has("time"));
		assertTrue(actualObj.has("jurisdictions"));
	}
}
