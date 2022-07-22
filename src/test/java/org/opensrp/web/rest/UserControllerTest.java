package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.representations.AccessToken;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.api.domain.User;
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.domain.UserDetail;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.controller.UserController;
import org.opensrp.web.exceptions.MissingTeamAssignmentException;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.utils.TestData;
import org.powermock.reflect.Whitebox;
import org.smartregister.domain.Jurisdiction;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.PlanDefinition;
import org.smartregister.domain.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class UserControllerTest {

    private final List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Value("#{opensrp['keycloak.configuration.endpoint']}")
    protected String keycloakConfigurationURL;
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
    @Mock
    private PlanService planService;
    @Autowired
    private KeycloakRestTemplate restTemplate;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
                .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
        userController = webApplicationContext.getBean(UserController.class);
        userController.setOrganizationService(organizationService);
        userController.setLocationService(locationService);
        userController.setPractitionerService(practitionerService);
        userController.setPlanService(planService);
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
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetUserDetailsIntegrationTest() throws Exception {
        Pair<User, Authentication> authenticatedUser = TestData.getAuthentication(token, keycloakPrincipal, securityContext);
        authentication = authenticatedUser.getSecond();
        MvcResult result = mockMvc
                .perform(get("/user-details").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk()).andReturn();
        UserDetail userDetail = new ObjectMapper().readValue(result.getResponse().getContentAsString(), UserDetail.class);

        User user = authenticatedUser.getFirst();
        assertEquals(user.getUsername(), userDetail.getUserName());
        assertEquals(user.getBaseEntityId(), userDetail.getIdentifier());
        assertEquals(user.getRoles(), userDetail.getRoles());
    }

    @Test(expected = MissingTeamAssignmentException.class)
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
        String planId = UUID.randomUUID().toString();
        List<AssignedLocations> assignedLocations = Collections
                .singletonList(new AssignedLocations(jurisdictionId, planId));
        PlanDefinition plan = new PlanDefinition();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setCode(jurisdictionId);
        plan.setJurisdiction(Collections.singletonList(jurisdiction));
        plan.setStatus(PlanDefinition.PlanStatus.ACTIVE);

        List<PlanDefinition> planDefinitions = Collections.singletonList(plan);
        when(organizationService.findAssignedLocationsAndPlans(ids)).thenReturn(assignedLocations);
        when(planService.getPlansByIdsReturnOptionalFields(
                Collections.singletonList(planId),
                Arrays.asList(UserController.JURISDICTION, UserController.STATUS), false)
        ).thenReturn(planDefinitions);

        PhysicalLocation location = LocationResourceTest.createStructure();
        location.getProperties().setName("OA123");
        location.setId(jurisdictionId);
        when(locationService.findLocationByIdsWithChildren(false, Collections.singleton(jurisdictionId), Integer.MAX_VALUE))
                .thenReturn(Collections.singletonList(location));
        when(locationService.buildLocationHierachy(Collections.singleton(location.getId()), false, true))
                .thenReturn(new LocationTree());

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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualObj = objectMapper.readTree(responseString);
        assertEquals(result.getStatusCode(), HttpStatus.OK);
        assertEquals(actualObj.get("user").get("username").asText(), user.getUsername());
        assertTrue(actualObj.has("locations"));
        assertTrue(actualObj.has("team"));
        assertTrue(actualObj.has("time"));
        assertTrue(actualObj.has("jurisdictions"));
        assertTrue(actualObj.has("jurisdictionIds"));
        assertEquals(objectMapper.writeValueAsString(new String[]{location.getProperties().getName()}),
                actualObj.get("jurisdictions").toString());
        assertEquals(objectMapper.writeValueAsString(new String[]{location.getId()}),
                actualObj.get("jurisdictionIds").toString());
    }


    @Test
    public void testAuthenticateWhenAssignedLocationIsNotOperationArea() throws Exception {
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
        String planId = UUID.randomUUID().toString();
        String secondJurisdiction = UUID.randomUUID().toString();
        List<AssignedLocations> assignedLocations = Collections
                .singletonList(new AssignedLocations(jurisdictionId, planId));

        PlanDefinition plan = new PlanDefinition();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setCode(secondJurisdiction);
        plan.setJurisdiction(Collections.singletonList(jurisdiction));
        plan.setStatus(PlanDefinition.PlanStatus.ACTIVE);

        List<PlanDefinition> planDefinitions = Collections.singletonList(plan);
        when(organizationService.findAssignedLocationsAndPlans(ids)).thenReturn(assignedLocations);
        when(planService.getPlansByIdsReturnOptionalFields(
                Collections.singletonList(planId),
                Arrays.asList(UserController.JURISDICTION, UserController.STATUS), false)
        ).thenReturn(planDefinitions);

        PhysicalLocation location = LocationResourceTest.createStructure();
        location.getProperties().setName("OA123");
        location.setId(jurisdictionId);

        PhysicalLocation location2 = LocationResourceTest.createStructure();
        location2.getProperties().setName("Other");
        location2.setId(secondJurisdiction);

        when(locationService.findLocationByIdsWithChildren(false, Collections.singleton(jurisdictionId), Integer.MAX_VALUE))
                .thenReturn(Arrays.asList(location, location2));
        when(locationService.buildLocationHierachy(Collections.singleton(location.getId()), false, true))
                .thenReturn(new LocationTree());

        when(locationService.findLocationByIdsWithChildren(false, Collections.singleton(secondJurisdiction), Integer.MAX_VALUE))
                .thenReturn(Collections.singletonList(location2));

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
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualObj = objectMapper.readTree(responseString);
        assertEquals(result.getStatusCode(), HttpStatus.OK);

        assertTrue(actualObj.has("team"));
        assertEquals("Other", actualObj.get("team").get("team").get("location").get("name").asText());
        assertEquals(secondJurisdiction, actualObj.get("team").get("team").get("location").get("uuid").asText());

        assertEquals(objectMapper.writeValueAsString(new String[]{location2.getProperties().getName()}),
                actualObj.get("jurisdictions").toString());
        assertEquals(objectMapper.writeValueAsString(new String[]{location2.getId()}),
                actualObj.get("jurisdictionIds").toString());
    }

    @Test
    public void testLogoutShouldLogoutOpenSRPSessionAndKeycloak() throws Exception {
        Pair<User, Authentication> authenticatedUser = TestData.getAuthentication(token, keycloakPrincipal, securityContext);
        authentication = authenticatedUser.getSecond();
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("User Logged out", HttpStatus.OK);
        Whitebox.setInternalState(userController, "restTemplate", restTemplate);
        MvcResult result = mockMvc
                .perform(get("/logout.do").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk()).andReturn();
        assertEquals(expectedResponse.getStatusCodeValue(), result.getResponse().getStatus());
        assertEquals(expectedResponse.getBody(), result.getResponse().getContentAsString());
        MockHttpServletRequest request = result.getRequest();
        assertNull(request.getUserPrincipal());
        assertNull(request.getRemoteUser());
        assertNull(request.getAuthType());

    }

    @Test
    public void testLogoutShouldReturnUnAuthorizedIfNotLoggedIn() throws Exception {
        ResponseEntity<String> expectedResponse = new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        Whitebox.setInternalState(userController, "restTemplate", restTemplate);
        MvcResult result = mockMvc
                .perform(get("/logout.do").with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isUnauthorized()).andReturn();
        assertEquals(expectedResponse.getStatusCodeValue(), result.getResponse().getStatus());
        assertEquals(expectedResponse.getBody(), result.getResponse().getContentAsString());
        MockHttpServletRequest request = result.getRequest();
        assertNull(request.getUserPrincipal());
        assertNull(request.getRemoteUser());
        assertNull(request.getAuthType());

    }
}
