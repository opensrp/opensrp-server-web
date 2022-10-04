package org.opensrp.web.rest.rapid;

import org.junit.Assert;
import org.junit.Test;
import org.opensrp.web.rest.it.BaseResourceTest;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class RapidClientResourceTests extends BaseResourceTest {

	@Test
	public void testEligibleVaccines() throws Exception {
		int age = 3;
		Method method = RapidClientResource.class.getDeclaredMethod("eligibleVaccines", Integer.class);
		ArrayList<String> response = (ArrayList<String>) method.invoke("eligibleVaccines", age);
		Assert.assertNotNull(response.size());
	}

}
