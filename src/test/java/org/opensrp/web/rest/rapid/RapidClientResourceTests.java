package org.opensrp.web.rest.rapid;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath*:context.xml", })
@ActiveProfiles(profiles = { "jedis", "postgres", "basic_auth", "rabbitmq" })
public class RapidClientResourceTests {

	@Test
	public void testEligibleVaccines() throws Exception {
		int age = 3;
		ArrayList<String> response = Whitebox.invokeMethod(RapidClientResource.class, "eligibleVaccines", age);
		Assert.assertNotNull(response.size());
	}

}
