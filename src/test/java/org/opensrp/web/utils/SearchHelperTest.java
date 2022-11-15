package org.opensrp.web.utils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensrp.common.util.EasyMap;
import org.opensrp.search.ClientSearchBean;
import org.smartregister.domain.Client;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.AssertTrue;
import java.text.ParseException;
import java.util.*;

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
	public void childSearchParamProcessorShouldHaveCorrectIdentifierWhenCreatingBean() throws ParseException {
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

	@Test
	public void testMotherSearchParamProcessorForHttpServletRequest() throws ParseException {
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(httpServletRequest.getParameter("limit")).thenReturn("0");
		Mockito.when(httpServletRequest.getParameter("mother_first_name")).thenReturn("Jane");
		Mockito.when(httpServletRequest.getParameter("mother_last_name")).thenReturn("Doe");
		Mockito.when(httpServletRequest.getParameter("mother_nrc_number")).thenReturn("2093980");
		Mockito.when(httpServletRequest.getParameter("NRC_Number")).thenReturn("20939801123");
		Mockito.when(httpServletRequest.getParameter("mother_compass_relationship_id")).thenReturn("dab102f71bd");
		SearchEntityWrapper searchEntityWrapper = SearchHelper.motherSearchParamProcessor(httpServletRequest);
		Map<String, String> result = searchEntityWrapper.getClientSearchBean().getAttributes();
		Assert.assertEquals(2,  result.size());
		Assert.assertTrue( result.containsKey("NRC_Number"));
		Assert.assertTrue( result.containsKey("Compass_Relationship_ID"));
		Assert.assertEquals("2093980", result.get("NRC_Number"));
	}

	@Test
	public void testMotherSearchParamProcessorForJSONObject() throws ParseException {
		JSONObject jsonObject = Mockito.mock(JSONObject.class);
		Mockito.when(jsonObject.optString("limit")).thenReturn("0");
		Mockito.when(jsonObject.optString("mother_first_name")).thenReturn("Jane");
		Mockito.when(jsonObject.optString("mother_last_name")).thenReturn("Doe");
		Mockito.when(jsonObject.optString("mother_nrc_number")).thenReturn("2093980");
		Mockito.when(jsonObject.optString("NRC_Number")).thenReturn("20939801123");
		Mockito.when(jsonObject.optString("mother_compass_relationship_id")).thenReturn("dab102f71bd");
		Mockito.when(jsonObject.optString("lastEdited")).thenReturn("");
		SearchEntityWrapper searchEntityWrapper = SearchHelper.motherSearchParamProcessor(jsonObject);
		Map<String, String> result = searchEntityWrapper.getClientSearchBean().getAttributes();
		Assert.assertEquals(2,  result.size());
		Assert.assertTrue( result.containsKey("NRC_Number"));
		Assert.assertTrue( result.containsKey("Compass_Relationship_ID"));
		Assert.assertEquals("2093980", result.get("NRC_Number"));
	}

	@Test
	public void testChildSearchParamProcessorForJSONObject() throws ParseException {
		JSONObject jsonObject = Mockito.mock(JSONObject.class);
		Mockito.when(jsonObject.optString("limit")).thenReturn("50");
		Mockito.when(jsonObject.optString("lastEdited")).thenReturn("");
		Mockito.when(jsonObject.optString(SearchHelper.BIRTH_DATE)).thenReturn("");
		Mockito.when(jsonObject.optString(SearchHelper.ZEIR_ID)).thenReturn("1234");
		Mockito.when(jsonObject.optString(SearchHelper.OPENSRP_ID)).thenReturn("4567");
		Mockito.when(jsonObject.optString(SearchHelper.SIM_PRINT_GUID)).thenReturn("91011");
		Mockito.when(jsonObject.optString(SearchHelper.INACTIVE)).thenReturn("false");
		Mockito.when(jsonObject.optString(SearchHelper.LOST_TO_FOLLOW_UP)).thenReturn("true");
		Mockito.when(jsonObject.optString(SearchHelper.NFC_CARD_IDENTIFIER)).thenReturn("nfc_card_identifier_1");
		SearchEntityWrapper searchEntityWrapper = SearchHelper.childSearchParamProcessor(jsonObject);

		Map<String, String> attributes = searchEntityWrapper.getClientSearchBean().getAttributes();
		Assert.assertEquals(3,  attributes.size());

		Map<String, String> identifiers = searchEntityWrapper.getClientSearchBean().getIdentifiers();
		Assert.assertEquals(4, identifiers.size());

		Assert.assertTrue(identifiers.containsKey(SearchHelper.ZEIR_ID));
		Assert.assertTrue(identifiers.containsKey("ZEIR_ID")); //check backward compatibility with upper case key
		Assert.assertTrue(identifiers.containsKey(SearchHelper.SIM_PRINT_GUID));

		Assert.assertTrue(attributes.containsKey(SearchHelper.INACTIVE));
		Assert.assertTrue(attributes.containsKey(SearchHelper.LOST_TO_FOLLOW_UP));
		Assert.assertTrue(attributes.containsKey("NFC_Card_Identifier"));

		Assert.assertEquals(identifiers.get(SearchHelper.ZEIR_ID), "1234");
		Assert.assertEquals(attributes.get("NFC_Card_Identifier"), "nfc_card_identifier_1");
	}

	@Test
	public void testChildSearchParamProcessorForHttpServletRequest() throws ParseException {
		HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
		Mockito.when(httpServletRequest.getParameter("limit")).thenReturn("50");
		Mockito.when(httpServletRequest.getParameter("lastEdited")).thenReturn("");
		Mockito.when(httpServletRequest.getParameter(SearchHelper.BIRTH_DATE)).thenReturn("");
		Mockito.when(httpServletRequest.getParameter(SearchHelper.ZEIR_ID)).thenReturn("1234");
		Mockito.when(httpServletRequest.getParameter(SearchHelper.OPENSRP_ID)).thenReturn("4567");
		Mockito.when(httpServletRequest.getParameter(SearchHelper.SIM_PRINT_GUID)).thenReturn("91011");
		Mockito.when(httpServletRequest.getParameter(SearchHelper.INACTIVE)).thenReturn("false");
		Mockito.when(httpServletRequest.getParameter(SearchHelper.LOST_TO_FOLLOW_UP)).thenReturn("true");
		Mockito.when(httpServletRequest.getParameter(SearchHelper.NFC_CARD_IDENTIFIER)).thenReturn("nfc_card_identifier_1");
		SearchEntityWrapper searchEntityWrapper = SearchHelper.childSearchParamProcessor(httpServletRequest);

		Map<String, String> attributes = searchEntityWrapper.getClientSearchBean().getAttributes();
		Assert.assertEquals(3,  attributes.size());

		Map<String, String> identifiers = searchEntityWrapper.getClientSearchBean().getIdentifiers();
		Assert.assertEquals(4, identifiers.size());

		Assert.assertTrue(identifiers.containsKey(SearchHelper.ZEIR_ID));
		Assert.assertTrue(identifiers.containsKey("ZEIR_ID")); //check backward compatibility with upper case key
		Assert.assertTrue(identifiers.containsKey(SearchHelper.SIM_PRINT_GUID));

		Assert.assertTrue(attributes.containsKey(SearchHelper.INACTIVE));
		Assert.assertTrue(attributes.containsKey(SearchHelper.LOST_TO_FOLLOW_UP));
		Assert.assertTrue(attributes.containsKey("NFC_Card_Identifier"));

		Assert.assertEquals(identifiers.get(SearchHelper.ZEIR_ID), "1234");
		Assert.assertEquals(attributes.get("NFC_Card_Identifier"), "nfc_card_identifier_1");
	}

	@Test
	public void testSetCoreFiltersForJSONObjectWithIntegerLimitReturnsValue(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("limit", 50);
		try {
			int result =  SearchHelper.setCoreFilters(jsonObject, new ClientSearchBean());
			Assert.assertEquals(50,result);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testSetCoreFiltersForJSONObjectWithStringLimitReturnsValue(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("limit", "50");
		try {
			int result =  SearchHelper.setCoreFilters(jsonObject, new ClientSearchBean());
			Assert.assertEquals(50,result);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
