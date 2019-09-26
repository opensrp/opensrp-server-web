package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.PlanDefinition;
import org.opensrp.domain.postgres.Jurisdiction;
import org.opensrp.service.PlanService;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opensrp.web.rest.PlanResource.OPERATIONAL_AREA_ID;

/**
 * Created by Vincent Karuri on 06/05/2019
 */
public class PlanResourceTest extends BaseResourceTest<PlanDefinition> {

    private final static String BASE_URL = "/rest/plans/";

    private PlanService planService;

    private final String plansJson = "{\n" +
            "  \"identifier\": \"plan_1\",\n" +
            "  \"version\": \"\",\n" +
            "  \"name\": \"\",\n" +
            "  \"title\": \"\",\n" +
            "  \"status\": \"\",\n" +
            "  \"date\": \"2019-04-10\",\n" +
            "  \"effectivePeriod\": {\n" +
            "    \"start\": \"2019-04-10\",\n" +
            "    \"end\": \"2019-04-10\"\n" +
            "  },\n" +
            "  \"useContext\": [\n" +
            "    {\n" +
            "      \"code\": \"\",\n" +
            "      \"valueCodableConcept\": \"\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"code\": \"\",\n" +
            "      \"valueCodableConcept\": \"\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"jurisdiction\": [\n" +
            "    {\n" +
            "      \"code\": \"operational_area_1\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"goal\": [\n" +
            "    {\n" +
            "      \"id\": \"\",\n" +
            "      \"description\": \"\",\n" +
            "      \"priority\": 1,\n" +
            "      \"target\": [\n" +
            "        {\n" +
            "          \"measure\": \"\",\n" +
            "          \"detail\": {\n" +
            "            \"detailQuantity\": {\n" +
            "              \"value\": 8,\n" +
            "              \"comparator\": \"\",\n" +
            "              \"unit\": \"\"\n" +
            "            },\n" +
            "            \"detailRange\": {\n" +
            "              \"high\": {\n" +
            "                \"value\": 0.2,\n" +
            "                \"comparator\": \"\",\n" +
            "                \"unit\": \"\"\n" +
            "              },\n" +
            "              \"low\": {\n" +
            "                \"value\": 0.2,\n" +
            "                \"comparator\": \"\",\n" +
            "                \"unit\": \"\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"detailCodableConcept\": {\n" +
            "              \"text\": \"\"\n" +
            "            }\n" +
            "          },\n" +
            "          \"due\": \"2019-04-10\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"action\": [\n" +
            "    {\n" +
            "      \"identifier\": \"\",\n" +
            "      \"prefix\": 1,\n" +
            "      \"title\": \"\",\n" +
            "      \"description\": \"\",\n" +
            "      \"code\": \"\",\n" +
            "      \"timingPeriod\": {\n" +
            "        \"start\": \"2019-04-10\",\n" +
            "        \"end\": \"2019-04-10\"\n" +
            "      },\n" +
            "      \"reason\": \"\",\n" +
            "      \"goalId\": \"\",\n" +
            "      \"subjectCodableConcept\": {\n" +
            "        \"text\": \"\"\n" +
            "      },\n" +
            "      \"taskTemplate\": \"\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"serverVersion\": 0\n" +
            "}";

    private ArgumentCaptor<PlanDefinition> argumentCaptor = ArgumentCaptor.forClass(PlanDefinition.class);

    private Class<ArrayList<String>> listClass =
            (Class<ArrayList<String>>)(Class)ArrayList.class;

    @Captor
    private ArgumentCaptor<ArrayList<String>> listArgumentCaptor = ArgumentCaptor.forClass(listClass);

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

