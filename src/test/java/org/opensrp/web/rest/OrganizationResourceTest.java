package org.opensrp.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.api.domain.User;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.search.OrganizationSearchBean;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.bean.OrganizationAssigmentBean;
import org.opensrp.web.bean.UserAssignmentBean;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.controller.UserController;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.utils.TestData;
import org.smartregister.domain.*;
import org.smartregister.domain.PlanDefinition.PlanStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Samuel Githengi created on 09/17/19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class OrganizationResourceTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private OrganizationService organizationService;

	@Mock
	private PractitionerService practitionerService;

	@Mock
	private PhysicalLocationService locationService;

	@Mock
	private PlanService planService;

	@Mock
	private KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal;

	@Mock
	private RefreshableKeycloakSecurityContext securityContext;

	@Mock
	private AccessToken token;

	@Captor
	private ArgumentCaptor<Organization> organizationArgumentCaptor;

	private OrganizationResource organizationResource;

	private String BASE_URL = "/rest/organization/";

	private String organizationJSON = "{\"identifier\":\"801874c0-d963-11e9-8a34-2a2ae2dbcce4\",\"active\":true,\"name\":\"B Team\",\"partOf\":1123,\"type\":{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/organization-type\",\"code\":\"team\",\"display\":\"Team\"}]},\"dateCreated\":\"2021-12-20T13:16:02.457Z\",\"dateEdited\":\"2021-12-20T13:16:02.457Z\",\"serverVersion\":1}";

	private ObjectMapper objectMapper;

	private String MESSAGE = "The server encountered an error processing the request.";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity())
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		organizationResource = webApplicationContext.getBean(OrganizationResource.class);
		organizationResource.setOrganizationService(organizationService);
		organizationResource.setPractitionerService(practitionerService);
		organizationResource.setLocationService(locationService);
		organizationResource.setPlanService(planService);
		objectMapper = new ObjectMapper();
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.registerModule(new JodaModule());
	}

	@Test
	public void testGetAllOrganizations() throws Exception {
		List<Organization> expected = Collections.singletonList(getOrganization());
		when(organizationService.getAllOrganizations(any(OrganizationSearchBean.class))).thenReturn(expected);
		MvcResult result = mockMvc.perform(get(BASE_URL)).andExpect(status().isOk()).andReturn();
		verify(organizationService).getAllOrganizations(any(OrganizationSearchBean.class));
		verifyNoMoreInteractions(organizationService);
		assertEquals("[" + organizationJSON + "]", result.getResponse().getContentAsString());

	}

	@Test
	public void testGetAllOrganizationsUnderLocation() throws Exception {
		List<Organization> expected = Collections.singletonList(getOrganization());
		when(organizationService.getAllOrganizations(any(OrganizationSearchBean.class))).thenReturn(expected);
		mockMvc.perform(get(BASE_URL).param("location_id", "12345")).andExpect(status().isOk()).andReturn();
		verify(organizationService).selectOrganizationsEncompassLocations("12345");
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testGetOrganizationByIdentifier() throws Exception {
		Organization expected = getOrganization();
		when(organizationService.getOrganization(expected.getIdentifier())).thenReturn(expected);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/{identifier}", expected.getIdentifier()))
				.andExpect(status().isOk()).andReturn();
		verify(organizationService).getOrganization(expected.getIdentifier());
		verifyNoMoreInteractions(organizationService);
		assertEquals(organizationJSON, result.getResponse().getContentAsString());

	}

	@Test
	public void testCreateOrganization() throws Exception {

		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(organizationJSON.getBytes()))
				.andExpect(status().isCreated());
		verify(organizationService).addOrganization(organizationArgumentCaptor.capture());
		assertEquals(organizationJSON, objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.writeValueAsString(organizationArgumentCaptor.getValue()));
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testCreateOrganizationWithoutIdentifier() throws Exception {
		doThrow(IllegalArgumentException.class).when(organizationService).addOrganization(any(Organization.class));
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(organizationJSON.getBytes()))
				.andExpect(status().isBadRequest());
		verify(organizationService).addOrganization(organizationArgumentCaptor.capture());
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testCreateOrganizationWithError() throws Exception {
		doThrow(RuntimeException.class).when(organizationService).addOrganization(any(Organization.class));
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(organizationJSON.getBytes()))
				.andExpect(status().isInternalServerError());
		verify(organizationService).addOrganization(organizationArgumentCaptor.capture());
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testUpdateOrganization() throws Exception {

		mockMvc.perform(put(BASE_URL + "/{identifier}", getOrganization().getIdentifier())
				.contentType(MediaType.APPLICATION_JSON).content(organizationJSON.getBytes()))
				.andExpect(status().isCreated());
		verify(organizationService).updateOrganization(organizationArgumentCaptor.capture());
		assertEquals(organizationJSON, objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.writeValueAsString(organizationArgumentCaptor.getValue()));
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testUpdateOrganizationWithoutIdentifier() throws Exception {
		doThrow(new IllegalArgumentException()).when(organizationService).updateOrganization(any(Organization.class));
		mockMvc.perform(put(BASE_URL + "/{identifier}", getOrganization().getIdentifier())
				.contentType(MediaType.APPLICATION_JSON).content(organizationJSON.getBytes()))
				.andExpect(status().isBadRequest());
		verify(organizationService).updateOrganization(organizationArgumentCaptor.capture());
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testUpdateOrganizationWithError() throws Exception {
		doThrow(new RuntimeException()).when(organizationService).updateOrganization(any(Organization.class));
		mockMvc.perform(put(BASE_URL + "/{identifier}", getOrganization().getIdentifier())
				.contentType(MediaType.APPLICATION_JSON).content(organizationJSON.getBytes()))
				.andExpect(status().isInternalServerError());
		verify(organizationService).updateOrganization(organizationArgumentCaptor.capture());
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testAssignLocationAndPlan() throws Exception {
		mockMvc.perform(post(BASE_URL + "/assignLocationsAndPlans").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(getOrganizationAssignment()))).andExpect(status().isOk());
		for (OrganizationAssigmentBean bean : getOrganizationAssignment())
			verify(organizationService).assignLocationAndPlan(bean.getOrganization(), bean.getJurisdiction(), bean.getPlan(),
					bean.getFromDate(), bean.getToDate());
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testAssignLocationAndPlanWithMissingParams() throws Exception {
		doThrow(new IllegalArgumentException()).when(organizationService).assignLocationAndPlan(null, null, null, null,
				null);
		OrganizationAssigmentBean[] beans = new OrganizationAssigmentBean[] { new OrganizationAssigmentBean() };
		mockMvc.perform(post(BASE_URL + "/assignLocationsAndPlans").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(beans))).andExpect(status().isBadRequest());
		for (OrganizationAssigmentBean bean : beans)
			verify(organizationService).assignLocationAndPlan(bean.getOrganization(), bean.getJurisdiction(), bean.getPlan(),
					bean.getFromDate(), bean.getToDate());
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testAssignLocationAndPlanWithInternalError() throws Exception {
		doThrow(new RuntimeException()).when(organizationService).assignLocationAndPlan(null, null, null, null, null);
		OrganizationAssigmentBean[] beans = new OrganizationAssigmentBean[] { new OrganizationAssigmentBean() };
		mockMvc.perform(post(BASE_URL + "/assignLocationsAndPlans").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsBytes(beans))).andExpect(status().isInternalServerError());
		for (OrganizationAssigmentBean bean : beans)
			verify(organizationService).assignLocationAndPlan(bean.getOrganization(), bean.getJurisdiction(), bean.getPlan(),
					bean.getFromDate(), bean.getToDate());
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testGetAssignedLocationAndPlan() throws Exception {
		objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String identifier = UUID.randomUUID().toString();
		List<AssignedLocations> expected = getOrganizationLocationsAssigned(true);
		when(organizationService.findAssignedLocationsAndPlans(identifier, true, null, null, null, null))
				.thenReturn(expected);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/assignedLocationsAndPlans/{identifier}", identifier))
				.andExpect(status().isOk()).andReturn();

		verify(organizationService).findAssignedLocationsAndPlans(identifier, true, null, null, null, null);
		verifyNoMoreInteractions(organizationService);
		assertEquals(objectMapper.writeValueAsString(expected), result.getResponse().getContentAsString());

	}

	@Test
	public void testGetAssignedLocationAndPlanWithMissingParams() throws Exception {
		String identifier = UUID.randomUUID().toString();
		doThrow(new IllegalArgumentException()).when(organizationService).findAssignedLocationsAndPlans(identifier, true,
				null, null, null, null);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/assignedLocationsAndPlans/{identifier}", identifier))
				.andExpect(status().isBadRequest()).andReturn();

		verify(organizationService).findAssignedLocationsAndPlans(identifier, true, null, null, null, null);
		verifyNoMoreInteractions(organizationService);
		assertEquals("", result.getResponse().getContentAsString());

	}

	@Test
	public void testGetAssignedLocationAndPlanWithInternalError() throws Exception {
		String identifier = UUID.randomUUID().toString();
		doThrow(new RuntimeException()).when(organizationService).findAssignedLocationsAndPlans(identifier, true, null, null,
				null, null);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/assignedLocationsAndPlans/{identifier}", identifier))
				.andExpect(status().isInternalServerError()).andReturn();

		verify(organizationService).findAssignedLocationsAndPlans(identifier, true, null, null, null, null);
		verifyNoMoreInteractions(organizationService);
		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = objectMapper.readTree(responseString);
		assertEquals(actualObj.get("message").asText(), MESSAGE);

	}

	@Test
	public void testGetAssignedLocationsAndPlansByPlanId() throws Exception {
		objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		String identifier = UUID.randomUUID().toString();
		List<AssignedLocations> expected = getOrganizationLocationsAssigned(true);
		when(organizationService.findAssignedLocationsAndPlansByPlanIdentifier(identifier, null, null, null, null))
				.thenReturn(expected);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/assignedLocationsAndPlans?plan=" + identifier))
				.andExpect(status().isOk()).andReturn();

		verify(organizationService).findAssignedLocationsAndPlansByPlanIdentifier(identifier, null, null, null, null);
		verifyNoMoreInteractions(organizationService);
		assertEquals(objectMapper.writeValueAsString(expected), result.getResponse().getContentAsString());
	}

	@Test
	public void testGetOrgPractitioners() throws Exception {
		String identifier = UUID.randomUUID().toString();
		List<Practitioner> expected = new ArrayList<>();
		expected.add(getPractioner());
		when(practitionerService.getPractitionersByOrgIdentifier(any(String.class))).thenReturn(expected);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/practitioner/{identifier}", identifier))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = objectMapper.readTree(responseString);
		assertEquals(actualObj.get(0).get("identifier").asText(), expected.get(0).getIdentifier());
		assertEquals(actualObj.get(0).get("active").asBoolean(), expected.get(0).getActive());
		assertEquals(actualObj.size(), expected.size());
	}

	@Test
	public void testGetOrgPractitionersWithInternalError() throws Exception {
		String identifier = UUID.randomUUID().toString();
		doThrow(new RuntimeException()).when(practitionerService).getPractitionersByOrgIdentifier(any(String.class));

		MvcResult result = mockMvc.perform(get(BASE_URL + "/practitioner/{identifier}", identifier))
				.andExpect(status().isInternalServerError()).andReturn();

		verify(practitionerService).getPractitionersByOrgIdentifier(identifier);
		verifyNoMoreInteractions(practitionerService);
		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = objectMapper.readTree(responseString);
		assertEquals(actualObj.get("message").asText(), MESSAGE);
	}

	@Test
	public void testgetUserAssignedLocationsAndPlansWithoutPlansShouldReturnUserAssignment() throws Exception {
		Pair<User, Authentication> authenticatedUser = TestData.getAuthentication(token, keycloakPrincipal, securityContext);
		List<Long> ids = Arrays.asList(123l, 124l);
		when(practitionerService.getOrganizationsByUserId(authenticatedUser.getFirst().getBaseEntityId()))
				.thenReturn(new ImmutablePair<>(getPractioner(), ids));
		List<AssignedLocations> assignments = getOrganizationLocationsAssigned(false);
		when(organizationService.findAssignedLocationsAndPlans(ids)).thenReturn(assignments);
		PhysicalLocation location = LocationResourceTest.createStructure();
		location.getProperties().setName("Vilage123");
		PhysicalLocation location2 = new PhysicalLocation();
		location2.setId(UUID.randomUUID().toString());
		location2.setProperties(new LocationProperty());
		location2.getProperties().setName("OA1");
		location2.getProperties().setParentId(location.getId());

		PhysicalLocation location3 = new PhysicalLocation();
		location3.setId(UUID.randomUUID().toString());
		location3.setProperties(new LocationProperty());
		location3.getProperties().setName("Oa3");
		location3.getProperties().setParentId(location.getId());
		when(locationService.findLocationByIdsWithChildren(eq(false), any(), eq(Integer.MAX_VALUE)))
				.thenReturn(Arrays.asList(location, location2, location3));

		Authentication authentication = authenticatedUser.getSecond();
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/user-assignment")
						.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isOk()).andReturn();

		UserAssignmentBean userAssignment = objectMapper.readValue(result.getResponse().getContentAsString(),
				UserAssignmentBean.class);

		assertEquals(new HashSet<>(ids), userAssignment.getOrganizationIds());
		assertEquals(2, userAssignment.getJurisdictions().size());
		assertEquals(0, userAssignment.getPlans().size());
		for (AssignedLocations assignment : assignments) {
			assertFalse(userAssignment.getJurisdictions().contains(location.getId()));
			assertTrue(userAssignment.getJurisdictions().contains(location2.getId()));
			assertTrue(userAssignment.getJurisdictions().contains(location3.getId()));
			assertFalse(userAssignment.getPlans().contains(assignment.getPlanId()));
		}
		verifyNoInteractions(planService);

	}

	@Test
	public void testGetUserAssignedLocationsAndPlansWithPlansShouldReturnActivePlansOnly() throws Exception {
		Pair<User, Authentication> authenticatedUser = TestData.getAuthentication(token, keycloakPrincipal, securityContext);
		List<Long> ids = Arrays.asList(123l, 124l);

		when(practitionerService.getOrganizationsByUserId(authenticatedUser.getFirst().getBaseEntityId()))
				.thenReturn(new ImmutablePair<>(getPractioner(), ids));
		List<AssignedLocations> assignments = getOrganizationLocationsAssigned(true);
		when(organizationService.findAssignedLocationsAndPlans(ids)).thenReturn(assignments);

		List<String> planIds = assignments.stream().map(a -> a.getPlanId()).collect(Collectors.toList());
		List<PlanDefinition> plans = getPlans(assignments);
		when(planService.getPlansByIdsReturnOptionalFields(any(), any(), eq(false))).thenReturn(plans);

		PhysicalLocation location = LocationResourceTest.createStructure();
		location.getProperties().setName("Vilage123");
		PhysicalLocation location2 = new PhysicalLocation();
		location2.setId(UUID.randomUUID().toString());
		location2.setProperties(new LocationProperty());
		location2.getProperties().setName("OA1");
		location2.getProperties().setParentId(location.getId());

		PhysicalLocation location3 = new PhysicalLocation();
		location3.setId(UUID.randomUUID().toString());
		location3.setProperties(new LocationProperty());
		location3.getProperties().setName("Oa3");
		location3.getProperties().setParentId(location.getId());
		when(locationService.findLocationByIdsWithChildren(eq(false), any(), eq(Integer.MAX_VALUE)))
				.thenReturn(Arrays.asList(location, location2, location3));

		Authentication authentication = authenticatedUser.getSecond();
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/user-assignment")
						.with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
				.andExpect(status().isOk()).andReturn();

		UserAssignmentBean userAssignment = objectMapper.readValue(result.getResponse().getContentAsString(),
				UserAssignmentBean.class);

		assertEquals(new HashSet<>(ids), userAssignment.getOrganizationIds());
		assertEquals(2, userAssignment.getJurisdictions().size());

		Set<String> activePlans = plans.stream()
				.filter(p -> PlanStatus.ACTIVE.equals(p.getStatus())
						&& (p.getEffectivePeriod().getEnd().isAfterNow()
						|| p.getEffectivePeriod().getEnd().isEqualNow()))
				.map(p -> p.getIdentifier())
				.collect(Collectors.toSet());

		assertEquals(activePlans.size(), userAssignment.getPlans().size());
		assertTrue(userAssignment.getPlans().containsAll(activePlans));
		Set<String> inActivePlans = new HashSet<>(planIds);
		inActivePlans.removeAll(activePlans);

		verify(planService).getPlansByIdsReturnOptionalFields(ArgumentMatchers.argThat(arg -> arg.containsAll(planIds)),
				eq(Arrays.asList(UserController.JURISDICTION, UserController.STATUS)), eq(false));

	}

	private Organization getOrganization() throws Exception {
		return objectMapper.readValue(organizationJSON, Organization.class);
	}

	private OrganizationAssigmentBean[] getOrganizationAssignment() {
		List<OrganizationAssigmentBean> organizationAssigmentBeans = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < 5; i++) {
			OrganizationAssigmentBean bean = new OrganizationAssigmentBean();
			bean.setOrganization("org" + i);
			bean.setJurisdiction("loc" + i);
			bean.setPlan("plan" + i);
			if (random.nextBoolean())
				bean.setFromDate(new Date());
			if (random.nextBoolean())
				bean.setToDate(new Date());

		}
		return organizationAssigmentBeans.toArray(new OrganizationAssigmentBean[] {});

	}

	private List<AssignedLocations> getOrganizationLocationsAssigned(boolean includePlans) {
		List<AssignedLocations> organizationAssignmentBeans = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < 5; i++) {
			AssignedLocations bean = new AssignedLocations();
			bean.setOrganizationId("org" + i);
			bean.setJurisdictionId("loc" + i);
			if (includePlans) {
				bean.setPlanId("plan" + i);
			}
			if (random.nextBoolean())
				bean.setFromDate(new Date());
			if (random.nextBoolean())
				bean.setToDate(new Date());
			organizationAssignmentBeans.add(bean);

		}
		return organizationAssignmentBeans;

	}

	private Practitioner getPractioner() {
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("ID-123");
		practitioner.setActive(Boolean.TRUE);
		return practitioner;

	}

	private List<PlanDefinition> getPlans(List<AssignedLocations> assignedLocations) {
		Random random = new Random();
		return assignedLocations.stream().map(al -> {
			PlanDefinition plan = new PlanDefinition();
			plan.setIdentifier(al.getPlanId());
			plan.setJurisdiction(Collections.singletonList(new Jurisdiction(al.getJurisdictionId())));
			plan.setEffectivePeriod(new Period(DateTime.now(), DateTime.now().plusDays(2)));
			plan.setStatus(random.nextBoolean() ? PlanStatus.ACTIVE : PlanStatus.COMPLETED);
			return plan;
		}).collect(Collectors.toList());

	}

	@Test
	public void testGetSearchOrganizationWithParams() throws Exception {
		List<Organization> expected = new ArrayList<>();
		expected.add(createSearchOrganization());
		when(organizationService.getSearchOrganizations((OrganizationSearchBean) any())).thenReturn(expected);
		when(organizationService.findOrganizationCount((OrganizationSearchBean) any())).thenReturn(1);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "search/").param("name", "C Team").param("orderByFieldName", "id")
						.param("pageNumber", "1").param("pageSize", "10").param("orderByType", "ASC"))
				.andExpect(status().isOk()).andReturn();

		verify(organizationService).getSearchOrganizations((OrganizationSearchBean) any());
		verify(organizationService).findOrganizationCount((OrganizationSearchBean) any());
		verifyNoMoreInteractions(organizationService);
		assertEquals(objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(expected),
				result.getResponse().getContentAsString());
	}

	private Organization createSearchOrganization() throws JsonMappingException, JsonProcessingException {
		String searchResponseJson = "{\"id\":3,\"identifier\":\"801874c0-d963-11e9-8a34-2a2ae2dbcce5\",\"active\":false,\"name\":\"C Team\",\"partOf\":2,\"memberCount\":2}";

		Organization searchOrganization = objectMapper.readValue(searchResponseJson, Organization.class);

		return searchOrganization;
	}

	@Test
	public void testCreateOrganizationInBatch() throws Exception {

		String organizations = "[" + organizationJSON + "]";
		mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON).content(organizations))
				.andExpect(status().isCreated());
		verify(organizationService).addOrUpdateOrganization(organizationArgumentCaptor.capture());
		assertEquals(organizationJSON, objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.writeValueAsString(organizationArgumentCaptor.getValue()));
		verifyNoMoreInteractions(organizationService);

	}

	@Test
	public void testThrowExceptionWithBadRequestStatus() throws Exception {
		String organizations = "{\"crappy_field\": \"crappy_value\"}";
		mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON).content(organizations))
				.andExpect(status().isBadRequest());
		verify(organizationService, atLeast(0)).addOrUpdateOrganization(organizationArgumentCaptor.capture());
	}

	@Test
	public void testCountOrganizations() throws Exception {
		doReturn(2L).when(organizationService).countAllOrganizations();
		ResultActions resultActions = mockMvc
				.perform(get(BASE_URL + "count").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(status().isOk());
		assertNotNull(resultActions);
		MvcResult mvcResult = resultActions.andReturn();
		assertNotNull(mvcResult);
		assertNotNull(mvcResult.getResponse());
		assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
	}

	@Test
	public void testCountOrganizationsWithException() throws Exception {
		doThrow(new IllegalArgumentException()).when(organizationService).countAllOrganizations();
		ResultActions resultActions = mockMvc
				.perform(get(BASE_URL + "count").contentType(MediaType.APPLICATION_JSON).content(""))
				.andExpect(status().isInternalServerError());
		assertNotNull(resultActions);
		MvcResult mvcResult = resultActions.andReturn();
		assertNotNull(mvcResult);
		assertNotNull(mvcResult.getResponse());
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), mvcResult.getResponse().getStatus());
	}

	@Test
	public void testGetTeamsByPractitionerIdentifier() throws Exception {
		List<Organization> expected = new ArrayList<>();
		expected.add(createSearchOrganization());
		when(organizationService.getOrganizationsByPractitionerIdentifier(anyString())).thenReturn(expected);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "by-practitioner/test-practitioner"))
				.andExpect(status().isOk()).andReturn();
		assertEquals(objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(expected),
				result.getResponse().getContentAsString());
	}
}
