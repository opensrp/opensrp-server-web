package org.opensrp.web.rest;

import junit.framework.TestCase;
import org.junit.Assert;
import org.smartregister.domain.Practitioner;

import java.util.Collections;
import java.util.List;

public class ResponseUtilTest extends TestCase {

	public void testPrepareDataResponseWithObjectParameter() throws IllegalAccessException, InstantiationException {
		Practitioner dataResponse = ResponseUtil.prepareDataResponse(getPractitioner(), new String[] {"identifier"});
		Assert.assertNotNull(dataResponse);
		Assert.assertEquals(dataResponse.getName(), "Eliud Owalo");
		Assert.assertEquals(dataResponse.getUsername(), "eliud");
		Assert.assertNull(dataResponse.getIdentifier());
	}

	private Practitioner getPractitioner() {
		Practitioner practitioner = new Practitioner();
		practitioner.setIdentifier("SomeIdentifier");
		practitioner.setName("Eliud Owalo");
		practitioner.setUsername("eliud");
		practitioner.setUserId("9109sj-12o90-21299");
		return practitioner;
	}

	public void testPrepareDataResponseWithListParameter() throws IllegalAccessException, InstantiationException {
		List<Practitioner> dataResponse = ResponseUtil.prepareDataResponse(Collections.singletonList(getPractitioner()), new String[] {"identifier"});
		Assert.assertNotNull(dataResponse);
		Assert.assertEquals(dataResponse.size(), 1);
		Practitioner practitioner = dataResponse.get(0);
		Assert.assertEquals(practitioner.getName(), "Eliud Owalo");
		Assert.assertEquals(practitioner.getUsername(), "eliud");
		Assert.assertNull(practitioner.getIdentifier());
	}
}
