package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.service.PlanService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Created by Brian Mwasi
 */
public class PlanIdentifierResourceTest extends BaseResourceTest<String> {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	private final static String BASE_URL = "/rest/planIdentifiers";

	private PlanService planService;

	@Before
	public void setUp() {
		planService = mock(PlanService.class);
		PlanIdentifierResource planIdentifierResource = webApplicationContext.getBean(PlanIdentifierResource.class);
		planIdentifierResource.setPlanService(planService);
	}

	@Test
	public void testGetPlanIdentifiersByUserNameShouldReturnListOfIdentifiersPlans() throws Exception {
		List<String> expectedPlanIdentifiers = new ArrayList<>();
		expectedPlanIdentifiers.add("plan_1");
		expectedPlanIdentifiers.add("plan_2");

		doReturn(expectedPlanIdentifiers).when(planService).getPlanIdentifiersByUsername("mwasi");

		String actualIdentifierString = getResponseAsString(BASE_URL, "mwasi", status().isOk());
		List<String> actualPlanIdentifiers = new Gson().fromJson(actualIdentifierString, new TypeToken<List<String>>(){}.getType());

		assertListsAreSameIgnoringOrder(actualPlanIdentifiers, expectedPlanIdentifiers);
	}

	@Override
	protected void assertListsAreSameIgnoringOrder(List<String> expectedList, List<String> actualList) {
		if (expectedList == null || actualList == null) {
			throw new AssertionError("One of the lists is null");
		}
		assertEquals(expectedList.size(), actualList.size());
	}
}
