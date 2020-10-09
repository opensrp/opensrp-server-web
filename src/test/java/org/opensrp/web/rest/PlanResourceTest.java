package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.web.rest.PlanResource.OPERATIONAL_AREA_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.domain.LocationDetail;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.web.bean.Identifier;
import org.smartregister.domain.Jurisdiction;
import org.smartregister.domain.PlanDefinition;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Vincent Karuri on 06/05/2019
 */
public class PlanResourceTest extends BaseSecureResourceTest<PlanDefinition> {
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	private final static String BASE_URL = "/rest/plans/";
	
	private PlanService planService;
	
	private PhysicalLocationService locationService;
	
	private final String plansJson = "{\n" + "  \"identifier\": \"plan_1\",\n" + "  \"version\": \"\",\n"
	        + "  \"name\": \"\",\n" + "  \"title\": \"\",\n" + "  \"status\": \"\",\n" + "  \"date\": \"2019-04-10\",\n"
	        + "  \"effectivePeriod\": {\n" + "    \"start\": \"2019-04-10\",\n" + "    \"end\": \"2019-04-10\"\n" + "  },\n"
	        + "  \"useContext\": [\n" + "    {\n" + "      \"code\": \"\",\n" + "      \"valueCodableConcept\": \"\"\n"
	        + "    },\n" + "    {\n" + "      \"code\": \"\",\n" + "      \"valueCodableConcept\": \"\"\n" + "    }\n"
	        + "  ],\n" + "  \"jurisdiction\": [\n" + "    {\n" + "      \"code\": \"operational_area_1\"\n" + "    }\n"
	        + "  ],\n" + "  \"goal\": [\n" + "    {\n" + "      \"id\": \"\",\n" + "      \"description\": \"\",\n"
	        + "      \"priority\": 1,\n" + "      \"target\": [\n" + "        {\n" + "          \"measure\": \"\",\n"
	        + "          \"detail\": {\n" + "            \"detailQuantity\": {\n" + "              \"value\": 8,\n"
	        + "              \"comparator\": \"\",\n" + "              \"unit\": \"\"\n" + "            },\n"
	        + "            \"detailRange\": {\n" + "              \"high\": {\n" + "                \"value\": 0.2,\n"
	        + "                \"comparator\": \"\",\n" + "                \"unit\": \"\"\n" + "              },\n"
	        + "              \"low\": {\n" + "                \"value\": 0.2,\n" + "                \"comparator\": \"\",\n"
	        + "                \"unit\": \"\"\n" + "              }\n" + "            },\n"
	        + "            \"detailCodableConcept\": {\n" + "              \"text\": \"\"\n" + "            }\n"
	        + "          },\n" + "          \"due\": \"2019-04-10\"\n" + "        }\n" + "      ]\n" + "    }\n" + "  ],\n"
	        + "  \"action\": [\n" + "    {\n" + "      \"identifier\": \"\",\n" + "      \"prefix\": 1,\n"
	        + "      \"title\": \"\",\n" + "      \"description\": \"\",\n" + "      \"code\": \"\",\n"
	        + "      \"timingPeriod\": {\n" + "        \"start\": \"2019-04-10\",\n" + "        \"end\": \"2019-04-10\"\n"
	        + "      },\n" + "      \"reason\": \"\",\n" + "      \"goalId\": \"\",\n"
	        + "      \"subjectCodableConcept\": {\n" + "        \"text\": \"\"\n" + "      },\n"
	        + "      \"taskTemplate\": \"\"\n" + "    }\n" + "  ],\n" + "  \"serverVersion\": 0\n" + "}";
	
	private ArgumentCaptor<PlanDefinition> argumentCaptor = ArgumentCaptor.forClass(PlanDefinition.class);
	
	private Class<ArrayList<String>> listClass = (Class<ArrayList<String>>) (Class) ArrayList.class;
	
	@Captor
	private ArgumentCaptor<ArrayList<String>> listArgumentCaptor = ArgumentCaptor.forClass(listClass);
	
	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
	