    @Before
    public void setUp() {
        planService = mock(PlanService.class);
        PlanResource planResource = webApplicationContext.getBean(PlanResource.class);
        planResource.setPlanService(planService);
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

        doReturn(expectedPlans).when(planService).getAllPlans();

        String actualPlansString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

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

        doReturn(Collections.singletonList(expectedPlan)).when(planService).getPlansByIdsReturnOptionalFields(anyList(), anyList());

        String actualPlansString = getResponseAsString(BASE_URL + "plan_1", null, MockMvcResultMatchers.status().isOk());
        List<PlanDefinition>  actualPlanList = new Gson().fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

        assertNotNull(actualPlanList);
        assertEquals(1, actualPlanList.size());
        PlanDefinition actualPlan = actualPlanList.get(0);

        assertEquals(actualPlan.getIdentifier(), expectedPlan.getIdentifier());
        assertEquals(actualPlan.getJurisdiction().get(0).getCode(), expectedPlan.getJurisdiction().get(0).getCode());
    }

    @Test
    public void testCreateShouldCreateNewPlanResource() throws Exception {
        doReturn(new PlanDefinition()).when(planService).addPlan(any(PlanDefinition.class));
        List<Jurisdiction> operationalAreas = new ArrayList<>();
        Jurisdiction operationalArea = new Jurisdiction();
        operationalArea.setCode("operational_area_1");
        operationalAreas.add(operationalArea);

        PlanDefinition expectedPlan = new PlanDefinition();
        expectedPlan.setIdentifier("plan_1");
        expectedPlan.setJurisdiction(operationalAreas);

        postRequestWithJsonContent(BASE_URL, plansJson, MockMvcResultMatchers.status().isCreated());

        verify(planService).addPlan(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPlan.getIdentifier());
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

        String plansJson = new Gson().toJson(expectedPlan, new TypeToken<PlanDefinition>(){}.getType());
        putRequestWithJsonContent(BASE_URL, plansJson, MockMvcResultMatchers.status().isCreated());

        verify(planService).updatePlan(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPlan.getIdentifier());
    }

    @Test
    public void testSyncByServerVersionAndOperationalAreaShouldSyncCorrectPlans() throws Exception {
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

        doReturn(expectedPlans).when(planService).getPlansByServerVersionAndOperationalArea(anyLong(), anyList());

        String data = "{\"serverVersion\":\"1\",\"operational_area_id\":[\"operational_area\",\"operational_area_2\"]}";
        String actualPlansString = postRequestWithJsonContentAndReturnString(BASE_URL + "sync", data, MockMvcResultMatchers.status().isOk());

        List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

        verify(planService).getPlansByServerVersionAndOperationalArea(longArgumentCaptor.capture(), listArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue().longValue(), 1);
        List<String> list  = listArgumentCaptor.getValue();
        assertEquals(list.get(0), "operational_area");
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

        doReturn(expectedPlans).when(planService).getPlansByServerVersionAndOperationalArea(anyLong(), anyList());

        String actualPlansString = getResponseAsString(BASE_URL + "sync", AllConstants.BaseEntity.SERVER_VERSIOIN + "="+ 1 + "&" + OPERATIONAL_AREA_ID + "=" + "operational_area" + "&" + OPERATIONAL_AREA_ID + "=" + "operational_area_2", MockMvcResultMatchers.status().isOk());
        List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

        verify(planService).getPlansByServerVersionAndOperationalArea(longArgumentCaptor.capture(), listArgumentCaptor.capture());
        assertEquals(longArgumentCaptor.getValue().longValue(), 1);
        assertEquals(listArgumentCaptor.getValue().get(0), "operational_area" );
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

        List<String> fieldNameList = new ArrayList<>();
        fieldNameList.add("action");
        fieldNameList.add("name");

        doReturn(Collections.singletonList(expectedPlan)).when(planService).getPlansByIdsReturnOptionalFields(planIdList, fieldNameList);

        String actualPlansString = getResponseAsString(BASE_URL + "findByIdsWithOptionalFields?identifiers=" + expectedPlan.getIdentifier() + "&fields=action,name", null, MockMvcResultMatchers.status().isOk());
        List<PlanDefinition>  actualPlanList = new Gson().fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

        assertNotNull(actualPlanList);
        assertEquals(1, actualPlanList.size());
        PlanDefinition actualPlan = actualPlanList.get(0);

        assertEquals(actualPlan.getIdentifier(), expectedPlan.getIdentifier());
        assertEquals(actualPlan.getJurisdiction().get(0).getCode(), expectedPlan.getJurisdiction().get(0).getCode());
    }
}
