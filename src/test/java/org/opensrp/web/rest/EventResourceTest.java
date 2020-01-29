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
import org.opensrp.domain.Event;
import org.opensrp.service.EventService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

        doReturn(expectedEventIdList).when(eventService).findAllIdsByEventType(null, null);

        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", null, status().isOk());
        List<String> actualEventIdList = new Gson().fromJson(actualEventIdString, new TypeToken<List<String>>(){}.getType());

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture());
        assertNull(stringArgumentCaptor.getValue());
        assertNull(dateArgumentCaptor.getValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));

    }

    @Test
    public void testFindAllEventIdsByEventType() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();

        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");

        doReturn(expectedEventIdList).when(eventService).findAllIdsByEventType(eventType, null);

        String parameter = AllConstants.Event.EVENT_TYPE + "=" + eventType;
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        List<String> actualEventIdList = new Gson().fromJson(actualEventIdString, new TypeToken<List<String>>(){}.getType());

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), eventType);
        assertNull(dateArgumentCaptor.getValue());

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));

    }

    @Test
    public void testFindAllDeletedEventIdsByEventType() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();

        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");

        Date dateDeleted = convertDate(dateDeletedString, ISO_DATE_TIME_FORMAT);

        doReturn(expectedEventIdList).when(eventService).findAllIdsByEventType(eventType, dateDeleted);

        String parameter = AllConstants.Event.EVENT_TYPE + "=" + eventType + "&" + EventResource.DATE_DELETED + "=" + dateDeletedString;
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        List<String> actualEventIdList = new Gson().fromJson(actualEventIdString, new TypeToken<List<String>>(){}.getType());

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), eventType);
        assertEquals(dateArgumentCaptor.getValue(), dateDeleted);

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));

    }

    @Test
    public void testFindAllDeletedEventIds() throws Exception {
        List<String> expectedEventIdList = new ArrayList<>();

        expectedEventIdList.add("event_1");
        expectedEventIdList.add("event_2");

        Date dateDeleted = convertDate(dateDeletedString, ISO_DATE_TIME_FORMAT);

        doReturn(expectedEventIdList).when(eventService).findAllIdsByEventType(null, dateDeleted);

        String parameter = EventResource.DATE_DELETED + "=" + dateDeletedString;
        String actualEventIdString = getResponseAsString(BASE_URL + "/findIdsByEventType", parameter, status().isOk());
        List<String> actualEventIdList = new Gson().fromJson(actualEventIdString, new TypeToken<List<String>>(){}.getType());

        verify(eventService).findAllIdsByEventType(stringArgumentCaptor.capture(), dateArgumentCaptor.capture());
        assertNull(stringArgumentCaptor.getValue());
        assertEquals(dateArgumentCaptor.getValue(), dateDeleted);

        assertNotNull(actualEventIdList);
        assertEquals(2, actualEventIdList.size());
        assertEquals(expectedEventIdList.get(0), actualEventIdList.get(0));
        assertEquals(expectedEventIdList.get(1), actualEventIdList.get(1));

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
