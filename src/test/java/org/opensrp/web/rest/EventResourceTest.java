package org.opensrp.web.rest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.opensrp.common.AllConstants;
import static org.opensrp.common.AllConstants.Event.EVENT_TYPE;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.opensrp.common.AllConstants.Event.LOCATION_ID;
import static org.opensrp.common.AllConstants.Event.TEAM;
import static org.opensrp.common.AllConstants.Event.TEAM_ID;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.search.EventSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.bean.EventSyncBean;
import org.opensrp.web.bean.Identifier;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.servlet.http.HttpServletRequest;

public class EventResourceTest extends BaseResourceTest<Event> {

    private final static String BASE_URL = "/rest/event";

    private String eventType = "Spray";

    private EventService eventService;

	private ClientService clientService;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

    @Captor
    private ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

    @Captor
    private ArgumentCaptor<Boolean> booleanArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
    
    @Captor
    private ArgumentCaptor<EventSearchBean> eventSearchBeanArgumentCaptor = ArgumentCaptor.forClass(EventSearchBean.class);
    
    @Captor
    private ArgumentCaptor<Client> clientArgumentCaptor = ArgumentCaptor.forClass(Client.class);
    
    @Captor
    private ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
    
	private EventResource eventResource;
	
 	private String ADD_REQUEST_PAYLOAD  = "{\n"
			+ "\t\"clients\": [{\"birthdate\":\"1970-01-01T05:00:00.000Z\",\"firstName\":\"Test\",\"gender\":\"Male\",\"lastName\":\"User\",\"baseEntityId\":\"502f5f2d-5a06-4f71-8f8a-b19a846b9a93\"}],\n"
			+ "\t\"events\": [{\"baseEntityId\":\"502f5f2d-5a06-4f71-8f8a-b19a846b9a93\",\"entityType\":\"ec_family\",\"eventDate\":\"2020-05-02T23:26:21.685Z\"}]\n"
			+ "}";
	
	private String POST_SYNC_REQUEST = "{\n"
			+ "\t\"providerId\": \"test\",\n"
			+ "\t\"locationId\": \"test\",\n"
			+ "\t\"baseEntityId\": \"test\",\n"
			+ "\t\"serverVersion\": 15421904649873,\n"
			+ "\t\"team\": \"test\",\n"
			+ "\t\"teamId\": \"test\",\n"
			+ "\t\"limit\": 5\n"
			+ "}";

    public EventResourceTest() throws IOException {
        super();
    }

