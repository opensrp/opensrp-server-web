package org.opensrp.web.rest.rapid;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;

@RunWith(SpringRunner.class)
public class RapidClientResourceTests {

	@Test
	public void testEligibleVaccines() throws Exception {
		int age = 3;
		Method method = RapidClientResource.class.getDeclaredMethod("eligibleVaccines", Integer.class);
		ArrayList<String> response = (ArrayList<String>) method.invoke("eligibleVaccines", age);
		Assert.assertNotNull(response.size());

	}

}