	@Captor
	private ArgumentCaptor<List<Long>> orgsArgumentCaptor;

	@Captor
	private ArgumentCaptor<Boolean> booleanArgumentCaptor = ArgumentCaptor.forClass(boolean.class);

	@Before
	public void setUp() {
		planService = mock(PlanService.class);
		locationService = mock(PhysicalLocationService.class);
		PlanResource planResource = webApplicationContext.getBean(PlanResource.class);
		planResource.setPlanService(planService);
		planResource.setLocationService(locationService);
	}
	
	@Test
	public void testGetPlansShouldReturnAllPlans() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		
		expectedPlans.add(expectedPlan);
		
		doReturn(expectedPlans).when(planService).getAllPlans(false);
		
		String actualPlansString = getResponseAsString(BASE_URL, null, status().isOk());
		List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		assertListsAreSameIgnoringOrder(actualPlans, expectedPlans);
	}
	
	@Test
	public void testGetPlanByUniqueIdShouldReturnCorrectPlan() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<String> planIdList = new ArrayList<>();
		planIdList.add(expectedPlan.getIdentifier());
		expectedPlans.add(expectedPlan);

		doReturn(expectedPlans).when(planService).getPlansByIdsReturnOptionalFields(anyList(),
		    eq(null), anyBoolean());
		
		String actualPlansString = getResponseAsString(BASE_URL + "plan_1", null, status().isOk());
		List<PlanDefinition> actualPlanList = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		assertNotNull(actualPlanList);
		assertEquals(1, actualPlanList.size());
		PlanDefinition actualPlan = actualPlanList.get(0);
		
		assertEquals(actualPlan.getIdentifier(), expectedPlan.getIdentifier());
		assertEquals(actualPlan.getJurisdiction().get(0).getCode(), expectedPlan.getJurisdiction().get(0).getCode());
	}
	
	@Test
	public void testCreateShouldCreateNewPlanResource() throws Exception {
		doReturn(new PlanDefinition()).when(planService).addPlan(any(PlanDefinition.class), anyString());
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_1");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		postRequestWithJsonContent(BASE_URL, plansJson, status().isCreated());
		
		verify(planService).addPlan(argumentCaptor.capture(), eq(authenticatedUser.getFirst().getUsername()));
		assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPlan.getIdentifier());
	}
	
	@Test
	public void testCreateShouldThrowException() throws Exception {
		doThrow(new JsonSyntaxException("Unable to parse exception")).when(planService).addPlan(any(PlanDefinition.class),
		    anyString());
		postRequestWithJsonContent(BASE_URL, plansJson, status().isBadRequest());
	}
	
	@Test
	public void testUpdateShouldUpdateExistingPlanResource() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_1");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		
		String plansJson = new Gson().toJson(expectedPlan, new TypeToken<PlanDefinition>() {}.getType());
		putRequestWithJsonContent(BASE_URL, plansJson, status().isCreated());
		
		verify(planService).updatePlan(argumentCaptor.capture(), eq(authenticatedUser.getFirst().getUsername()));
		assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPlan.getIdentifier());
	}
	
	@Test
	public void testUpdateShouldThrowException() throws Exception {
		doThrow(new JsonSyntaxException("Unable to parse exception")).when(planService).updatePlan(any(PlanDefinition.class),
		    anyString());
		putRequestWithJsonContent(BASE_URL, plansJson, status().isBadRequest());
	}
	
	@Test
	public void testSyncByServerVersionAndAssignedPlansOnOrganization() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		doReturn(expectedPlans).when(planService).getPlansByOrganizationsAndServerVersion(anyList(), anyLong(), anyBoolean());
		
		String data = "{\"serverVersion\":\"1\",\"operational_area_id\":[\"operational_area\",\"operational_area_2\"],\"organizations\":[2]}";
		String actualPlansString = postRequestWithJsonContentAndReturnString(BASE_URL + "sync", data, status().isOk());
		
		Gson gson = PlanResource.gson;
		List<PlanDefinition> actualPlans = gson.fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		verify(planService).getPlansByOrganizationsAndServerVersion(orgsArgumentCaptor.capture(),
		    longArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		assertEquals(longArgumentCaptor.getValue().longValue(), 1);
		List<Long> list = orgsArgumentCaptor.getValue();
		assertEquals(2l, list.get(0), 0);
		assertEquals(1, longArgumentCaptor.getValue(), 0);
		
		assertEquals(3, actualPlans.size());
		assertEquals("plan_1", actualPlans.get(0).getIdentifier());
		assertEquals("plan_2", actualPlans.get(1).getIdentifier());
		assertEquals("plan_3", actualPlans.get(2).getIdentifier());
		
		assertEquals(gson.toJson(expectedPlans), gson.toJson(actualPlans));
		
	}
	
	@Test
	public void testSyncByServerVersionAndAssignedPlansOnOrganizationWithoutOrgsAndAuthencation() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		doReturn(expectedPlans).when(planService).getPlansByOrganizationsAndServerVersion(anyList(), anyLong(), anyBoolean());
		
		String data = "{\"serverVersion\":\"1\",\"operational_area_id\":[\"operational_area\",\"operational_area_2\"]}";
		
		this.mockMvc
		        .perform(post(BASE_URL + "sync").contentType(MediaType.APPLICATION_JSON).content(data.getBytes())
		                .accept(MediaType.APPLICATION_JSON))
		        .andExpect(status().isBadRequest()).andReturn();
		
	
		verify(planService, never()).getPlansByOrganizationsAndServerVersion(orgsArgumentCaptor.capture(),
		    longArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		
	}
	
	public void testGetSyncByServerVersionAndOperationalAreaShouldSyncCorrectPlans() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		
		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);
		
		doReturn(expectedPlans).when(planService).getPlansByServerVersionAndOperationalArea(anyLong(), anyList(), anyBoolean());
		
		String actualPlansString = getResponseAsString(BASE_URL + "sync", SERVER_VERSIOIN + "=" + 1 + "&"
		        + OPERATIONAL_AREA_ID + "=" + "operational_area" + "&" + OPERATIONAL_AREA_ID + "=" + "operational_area_2",
		    status().isOk());
		List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		verify(planService).getPlansByServerVersionAndOperationalArea(longArgumentCaptor.capture(),
		    listArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		assertEquals(longArgumentCaptor.getValue().longValue(), 1);
		assertEquals(listArgumentCaptor.getValue().get(0), "operational_area");
		assertEquals(expectedPlans, actualPlans);
	}
	
	@Override
	protected void assertListsAreSameIgnoringOrder(List<PlanDefinition> expectedList, List<PlanDefinition> actualList) {
		if (expectedList == null || actualList == null) {
			throw new AssertionError("One of the lists is null");
		}
		
		assertEquals(expectedList.size(), actualList.size());
		
		Set<String> expectedIds = new HashSet<>();
		for (PlanDefinition plan : expectedList) {
			expectedIds.add(plan.getIdentifier());
		}
		
		for (PlanDefinition plan : actualList) {
			assertTrue(expectedIds.contains(plan.getIdentifier()));
		}
	}
	
	@Test
	public void testfindByIdentifiersReturnOptionalFieldsShouldReturnCorrectPlans() throws Exception {
		
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<String> planIdList = new ArrayList<>();
		planIdList.add(expectedPlan.getIdentifier());
		
		List<String> fieldNameList = new ArrayList<>();
		fieldNameList.add("action");
		fieldNameList.add("name");
		
		doReturn(Collections.singletonList(expectedPlan)).when(planService).getPlansByIdsReturnOptionalFields(planIdList,
		    fieldNameList, false);
		
		String actualPlansString = getResponseAsString(
		    BASE_URL + "findByIdsWithOptionalFields?identifiers=" + expectedPlan.getIdentifier() + "&fields=action,name",
		    null, status().isOk());
		List<PlanDefinition> actualPlanList = new Gson().fromJson(actualPlansString,
		    new TypeToken<List<PlanDefinition>>() {}.getType());
		
		assertNotNull(actualPlanList);
		assertEquals(1, actualPlanList.size());
		PlanDefinition actualPlan = actualPlanList.get(0);
		
		assertEquals(actualPlan.getIdentifier(), expectedPlan.getIdentifier());
		assertEquals(actualPlan.getJurisdiction().get(0).getCode(), expectedPlan.getJurisdiction().get(0).getCode());
	}
	
	@Test
	public void testFindLocationNamesByPlanId() throws Exception {
		LocationDetail locationDetail = new LocationDetail();
		locationDetail.setIdentifier("304cbcd4-0850-404a-a8b1-486b02f7b84d");
		locationDetail.setName("location one");
		
		Set<LocationDetail> locationDetails = Collections.singleton(locationDetail);
		when(locationService.findLocationDetailsByPlanId(anyString())).thenReturn(locationDetails);
		MvcResult result = mockMvc.perform(get(BASE_URL + "findLocationNames/{planIdentifier}", "plan_id"))
		        .andExpect(status().isOk()).andReturn();
		verify(locationService).findLocationDetailsByPlanId(anyString());
		assertEquals(new GsonBuilder().serializeNulls().create().toJson(locationDetails),
		    result.getResponse().getContentAsString());
		
	}
	
	@Test
	public void testGetAll() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<PlanDefinition> planDefinitions = Collections.singletonList(expectedPlan);
		when(planService.getAllPlans(anyLong(), anyInt(), anyBoolean())).thenReturn(planDefinitions);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/getAll?serverVersion=0&limit=25")).andExpect(status().isOk())
		        .andReturn();
		verify(planService).getAllPlans(anyLong(), anyInt(), anyBoolean());
		assertEquals(PlanResource.gson.toJson(planDefinitions), result.getResponse().getContentAsString());
		
	}

	@Test
	public void testCountAll() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);

		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);

		List<PlanDefinition> planDefinitions = Collections.singletonList(expectedPlan);
		when(planService.getAllPlans(anyLong(), anyInt(), anyBoolean())).thenReturn(planDefinitions);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/countAll?serverVersion=0&limit=25")).andExpect(status().isOk())
				.andReturn();
		verify(planService).getAllPlans(anyLong(), anyInt(), anyBoolean());
		assertEquals(1 , new JSONObject(result.getResponse().getContentAsString()).optInt("count"));

	}
	
	@Test
	public void testFindAllIds() throws Exception {
		Pair<List<String>, Long> idsModel = Pair.of(Collections.singletonList("plan-id-1"), 12345l);
		when(planService.findAllIds(anyLong(), anyInt(), anyBoolean(), isNull(), isNull())).thenReturn(idsModel);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/findIds?serverVersion=0", "")).andExpect(status().isOk())
		        .andReturn();
		
		String actualTaskIdString = result.getResponse().getContentAsString();
		Identifier actualIdModels = new Gson().fromJson(actualTaskIdString, new TypeToken<Identifier>() {}.getType());
		List<String> actualTaskIdList = actualIdModels.getIdentifiers();
		
		verify(planService).findAllIds(anyLong(), anyInt(), anyBoolean(), isNull(), isNull());
		verifyNoMoreInteractions(planService);
		assertEquals("{\"identifiers\":[\"plan-id-1\"],\"lastServerVersion\":12345}",
		    result.getResponse().getContentAsString());
		assertEquals((idsModel.getLeft()).get(0), actualTaskIdList.get(0));
		assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());
	}
	
	@Test
	public void testFindByUsername() throws Exception {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		
		List<PlanDefinition> planDefinitions = Collections.singletonList(expectedPlan);
		when(planService.getPlansByUsernameAndServerVersion("onatest", 0l, false)).thenReturn(planDefinitions);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/user/onatest?serverVersion=0")).andExpect(status().isOk())
		        .andReturn();
		verify(planService).getPlansByUsernameAndServerVersion("onatest", 0l, false);
		assertEquals(PlanResource.gson.toJson(planDefinitions), result.getResponse().getContentAsString());
		
	}
	
	@Test
	public void testGetSync() throws Exception {
		List<PlanDefinition> planDefinitions = new ArrayList<>();
		planDefinitions.add(createPlanDefinition());
		List<String> operationalAreaIds = new ArrayList<>();
		operationalAreaIds.add("1");
		
		doReturn(planDefinitions).when(planService).getPlansByServerVersionAndOperationalArea(any(long.class), anyList(), anyBoolean());
		
		String parameter = SERVER_VERSIOIN + "=15421904649873&" + OPERATIONAL_AREA_ID + "=[1]";
		String response = getResponseAsString(BASE_URL + "/sync", parameter, status().isOk());
		JsonNode actualObj = mapper.readTree(response);
		
		verify(planService).getPlansByServerVersionAndOperationalArea(longArgumentCaptor.capture(),
		    listArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		assertEquals(longArgumentCaptor.getValue().longValue(), 15421904649873l);
		assertEquals(listArgumentCaptor.getAllValues().size(), operationalAreaIds.size());
		assertEquals(actualObj.get(0).get("identifier").textValue(), planDefinitions.get(0).getIdentifier());
		
	}
	
	@Test
	public void testSyncByServerVersionAndAssignedPlansOnOrganizationWithReturnCount() throws Exception {
		List<PlanDefinition> expectedPlans = new ArrayList<>();
		long returnCount = 9l;

		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);

		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);

		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_2");
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(0l);
		expectedPlans.add(expectedPlan);

		expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_3");
		operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area_2");
		operationalAreas.clear();
		operationalAreas.add(operationalArea);
		expectedPlan.setJurisdiction(operationalAreas);
		expectedPlan.setServerVersion(1l);
		expectedPlans.add(expectedPlan);

		doReturn(expectedPlans).when(planService).getPlansByOrganizationsAndServerVersion(anyList(), anyLong(), anyBoolean());
		doReturn(returnCount).when(planService).countPlansByOrganizationsAndServerVersion(anyList(), anyLong());

		String data = "{\"serverVersion\":\"1\",\"operational_area_id\":[\"operational_area\",\"operational_area_2\"],\"organizations\":[2], \"return_count\":true}";
		String actualPlansString = postRequestWithJsonContentAndReturnString(BASE_URL + "sync", data, status().isOk());

		Gson gson =PlanResource.gson;
		List<PlanDefinition> actualPlans = gson.fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

		verify(planService).getPlansByOrganizationsAndServerVersion(orgsArgumentCaptor.capture(), longArgumentCaptor.capture(), booleanArgumentCaptor.capture());
		verify(planService).countPlansByOrganizationsAndServerVersion(any(), anyLong());
		assertEquals(longArgumentCaptor.getValue().longValue(), 1);
		List<Long> list  = orgsArgumentCaptor.getValue();
		assertEquals(2l,list.get(0),0);
		assertEquals(1,longArgumentCaptor.getValue(),0);

		assertEquals(3, actualPlans.size());
		assertEquals("plan_1", actualPlans.get(0).getIdentifier());
		assertEquals("plan_2", actualPlans.get(1).getIdentifier());
		assertEquals("plan_3", actualPlans.get(2).getIdentifier());
		assertEquals(gson.toJson(expectedPlans), gson.toJson(actualPlans));

	}
	
	private PlanDefinition createPlanDefinition() {
		List<Jurisdiction> operationalAreas = new ArrayList<>();
		Jurisdiction operationalArea = new Jurisdiction();
		operationalArea.setCode("operational_area");
		operationalAreas.add(operationalArea);
		PlanDefinition expectedPlan = new PlanDefinition();
		expectedPlan.setIdentifier("plan_1");
		expectedPlan.setJurisdiction(operationalAreas);
		return expectedPlan;
	}
	
}
