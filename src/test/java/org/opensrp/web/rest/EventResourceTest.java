package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.AllIdsModel;
import org.opensrp.domain.Event;
import org.opensrp.service.EventService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

public class EventResourceTest extends BaseResourceTest<Event> {

    private final static String BASE_URL = "/rest/event";

    private String dateDeletedString = "2000-10-31T01:30:00.000-05:00";

    private String eventType = "Spray";

    private EventService eventService;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    @Captor
    private ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

    @Captor
    private ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);

    public EventResourceTest() throws IOException {
        super();
    }

    @Before
    public void setUp() {
        eventService = mock(EventService.class);
        EventResource eventResource = webApplicationContext.getBean(EventResource.class);
        eventResource.setEventService(eventService);
    }

    @Test
    public void testFindAllEventIds() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");

        AllIdsModel idsModel = new AllIdsModel();
        idsModel.setIdentifiers(expectedEventIdList);
        idsModel.setLastServerVersion(1234l);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(null, null, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType?serverVersion=0", null, status().isOk());
        AllIdsModel actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<AllIdsModel>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture(), longArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertNull(stringArgumentCaptor.getValue());
        assertNull(dateArgumentCaptor.getValue());
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().intValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getLastServerVersion(), actualIdModels.getLastServerVersion());

    }

    @Test
    public void testFindAllEventIdsByEventType() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");
        AllIdsModel idsModel = new AllIdsModel();
        idsModel.setIdentifiers(expectedEventIdList);
        idsModel.setLastServerVersion(1234l);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(eventType, null, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String parameter = AllConstants.Event.EVENT_TYPE + "=" + eventType + "&serverVersion=0";
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        AllIdsModel actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<AllIdsModel>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture(), longArgumentCaptor.capture() ,integerArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), eventType);
        assertNull(dateArgumentCaptor.getValue());
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().intValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getLastServerVersion(), actualIdModels.getLastServerVersion());

    }

    @Test
    public void testFindAllDeletedEventIdsByEventType() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");

        AllIdsModel idsModel = new AllIdsModel();
        idsModel.setIdentifiers(expectedEventIdList);
        idsModel.setLastServerVersion(1234l);

        Date dateDeleted = convertDate(dateDeletedString, ISO_DATE_TIME_FORMAT);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(eventType, dateDeleted, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String parameter = AllConstants.Event.EVENT_TYPE + "=" + eventType + "&" + EventResource.DATE_DELETED + "=" + dateDeletedString + "&serverVersion=0";
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        AllIdsModel actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<AllIdsModel>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture(), longArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), eventType);
        assertEquals(dateArgumentCaptor.getValue(), dateDeleted);
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().intValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getLastServerVersion(), actualIdModels.getLastServerVersion());

    }

    @Test
    public void testFindAllDeletedEventIds() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();
        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");
        AllIdsModel idsModel = new AllIdsModel();
        idsModel.setIdentifiers(expectedEventIdList);
        idsModel.setLastServerVersion(1234l);

        Date dateDeleted = convertDate(dateDeletedString, ISO_DATE_TIME_FORMAT);

        doReturn(idsModel).when(eventService).findAllIdsByEventType(null, dateDeleted, 0l, DEFAULT_GET_ALL_IDS_LIMIT);

        String parameter = EventResource.DATE_DELETED + "=" + dateDeletedString + "&serverVersion=0";
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        AllIdsModel actualIdModels = new Gson().fromJson(actualEventIdString, new TypeToken<AllIdsModel>(){}.getType());
        List<String> actualEventIdList = actualIdModels.getIdentifiers();

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture(), longArgumentCaptor.capture(), integerArgumentCaptor.capture());
        assertNull(stringArgumentCaptor.getValue());
        assertEquals(dateArgumentCaptor.getValue(), dateDeleted);
        assertEquals(0l, longArgumentCaptor.getValue().longValue());
        assertEquals(DEFAULT_GET_ALL_IDS_LIMIT, integerArgumentCaptor.getValue().longValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));
        assertEquals(idsModel.getLastServerVersion(), actualIdModels.getLastServerVersion());

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
