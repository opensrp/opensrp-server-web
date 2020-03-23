package org.opensrp.web.rest;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.service.EventService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.bean.EventSyncBean;
import org.opensrp.web.bean.Identifier;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
public class EventResourceTest extends BaseResourceTest<Event> {

    private final static String BASE_URL = "/rest/event";

    private String eventType = "Spray";

    private EventService eventService;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

    @Captor
    private ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

    @Captor
    private ArgumentCaptor<Boolean> booleanArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);

	private EventResource eventResource;


    public EventResourceTest() throws IOException {
        super();
    }

    @Before
    public void setUp() {
        eventService = mock(EventService.class);
        eventResource = webApplicationContext.getBean(EventResource.class);
        eventResource.setEventService(eventService);
		eventResource.setObjectMapper(mapper);
    }

    @Test
    public void testFindAllEventIds() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");

        Pair idsModel = Pair.of(expectedEventIdList, 1234l);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(null, false, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType?serverVersion=0", null, status().isOk());
        Identifier actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<Identifier>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), booleanArgumentCaptor.capture(), longArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertNull(stringArgumentCaptor.getValue());
        assertFalse(booleanArgumentCaptor.getValue());
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().intValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());

    }

    @Test
    public void testFindAllEventIdsByEventType() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");
        Pair<List<String>, Long> idsModel = Pair.of(expectedEventIdList, 1234l);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(eventType, false, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String parameter = AllConstants.Event.EVENT_TYPE + "=" + eventType + "&serverVersion=0";
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        Identifier actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<Identifier>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), booleanArgumentCaptor.capture(), longArgumentCaptor.capture() ,integerArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), eventType);
        assertFalse(booleanArgumentCaptor.getValue());
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().intValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());

    }

    @Test
    public void testFindAllDeletedEventIdsByEventType() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");
        Pair<List<String>, Long> idsModel = Pair.of(expectedEventIdList, 1234l);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(eventType, true, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String parameter = AllConstants.Event.EVENT_TYPE + "=" + eventType + "&is_deleted=" + true + "&serverVersion=0";
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        Identifier actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<Identifier>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), booleanArgumentCaptor.capture(), longArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), eventType);
        assertTrue(booleanArgumentCaptor.getValue());
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().intValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());

    }

    @Test
    public void testFindAllDeletedEventIds() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");
        Pair<List<String>, Long> idsModel = Pair.of(expectedEventIdList, 1234l);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(null, true, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String parameter = "is_deleted=" + true + "&serverVersion=0";
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        Identifier actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<Identifier>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), booleanArgumentCaptor.capture(), longArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertNull(stringArgumentCaptor.getValue());
        assertTrue(booleanArgumentCaptor.getValue());
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().longValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());

    }

	@Test
	public void testSyncClientsAndEventsByBaseEntityIds() throws Exception {

		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
				.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

		eventResource = spy(eventResource);

		String expectedEventString = "{\"identifiers\":{},\"baseEntityId\":\"5dd43b2e-a873-444b-b527-95c4b040a5bb\",\"locationId\":\"fb7ed5db-138d-4e6f-94d8-bc443b58dadb\",\"eventDate\":\"2020-02-14T03:00:00.000+03:00\",\"eventType\":\"Family Member Registration\",\"formSubmissionId\":\"a2fba8d2-42f5-4811-b982-57609f1815fe\",\"providerId\":\"unifiedchwone\",\"duration\":0,\"obs\":[{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"same_as_fam_name\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"same_as_fam_name\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"Baba\"],\"set\":[],\"formSubmissionField\":\"fam_name\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"43\"],\"set\":[],\"formSubmissionField\":\"age_calculated\",\"humanReadableValues\":[]},{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"dob_unknown\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"dob_unknown\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"1\"],\"set\":[],\"formSubmissionField\":\"wra\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"0\"],\"set\":[],\"formSubmissionField\":\"mra\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"160692AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"preg_1yr\",\"humanReadableValues\":[\"No\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"162558AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"disabilities\",\"humanReadableValues\":[\"No\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[null],\"set\":[],\"formSubmissionField\":\"is_primary_caregiver\",\"humanReadableValues\":[\"Yes\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"values\":[\"163096AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"service_provider\",\"humanReadableValues\":[\"Community IMCI\"]},{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"last_interacted_with\",\"parentCode\":\"\",\"values\":[\"1581697252432\"],\"set\":[],\"formSubmissionField\":\"last_interacted_with\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"start\",\"fieldCode\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"2020-02-14 19:19:58\"],\"set\":[],\"formSubmissionField\":\"start\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"end\",\"fieldCode\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"2020-02-14 19:20:52\"],\"set\":[],\"formSubmissionField\":\"end\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"deviceid\",\"fieldCode\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"359050095070928\"],\"set\":[],\"formSubmissionField\":\"deviceid\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"subscriberid\",\"fieldCode\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"639070028267663\"],\"set\":[],\"formSubmissionField\":\"subscriberid\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"simserial\",\"fieldCode\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"89254070000282676636\"],\"set\":[],\"formSubmissionField\":\"simserial\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"162849AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"wra\",\"humanReadableValues\":[]}],\"entityType\":\"ec_family_member\",\"version\":1581697252446,\"teamId\":\"de7d5dbe-6d21-4300-a72e-6eee14712f62\",\"team\":\"Madona\",\"dateCreated\":\"2020-02-14T19:21:21.295+03:00\",\"serverVersion\":1581697281295,\"clientApplicationVersion\":2,\"clientDatabaseVersion\":13,\"type\":\"Event\",\"id\":\"24032715-7d65-434d-b50a-0b96ec63996a\",\"revision\":\"v1\"}";
		Event expectedEvent = gson.fromJson(expectedEventString, new TypeToken<Event>() {}.getType());
		String expectedClientString = "{\"firstName\":\"Hilda\",\"middleName\":\"Wabera\",\"lastName\":\"Baba\",\"birthdate\":\"1977-01-01T03:00:00.000+03:00\",\"birthdateApprox\":true,\"deathdateApprox\":false,\"gender\":\"Female\",\"relationships\":{\"family\":[\"cf5d5fef-f120-4eb3-ab29-ed4d437e30c4\"]},\"baseEntityId\":\"5dd43b2e-a873-444b-b527-95c4b040a5bb\",\"identifiers\":{\"opensrp_id\":\"6468474\"},\"addresses\":[],\"attributes\":{\"age\":\"43\",\"id_avail\":\"[\\\"chk_none\\\"]\",\"Community_Leader\":\"[\\\"chk_political\\\",\\\"chk_traditional\\\",\\\"chk_religious\\\"]\",\"Health_Insurance_Type\":\"None\"},\"dateCreated\":\"2020-02-14T22:20:52.441+03:00\",\"dateEdited\":\"2020-02-14T19:21:21.291+03:00\",\"serverVersion\":1581697285557,\"clientApplicationVersion\":2,\"clientDatabaseVersion\":13,\"type\":\"Client\",\"id\":\"207bd5c7-788b-46b0-9635-2d7c4561fdd7\",\"revision\":\"v4\"}";
		Client expectedClient = gson.fromJson(expectedClientString, new TypeToken<Client>() {}.getType());
		EventSyncBean expectedEventSyncBean = new EventSyncBean();
		expectedEventSyncBean.setNoOfEvents(1);
		expectedEventSyncBean.setEvents(Collections.singletonList(expectedEvent));
		expectedEventSyncBean.setClients(Collections.singletonList(expectedClient));

		String expectedFamilyEventString = "{\"identifiers\":{},\"baseEntityId\":\"5dd43b2e-a873-444b-b527-95c4b040a5bb\",\"locationId\":\"fb7ed5db-138d-4e6f-94d8-bc443b58dadb\",\"eventDate\":\"2020-02-14T22:20:52.670+03:00\",\"eventType\":\"Update Family Member Relations\",\"formSubmissionId\":\"65523dec-0c65-42f9-a55a-ca0bba226fa8\",\"providerId\":\"unifiedchwone\",\"duration\":0,\"obs\":[{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"159635AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"\"],\"set\":[],\"formSubmissionField\":\"phone_number\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"159635AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"values\":[\"\"],\"set\":[],\"formSubmissionField\":\"other_phone_number\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"1712AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[null],\"set\":[],\"formSubmissionField\":\"highest_edu_level\",\"humanReadableValues\":[\"\"]}],\"entityType\":\"ec_family_member\",\"version\":1581697252674,\"teamId\":\"de7d5dbe-6d21-4300-a72e-6eee14712f62\",\"team\":\"Madona\",\"dateCreated\":\"2020-02-14T19:21:21.314+03:00\",\"serverVersion\":1581697281295,\"clientApplicationVersion\":2,\"clientDatabaseVersion\":13,\"type\":\"Event\",\"id\":\"7cbc9761-75e3-4047-9f9f-7a30bc290b99\",\"revision\":\"v1\"}";
		Event expectedFamilyEvent = gson.fromJson(expectedFamilyEventString, new TypeToken<Event>() {}.getType());
		String expectedFamilyClientString = "{\"firstName\":\"Hilda\",\"middleName\":\"Wabera\",\"lastName\":\"Baba\",\"birthdate\":\"1977-01-01T03:00:00.000+03:00\",\"birthdateApprox\":true,\"deathdateApprox\":false,\"gender\":\"Female\",\"relationships\":{\"family\":[\"cf5d5fef-f120-4eb3-ab29-ed4d437e30c4\"]},\"baseEntityId\":\"5dd43b2e-a873-444b-b527-95c4b040a5bb\",\"identifiers\":{\"opensrp_id\":\"6468474\"},\"addresses\":[],\"attributes\":{\"age\":\"43\",\"id_avail\":\"[\\\"chk_none\\\"]\",\"Community_Leader\":\"[\\\"chk_political\\\",\\\"chk_traditional\\\",\\\"chk_religious\\\"]\",\"Health_Insurance_Type\":\"None\"},\"dateCreated\":\"2020-02-14T22:20:52.441+03:00\",\"dateEdited\":\"2020-02-14T19:21:21.291+03:00\",\"serverVersion\":1581697285557,\"clientApplicationVersion\":2,\"clientDatabaseVersion\":13,\"type\":\"Client\",\"id\":\"207bd5c7-788b-46b0-9635-2d7c4561fdd7\",\"revision\":\"v4\"}";
		Client expectedFamilyClient = gson.fromJson(expectedFamilyClientString, new TypeToken<Client>() {}.getType());

		EventSyncBean expectedFamilyEventSyncBean = new EventSyncBean();
		expectedFamilyEventSyncBean.setNoOfEvents(1);
		expectedFamilyEventSyncBean.setEvents(Collections.singletonList(expectedFamilyEvent));
		expectedFamilyEventSyncBean.setClients(Collections.singletonList(expectedFamilyClient));

		String jsonObjectPayload = "{\"baseEntityIds\":[\"5dd43b2e-a873-444b-b527-95c4b040a5bb\"],\"withFamilyEvents\":true}";

		doReturn(expectedEventSyncBean).when(eventResource).sync(null, null, "5dd43b2e-a873-444b-b527-95c4b040a5bb", "0",
				null, null, null);
		doReturn(expectedFamilyEventSyncBean).when(eventResource).sync(null, null, "cf5d5fef-f120-4eb3-ab29-ed4d437e30c4",
				"0", null, null, null);

		ResponseEntity<String> clientEventsResponseEntity = eventResource
				.syncClientsAndEventsByBaseEntityIds(jsonObjectPayload);
		verify(eventResource).syncClientsAndEventsByBaseEntityIds(stringArgumentCaptor.capture());
		assertEquals(stringArgumentCaptor.getValue(), jsonObjectPayload);

		JSONObject clientEventsResponseObject = new JSONObject(clientEventsResponseEntity.getBody());
		int noOfEvents = (int) clientEventsResponseObject.get("no_of_events");
		assertEquals(2, noOfEvents);

		JSONArray eventsArray = clientEventsResponseObject.getJSONArray("events");
		assertEquals(2, eventsArray.length());

		JSONArray clientsArray = clientEventsResponseObject.getJSONArray("clients");
		assertEquals(2, clientsArray.length());

		ResponseEntity<String> emptyClientEventsResponseEntity = eventResource.syncClientsAndEventsByBaseEntityIds("");

		JSONObject errorObject = new JSONObject(emptyClientEventsResponseEntity.getBody());
		assertTrue(errorObject.has("msg"));
		assertTrue(errorObject.getString("msg").contains("Error occurred"));

	}


    @Override
    protected void assertListsAreSameIgnoringOrder(List<Event> expectedList, List<Event> actualList) {
        if (expectedList == null || actualList == null) {
            throw new AssertionError("One of the lists is null");
        }

        assertEquals(expectedList.size(), actualList.size());

        Set<String> expectedIds = new HashSet<>();
        for (Event event : expectedList) {
            expectedIds.add(event.getFormSubmissionId());
        }

        for (Event event : actualList) {
            assertTrue(expectedIds.contains(event.getFormSubmissionId()));
        }
    }

}