    @Before
    public void setUp() {
        eventService = mock(EventService.class);
        clientService = mock(ClientService.class);
        eventResource = webApplicationContext.getBean(EventResource.class);
        eventResource.setEventService(eventService);
        eventResource.setClientService(clientService);
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

        String parameter = EVENT_TYPE + "=" + eventType + "&serverVersion=0";
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

        String parameter = EVENT_TYPE + "=" + eventType + "&is_deleted=" + true + "&serverVersion=0";
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
    
    @Test
	public void testGetByUniqueId() {
    	Event actualEvent = createEvent();
    	when(eventService.find(anyString())).thenReturn(actualEvent);
    	Event expectedEvent = eventResource.getByUniqueId("123");
    	assertEquals(actualEvent.getBaseEntityId(),expectedEvent.getBaseEntityId());
    	assertEquals(actualEvent.getId(), expectedEvent.getId());
    }

	@Test
	public void testFilter() {
		List<Event> expected = new ArrayList<>();
		expected.add(createEvent());

		when(eventService.findEventsByDynamicQuery(anyString())).thenReturn(expected);
		List<Event> actual = eventResource.filter("");
		assertEquals(actual.size(),expected.size());
		assertEquals(actual.get(0).getId(),expected.get(0).getId());
	}
	
	@Test
	public void testSearch() throws Exception {
    	List<Event> expected = new ArrayList<>();
    	expected.add(createEvent());
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		when(eventService.findEventsBy(any(EventSearchBean.class))).thenReturn(expected);
		List<Event> events = eventResource.search(httpServletRequest);
		assertEquals(events.size(),expected.size());
	}
	
	@Test
	public void testSave() throws Exception {
	    Client client = createClient();
	    Event event = createEvent();
		doReturn(client).when(clientService).addorUpdate(any(Client.class));
		doReturn(event).when(eventService).processOutOfArea(any(Event.class));
		doReturn(event).when(eventService).addorUpdateEvent(any(Event.class));
		postRequestWithJsonContent(BASE_URL + "/add", ADD_REQUEST_PAYLOAD, status().isCreated());
		verify(clientService).addorUpdate(clientArgumentCaptor.capture());
		assertEquals(clientArgumentCaptor.getValue().getFirstName(), "Test");
		verify(eventService).addorUpdateEvent(eventArgumentCaptor.capture());
		assertEquals(eventArgumentCaptor.getValue().getEventType(), "Family Member Registration");
	}

	@Test
	public void testGetAll() throws Exception {
		List<Event> expectedEvents = new ArrayList<>();
		expectedEvents.add(createEvent());
		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		
		doReturn(expectedEvents).when(eventService).findEvents(any(EventSearchBean.class), anyString(), anyString(), any(int.class));
		doReturn(expectedClients).when(clientService).findByFieldValue(anyString(),anyList());
		doReturn(createClient()).when(clientService).getByBaseEntityId(anyString());

		String parameter = SERVER_VERSIOIN + "=15421904649873";
		String response = getResponseAsString(BASE_URL + "/getAll", parameter, status().isOk());
		JsonNode actualObj = mapper.readTree(response);
		verify(eventService).findEvents(eventSearchBeanArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture() ,integerArgumentCaptor.capture());
		assertEquals(integerArgumentCaptor.getValue(), new Integer(25));
		assertEquals(stringArgumentCaptor.getAllValues().get(0), SERVER_VERSIOIN);
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "asc");
		assertEquals(actualObj.size(),3);
		assertEquals(actualObj.get("clients").size(),1);
		assertEquals(actualObj.get("events").size(),1);
    }
    
    @Test
    public void testGetSync() throws Exception{
	    List<Event> expectedEvents = new ArrayList<>();
	    expectedEvents.add(createEvent());
	    List<Client> expectedClients = new ArrayList<>();
	    expectedClients.add(createClient());

	    doReturn(expectedEvents).when(eventService).findEvents(any(EventSearchBean.class), anyString(), anyString(), any(int.class));
	    doReturn(expectedClients).when(clientService).findByFieldValue(anyString(),anyList());
	    doReturn(createClient()).when(clientService).getByBaseEntityId(anyString());

	    String parameter = PROVIDER_ID+"=providerId&"+LOCATION_ID+"=locationId&"+BASE_ENTITY_ID+"=base-entity-id&"+SERVER_VERSIOIN+"=15421904649873&"+TEAM+"=team&"+TEAM_ID+"=team_id";
	    String response = getResponseAsString(BASE_URL + "/sync", parameter, status().isOk());
	    JsonNode actualObj = mapper.readTree(response);
	    verify(eventService).findEvents(eventSearchBeanArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture() ,integerArgumentCaptor.capture());
	    assertEquals(integerArgumentCaptor.getValue(), new Integer(25));
	    assertEquals(stringArgumentCaptor.getAllValues().get(0), SERVER_VERSIOIN);
	    assertEquals(stringArgumentCaptor.getAllValues().get(1), "asc");
	    assertEquals(actualObj.size(),3);
	    assertEquals(actualObj.get("clients").size(),1);
	    assertEquals(actualObj.get("events").size(),1);
    }

	@Test
	public void testPostSync() throws Exception {
		List<Event> expectedEvents = new ArrayList<>();
		expectedEvents.add(createEvent());
		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());

		doReturn(expectedEvents).when(eventService).findEvents(any(EventSearchBean.class), anyString(), anyString(), any(int.class));
		doReturn(expectedClients).when(clientService).findByFieldValue(anyString(),anyList());
		doReturn(createClient()).when(clientService).getByBaseEntityId(anyString());

