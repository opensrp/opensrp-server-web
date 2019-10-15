package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensrp.domain.Practitioner;
import org.opensrp.service.PractitionerService;
import org.springframework.test.web.server.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PractitionerResourceTest extends BaseResourceTest<Practitioner> {

    private final static String BASE_URL = "/rest/practitioner/";

    private final static String DELETE_ENDPOINT = "delete/";

    private PractitionerService practitionerService;

    private ArgumentCaptor<Practitioner> argumentCaptor = ArgumentCaptor.forClass(Practitioner.class);

    private ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    private final String practitionerJson = "{\"identifier\":\"practitoner-1-identifier\",\"active\":true,\"name\":\"Practitioner\",\"userId\":\"user1\",\"username\":\"Practioner1\"}";

    @Before
    public void setUp() {
        practitionerService = mock(PractitionerService.class);
        PractitionerResource practitionerResource = webApplicationContext.getBean(PractitionerResource.class);
        practitionerResource.setPractitionerService(practitionerService);
    }

    @Test
    public void testGetPractitionersSHouldReturnAllPractitioners() throws Exception {
        List<Practitioner> expectedPractitoiners =  new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitoiners.add(expectedPractitioner);

        expectedPractitioner = initTestPractitioner2();
        expectedPractitoiners.add(expectedPractitioner);

        doReturn(expectedPractitoiners).when(practitionerService).getAllPractitioners();

        String actualPractitionersString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<Practitioner> actualPractitioners = new Gson().fromJson(actualPractitionersString, new TypeToken<List<Practitioner>>(){}.getType());

        assertListsAreSameIgnoringOrder(actualPractitioners, expectedPractitoiners);
    }

    @Test
    public void testGetPractitionerByUniqueIdShouldReturnCorrectPractititoner() throws Exception {
        List<Practitioner> expectedPractitoiners =  new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitoiners.add(expectedPractitioner);

        List<String> practitionerIdList = new ArrayList<>();
        practitionerIdList.add(expectedPractitioner.getIdentifier());

        doReturn(expectedPractitioner).when(practitionerService).getPractitioner(anyString());

        String actualPractitionersString = getResponseAsString(BASE_URL + "practitoner-1-identifier", null,
                MockMvcResultMatchers.status().isOk());
        Practitioner actualPractitioner = new Gson().fromJson(actualPractitionersString, new TypeToken<Practitioner>(){}.getType());

        assertNotNull(actualPractitioner);
        assertEquals(actualPractitioner.getIdentifier(), expectedPractitioner.getIdentifier());
        assertEquals(actualPractitioner.getUserId(), expectedPractitioner.getUserId());
        assertEquals(actualPractitioner.getName(), expectedPractitioner.getName());
        assertEquals(actualPractitioner.getUserName(), expectedPractitioner.getUserName());
        assertEquals(actualPractitioner.getActive(), expectedPractitioner.getActive());
    }

    @Test
    public void testCreateShouldCreateNewPractitionerResource() throws Exception {
        doReturn(new Practitioner()).when(practitionerService).addOrUpdatePractitioner((Practitioner) any());

        Practitioner expectedPractitioner = initTestPractitioner1();

        postRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitioner.getIdentifier());

    }

    @Test
    public void testUpdateShouldUpdateExistingPractitionerResource() throws Exception {
        Practitioner expectedPractitioner = initTestPractitioner1();

        String practitionerJson = new Gson().toJson(expectedPractitioner, new TypeToken<Practitioner>(){}.getType());
        putRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitioner.getIdentifier());
    }

    @Test
    public void testDeleteShouldDeleteExistingPractitionerResource() throws Exception {

        deleteRequestWithJsonContent(BASE_URL + DELETE_ENDPOINT + "practitioner-id", null, MockMvcResultMatchers.status().isNoContent());

        verify(practitionerService).deletePractitioner(stringArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), "practitioner-id");
    }

    @Override
    protected void assertListsAreSameIgnoringOrder(List<Practitioner> expectedList, List<Practitioner> actualList) {
        if (expectedList == null || actualList == null) {
            throw new AssertionError("One of the lists is null");
        }

        assertEquals(expectedList.size(), actualList.size());

        Set<String> expectedIds = new HashSet<>();
        for (Practitioner practitioner : expectedList) {
            expectedIds.add(practitioner.getIdentifier());
        }

        for (Practitioner practitioner : actualList) {
            assertTrue(expectedIds.contains(practitioner.getIdentifier()));
        }
    }

    private Practitioner initTestPractitioner1(){
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitoner-1-identifier");
        practitioner.setActive(true);
        practitioner.setName("Practitioner");
        practitioner.setUserName("Practioner1");
        practitioner.setUserId("user1");
        return practitioner;
    }

    private Practitioner initTestPractitioner2(){
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitoner-2-identifier");
        practitioner.setActive(false);
        practitioner.setName("Second Practitioner");
        practitioner.setUserName("Practioner2");
        practitioner.setUserId("user2");
        return practitioner;
    }

}
