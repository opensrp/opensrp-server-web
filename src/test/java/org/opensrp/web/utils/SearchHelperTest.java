package org.opensrp.web.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensrp.common.util.EasyMap;
import org.smartregister.domain.Client;
import org.springframework.mock.web.MockHttpServletRequest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class SearchHelperTest {
	
	@Test
	public void testCreateClientListIfEmptyCreatesListOnNull() {

		List<Client> list = SearchHelper.createClientListIfEmpty(null);
		Assert.assertNotNull(list);
	}

	@Test
	public void testCreateClientListIfEmptyDoesNotModifyParameterList() {
		Client client = new Client("dummy-base-entity-id");
		client.setFirstName("Johnny");
		client.setLastName("Test");
		client.setGender("MALE");

		List<Client> myClientList = Arrays.asList(new Client[] { client });

		List<Client> list = SearchHelper.createClientListIfEmpty(myClientList);

		Assert.assertNotNull(list);
		org.springframework.util.Assert.notEmpty(list);
		Assert.assertEquals("dummy-base-entity-id", list.get(0).getBaseEntityId());
		Assert.assertEquals("Johnny", list.get(0).getFirstName());
		Assert.assertEquals("Test", list.get(0).getLastName());
		Assert.assertEquals("MALE", list.get(0).getGender());
	}

	@Test
	public void testGetContactPhoneNumberParamReturnsCorrectValueForParam() {

		String MOTHER_GUARDIAN_PHONE_NUMBER = "mother_contact_phone_number";
		String CONTACT_PHONE_NUMBER = "contact_phone_number";

		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter(MOTHER_GUARDIAN_PHONE_NUMBER, "+254738388383");
		req.addParameter(CONTACT_PHONE_NUMBER, "+2547112233445");

		String motherGuardianNum = SearchHelper.getContactPhoneNumberParam(req);

		Assert.assertNotNull(motherGuardianNum);
		Assert.assertEquals("+254738388383", motherGuardianNum);

		req.removeParameter(MOTHER_GUARDIAN_PHONE_NUMBER);
		motherGuardianNum = SearchHelper.getContactPhoneNumberParam(req);

		Assert.assertNotNull(motherGuardianNum);
		Assert.assertEquals("+2547112233445", motherGuardianNum);
	}

	@Test
	public void testIntersectionReturnsEmptyListIfBothListsEmptyOrNull() {

		List<Client> list = new ArrayList<>();

		List<Client> list2 = new ArrayList<>();

		List<Client> resultList = SearchHelper.intersection(list, list2);

		Assert.assertNotNull(resultList);
		Assert.assertEquals(0, resultList.size());

		resultList = SearchHelper.intersection(new ArrayList<Client>(), new ArrayList<Client>());

		Assert.assertNotNull(resultList);
		Assert.assertEquals(0, resultList.size());

	}

	@Test
	public void testIntersectionReturnsClientListAIfClientBListIsNullOrEmpty() {

		List<Client> list = new ArrayList<>();

		Client client = new Client("dummy-base-entity-id_1");
		client.setFirstName("Johnny");
		client.setLastName("Test");
		client.setGender("MALE");

		Client client2 = new Client("dummy-base-entity-id_2");
		client2.setFirstName("Jane");
		client2.setLastName("Test");
		client2.setGender("FEMALE");

		list.add(client);
		list.add(client2);

		List<Client> resultList = SearchHelper.intersection(list, null);

		Assert.assertNotNull(resultList);
		Assert.assertEquals(2, resultList.size());
		Assert.assertEquals("Johnny", resultList.get(0).getFirstName());
		Assert.assertEquals("Jane", resultList.get(1).getFirstName());

		resultList = SearchHelper.intersection(list, new ArrayList<Client>());

		Assert.assertNotNull(resultList);
		Assert.assertEquals(2, resultList.size());
		Assert.assertEquals("Johnny", resultList.get(0).getFirstName());
		Assert.assertEquals("Jane", resultList.get(1).getFirstName());

	}

	@Test
	public void testIntersectionReturnsClientListBIfClientAListIsNullOrEmpty() {

		List<Client> list = new ArrayList<>();

		Client client3 = new Client("dummy-base-entity-id_3");
		client3.setFirstName("James");
		client3.setLastName("Dean");
		client3.setGender("MALE");

		Client client4 = new Client("dummy-base-entity-id_1");
		client4.setFirstName("Johnny");
		client4.setLastName("Test");
		client4.setGender("MALE");

		Client client5 = new Client("dummy-base-entity-id_2");
		client5.setFirstName("Jane");
		client5.setLastName("Test");
		client5.setGender("FEMALE");

		List<Client> list2 = new ArrayList<>();

		list2.add(client3);
		list2.add(client4);
		list2.add(client5);

		List<Client> resultList = SearchHelper.intersection(list, list2);

		Assert.assertNotNull(resultList);
		Assert.assertEquals(3, resultList.size());
		Assert.assertEquals("James", resultList.get(0).getFirstName());
		Assert.assertEquals("Johnny", resultList.get(1).getFirstName());
		Assert.assertEquals("Jane", resultList.get(2).getFirstName());

		resultList = SearchHelper.intersection(null, list2);

		Assert.assertNotNull(resultList);
		Assert.assertEquals(3, resultList.size());
		Assert.assertEquals("James", resultList.get(0).getFirstName());
		Assert.assertEquals("Johnny", resultList.get(1).getFirstName());
		Assert.assertEquals("Jane", resultList.get(2).getFirstName());

	}

	@Test
	public void testIntersectionReturnsAConjunctionOfTwoClientLists() {

		List<Client> list = new ArrayList<>();

		Client client = new Client("dummy-base-entity-id_1");
		client.setFirstName("Johnny");
		client.setLastName("Test");
		client.setGender("MALE");

		Client client2 = new Client("dummy-base-entity-id_2");
		client2.setFirstName("Jane");
		client2.setLastName("Test");
		client2.setGender("FEMALE");

		list.add(client);
		list.add(client2);

		Client client3 = new Client("dummy-base-entity-id_3");
		client3.setFirstName("James");
		client3.setLastName("Dean");
		client3.setGender("MALE");

		Client client4 = new Client("dummy-base-entity-id_1");
		client4.setFirstName("Johnny");
		client4.setLastName("Test");
		client4.setGender("MALE");

		Client client5 = new Client("dummy-base-entity-id_2");
		client5.setFirstName("Jane");
		client5.setLastName("Test");
		client5.setGender("FEMALE");

		List<Client> list2 = new ArrayList<>();

		list2.add(client3);
		list2.add(client4);
		list2.add(client5);

		List<Client> resultList = SearchHelper.intersection(list, list2);

		Assert.assertNotNull(resultList);
		Assert.assertEquals(2, resultList.size());
		Assert.assertEquals("Johnny", resultList.get(0).getFirstName());
		Assert.assertEquals("Jane", resultList.get(1).getFirstName());

	}

	@Test
	public void  childSearchParamProcessorShouldHaveCorrectIdentifierWhenCreatingBean() throws ParseException {
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(httpServletRequest.getParameter("limit")).thenReturn("20");
		Mockito.when(httpServletRequest.getParameter("opensrp_id")).thenReturn("2093980");
		SearchEntityWrapper searchEntityWrapper = SearchHelper.childSearchParamProcessor(httpServletRequest);
		String result = searchEntityWrapper.getClientSearchBean().getIdentifiers().get("opensrp_id");
		Assert.assertEquals("2093980", result);
	}

	@Test
	public void testProcessSearchResult() {
		final String mother = "mother";

		Client motherClient = new Client("dummy-mother-base-entity-id");
		motherClient.setFirstName("Jane");
		motherClient.setLastName("Doe");
		motherClient.setGender("FEMALE");
		motherClient.setIdentifiers(EasyMap.mapOf("M_ZEIR_ID", "673939_mother"));
		List<Client> motherList = new ArrayList<>();
		motherList.add(motherClient);

		List<String> list = new ArrayList<>();
		list.add("dummy-mother-base-entity-id");

		Map<String, List<String>> relationships = new HashMap<>();
		relationships.put(mother, list);

		Client child = new Client("dummy-mother-base-entity-id");
		child.setFirstName("John");
		child.setLastName("Doe");
		child.setGender("Male");
		child.setRelationships(relationships);
		child.setIdentifiers(EasyMap.mapOf("M_ZEIR_ID", "673939_mother"));

		List<Client> childList = new ArrayList<>();
		childList.add(child);

		List<ChildMother> childMothers = SearchHelper.processSearchResult(childList, motherList, mother);
		Assert.assertEquals(1, childMothers.size());
		Assert.assertEquals("John Doe", childMothers.get(0).getChild().fullName());
	}

}