		postRequestWithJsonContent(BASE_URL + "/sync", POST_SYNC_REQUEST, status().isOk());
		verify(eventService).findEvents(eventSearchBeanArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture() ,integerArgumentCaptor.capture());
		assertEquals(integerArgumentCaptor.getValue(), new Integer(5));
		assertEquals(stringArgumentCaptor.getAllValues().get(0), SERVER_VERSIOIN);
		assertEquals(stringArgumentCaptor.getAllValues().get(1), "asc");
	}


	private Event createEvent() {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
				.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
		String expectedEventString = "{\"identifiers\":{},\"baseEntityId\":\"5dd43b2e-a873-444b-b527-95c4b040a5bb\",\"locationId\":\"fb7ed5db-138d-4e6f-94d8-bc443b58dadb\",\"eventDate\":\"2020-02-14T03:00:00.000+03:00\",\"eventType\":\"Family Member Registration\",\"formSubmissionId\":\"a2fba8d2-42f5-4811-b982-57609f1815fe\",\"providerId\":\"unifiedchwone\",\"duration\":0,\"obs\":[{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"same_as_fam_name\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"same_as_fam_name\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"Baba\"],\"set\":[],\"formSubmissionField\":\"fam_name\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"43\"],\"set\":[],\"formSubmissionField\":\"age_calculated\",\"humanReadableValues\":[]},{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"dob_unknown\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"dob_unknown\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"1\"],\"set\":[],\"formSubmissionField\":\"wra\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[\"0\"],\"set\":[],\"formSubmissionField\":\"mra\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"160692AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"preg_1yr\",\"humanReadableValues\":[\"No\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"162558AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"disabilities\",\"humanReadableValues\":[\"No\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"\",\"parentCode\":\"\",\"values\":[null],\"set\":[],\"formSubmissionField\":\"is_primary_caregiver\",\"humanReadableValues\":[\"Yes\"]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"1542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"values\":[\"163096AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"],\"set\":[],\"formSubmissionField\":\"service_provider\",\"humanReadableValues\":[\"Community IMCI\"]},{\"fieldType\":\"formsubmissionField\",\"fieldDataType\":\"text\",\"fieldCode\":\"last_interacted_with\",\"parentCode\":\"\",\"values\":[\"1581697252432\"],\"set\":[],\"formSubmissionField\":\"last_interacted_with\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"start\",\"fieldCode\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"2020-02-14 19:19:58\"],\"set\":[],\"formSubmissionField\":\"start\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"end\",\"fieldCode\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"2020-02-14 19:20:52\"],\"set\":[],\"formSubmissionField\":\"end\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"deviceid\",\"fieldCode\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"359050095070928\"],\"set\":[],\"formSubmissionField\":\"deviceid\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"subscriberid\",\"fieldCode\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"639070028267663\"],\"set\":[],\"formSubmissionField\":\"subscriberid\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"simserial\",\"fieldCode\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"89254070000282676636\"],\"set\":[],\"formSubmissionField\":\"simserial\",\"humanReadableValues\":[]},{\"fieldType\":\"concept\",\"fieldDataType\":\"text\",\"fieldCode\":\"162849AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"parentCode\":\"\",\"values\":[\"true\"],\"set\":[],\"formSubmissionField\":\"wra\",\"humanReadableValues\":[]}],\"entityType\":\"ec_family_member\",\"version\":1581697252446,\"teamId\":\"de7d5dbe-6d21-4300-a72e-6eee14712f62\",\"team\":\"Madona\",\"dateCreated\":\"2020-02-14T19:21:21.295+03:00\",\"serverVersion\":1581697281295,\"clientApplicationVersion\":2,\"clientDatabaseVersion\":13,\"type\":\"Event\",\"id\":\"24032715-7d65-434d-b50a-0b96ec63996a\",\"revision\":\"v1\"}";
		return gson.fromJson(expectedEventString, new TypeToken<Event>() {}.getType());
	}

	private Client createClient() {
		Client client = new Client("base-entity-id");
		client.setFirstName("Test");
		client.setLastName("User");
		client.setId("1");
		client.setDateCreated(new DateTime());
		client.setDateEdited(new DateTime());
		return client;
	}

}
