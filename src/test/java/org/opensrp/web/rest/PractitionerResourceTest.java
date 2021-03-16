package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensrp.search.PractitionerSearchBean;
import org.opensrp.service.PractitionerService;
import org.smartregister.domain.Practitioner;
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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class PractitionerResourceTest extends BaseResourceTest<Practitioner> {

    private final static String BASE_URL = "/rest/practitioner/";

    private final static String DELETE_ENDPOINT = "delete/";

    private PractitionerService practitionerService;

    private final ArgumentCaptor<Practitioner> argumentCaptor = ArgumentCaptor.forClass(Practitioner.class);

    private final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    private final String practitionerJson = "{\"identifier\":\"practitoner-1-identifier\",\"active\":true,\"name\":\"Practitioner\",\"userId\":\"user1\",\"username\":\"Practioner1\"}";

    @Before
    public void setUp() {
        practitionerService = mock(PractitionerService.class);
        PractitionerResource practitionerResource = webApplicationContext.getBean(PractitionerResource.class);
        practitionerResource.setPractitionerService(practitionerService);
    }

    @Test
    public void testGetPractitionersSHouldReturnAllPractitioners() throws Exception {
        List<Practitioner> expectedPractitoiners = new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitoiners.add(expectedPractitioner);

        expectedPractitioner = initTestPractitioner2();
        expectedPractitoiners.add(expectedPractitioner);

        doReturn(expectedPractitoiners).when(practitionerService).getAllPractitioners(any(PractitionerSearchBean.class));

        String actualPractitionersString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<Practitioner> actualPractitioners = new Gson()
                .fromJson(actualPractitionersString, new TypeToken<List<Practitioner>>() {

                }.getType());

        assertListsAreSameIgnoringOrder(actualPractitioners, expectedPractitoiners);
    }

    @Test
    public void testGetPractitionerByUniqueIdShouldReturnCorrectPractititoner() throws Exception {
        List<Practitioner> expectedPractitoiners = new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitoiners.add(expectedPractitioner);

        List<String> practitionerIdList = new ArrayList<>();
        practitionerIdList.add(expectedPractitioner.getIdentifier());

        doReturn(expectedPractitioner).when(practitionerService).getPractitioner(anyString());

        String actualPractitionersString = getResponseAsString(BASE_URL + "practitoner-1-identifier", null,
                MockMvcResultMatchers.status().isOk());
        Practitioner actualPractitioner = new Gson().fromJson(actualPractitionersString, new TypeToken<Practitioner>() {

        }.getType());

        assertNotNull(actualPractitioner);
        assertEquals(actualPractitioner.getIdentifier(), expectedPractitioner.getIdentifier());
        assertEquals(actualPractitioner.getUserId(), expectedPractitioner.getUserId());
        assertEquals(actualPractitioner.getName(), expectedPractitioner.getName());
        assertEquals(actualPractitioner.getUsername(), expectedPractitioner.getUsername());
        assertEquals(actualPractitioner.getActive(), expectedPractitioner.getActive());
    }


    @Test
    public void testGetPractitionerByUniqueIdSWithBlankIdentifierShouldReturnAnError() throws Exception {
        String actualPractitionersString = getResponseAsString(BASE_URL + " ", null,
                MockMvcResultMatchers.status().isBadRequest());
        String  actualPractitioner = new Gson().fromJson(actualPractitionersString, new TypeToken<String>() {}.getType());
        assertNotNull(actualPractitioner);
        assertEquals("Practitioner Id is required", actualPractitioner);
    }
    @Test
    public void testGetPractitionerByUserIdShouldReturnCorrectPractititoner() throws Exception {
        List<Practitioner> expectedPractitioners = new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitioners.add(expectedPractitioner);

        List<String> practitionerIdList = new ArrayList<>();
        practitionerIdList.add(expectedPractitioner.getIdentifier());

        doReturn(expectedPractitioner).when(practitionerService).getPractitionerByUserId(anyString());

        String actualPractitionersString = getResponseAsString(BASE_URL + "/user/" + "user1", null,
                MockMvcResultMatchers.status().isOk());
        Practitioner actualPractitioner = new Gson().fromJson(actualPractitionersString, new TypeToken<Practitioner>() {

        }.getType());

        assertNotNull(actualPractitioner);
        assertEquals(actualPractitioner.getIdentifier(), expectedPractitioner.getIdentifier());
        assertEquals(actualPractitioner.getUserId(), expectedPractitioner.getUserId());
        assertEquals(actualPractitioner.getName(), expectedPractitioner.getName());
        assertEquals(actualPractitioner.getUsername(), expectedPractitioner.getUsername());
        assertEquals(actualPractitioner.getActive(), expectedPractitioner.getActive());
    }

    @Test
    public void testGetPractitionerByUserIdWithBlankIdentifierShouldReturnAnError() throws Exception {
        String actualPractitionersString = getResponseAsString(BASE_URL + "/user/" + " ", null,
                MockMvcResultMatchers.status().isBadRequest());
        String  actualPractitioner = new Gson().fromJson(actualPractitionersString, new TypeToken<String>() {}.getType());
        assertNotNull(actualPractitioner);
        assertEquals("The User Id is required", actualPractitioner);
    }

    @Test
    public void testCreateShouldCreateNewPractitionerResource() throws Exception {
        doReturn(new Practitioner()).when(practitionerService).addOrUpdatePractitioner(any());

        Practitioner expectedPractitioner = initTestPractitioner1();

        postRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitioner.getIdentifier());

    }

    @Test
    public void testUpdateShouldUpdateExistingPractitionerResource() throws Exception {
        Practitioner expectedPractitioner = initTestPractitioner1();

        String practitionerJson = new Gson().toJson(expectedPractitioner, new TypeToken<Practitioner>() {

        }.getType());
        putRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isCreated());

        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitioner.getIdentifier());
    }

    @Test
    public void testDeleteShouldDeleteExistingPractitionerResource() throws Exception {

        deleteRequestWithParams(BASE_URL + DELETE_ENDPOINT + "practitioner-id", null,
                MockMvcResultMatchers.status().isNoContent());

        verify(practitionerService).deletePractitioner(stringArgumentCaptor.capture());
        assertEquals(stringArgumentCaptor.getValue(), "practitioner-id");
    }

    @Test
    public void testCreateWithInternalError() throws Exception {
        doThrow(new IllegalArgumentException()).when(practitionerService).addOrUpdatePractitioner(any());
        postRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerService);
    }

    @Test
    public void testCreateWithJsonSyntaxException() throws Exception {
        doThrow(new JsonSyntaxException("Unable to parse JSON")).when(practitionerService)
                .addOrUpdatePractitioner(any());
        postRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerService);
    }

    @Test
    public void testUpdateWithInternalError() throws Exception {
        doThrow(new IllegalArgumentException()).when(practitionerService).addOrUpdatePractitioner(any());
        putRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerService);
    }

    @Test
    public void testUpdateWithJsonSyntaxException() throws Exception {
        doThrow(new JsonSyntaxException("Unable to parse JSON")).when(practitionerService)
                .addOrUpdatePractitioner(any());
        putRequestWithJsonContent(BASE_URL, practitionerJson, MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        verifyNoMoreInteractions(practitionerService);
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

    @Test
    public void testBatchSaveShouldCreateNewPractitioner() throws Exception {
        doReturn(new Practitioner()).when(practitionerService).addOrUpdatePractitioner(any());
        Practitioner expectedPractitioner = initTestPractitioner1();
        postRequestWithJsonContentAndReturnString(BASE_URL + "add" , "[" + practitionerJson + "]", MockMvcResultMatchers.status().isCreated());
        verify(practitionerService).addOrUpdatePractitioner(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getIdentifier(), expectedPractitioner.getIdentifier());
    }

    @Test
    public void testBatchSaveWithJsonSyntaxException() throws Exception {
        doThrow(new JsonSyntaxException("Unable to parse JSON")).when(practitionerService).addOrUpdatePractitioner(any());
        postRequestWithJsonContent(BASE_URL + "add", "{\"nothing\": \"works\"}", MockMvcResultMatchers.status().isBadRequest());
        verify(practitionerService, atLeast(0)).addOrUpdatePractitioner(argumentCaptor.capture());
    }

    private Practitioner initTestPractitioner1() {
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitoner-1-identifier");
        practitioner.setActive(true);
        practitioner.setName("Practitioner");
        practitioner.setUsername("Practioner1");
        practitioner.setUserId("user1");
        return practitioner;
    }

    private Practitioner initTestPractitioner2() {
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitoner-2-identifier");
        practitioner.setActive(false);
        practitioner.setName("Second Practitioner");
        practitioner.setUsername("Practioner2");
        practitioner.setUserId("user2");
        return practitioner;
    }

}
