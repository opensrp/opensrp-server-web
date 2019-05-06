package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.domain.PlanDefinition;
import org.opensrp.domain.postgres.Jurisdiction;
import org.opensrp.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Vincent Karuri on 06/05/2019
 */
public class PlanResourceTest extends BaseResourceTest<PlanDefinition> {

    private final static String BASE_URL = "/rest/plans/";

    @Autowired
    private PlanRepository repository;

    @Before
    public void setUp() {
        tableNames.add("core.plan");
        tableNames.add("core.plan_metadata");
        truncateTables();
    }

    @After
    public void tearDown() {
        truncateTables();
    }

    @Test
    public void testGetAllShouldReturnAllPlans() throws Exception {

        List<PlanDefinition> expectedPlans = new ArrayList<>();

        List<Jurisdiction> operationalAreas = new ArrayList<>();
        Jurisdiction operationalArea = new Jurisdiction();
        operationalArea.setCode("operational_area");
        operationalAreas.add(operationalArea);

        PlanDefinition expectedPlan = new PlanDefinition();
        expectedPlan.setIdentifier("plan_1");
        expectedPlan.setJurisdiction(operationalAreas);

        repository.add(expectedPlan);
        expectedPlans.add(expectedPlan);

        expectedPlan = new PlanDefinition();
        expectedPlan.setIdentifier("plan_2");
        expectedPlan.setJurisdiction(operationalAreas);

        repository.add(expectedPlan);
        expectedPlans.add(expectedPlan);

        String actualPlansString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<PlanDefinition> actualPlans = new Gson().fromJson(actualPlansString, new TypeToken<List<PlanDefinition>>(){}.getType());

        assertListsAreSameIgnoringOrder(actualPlans, expectedPlans);
    }

    @Override
    void assertListsAreSameIgnoringOrder(List<PlanDefinition> expectedList, List<PlanDefinition> actualList) {
        if (expectedList == null || actualList == null) {
            assertTrue(false);
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
}
