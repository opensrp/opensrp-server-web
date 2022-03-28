package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opensrp.search.PractitionerSearchBean;
import org.opensrp.service.PractitionerService;
import org.smartregister.domain.Practitioner;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.server.MvcResult;
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
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;

public class PractitionerResourceTest extends BaseResourceTest<Practitioner> {

    private final static String BASE_URL = "/rest/practitioner/";

    private final static String DELETE_ENDPOINT = "delete/";

    private PractitionerService practitionerService;

    private final ArgumentCaptor<Practitioner> argumentCaptor = ArgumentCaptor.forClass(Practitioner.class);

    private final ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

    private final String practitionerJson = "{\"identifier\":\"practitoner-1-identifier\",\"active\":true,\"name\":\"Practitioner\",\"userId\":\"user1\",\"username\":\"Practioner1\"}";

    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HHmm.ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    @Before
    public void setUp() {
        practitionerService = mock(PractitionerService.class);
        PractitionerResource practitionerResource = webApplicationContext.getBean(PractitionerResource.class);
        practitionerResource.setPractitionerService(practitionerService);
    }

    @Test
    public void testGetPractitionersShouldReturnAllPractitioners() throws Exception {
        List<Practitioner> expectedPractitioners = new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitioners.add(expectedPractitioner);

        expectedPractitioner = initTestPractitioner2();
        expectedPractitioners.add(expectedPractitioner);

        doReturn(expectedPractitioners).when(practitionerService).getAllPractitioners(any(PractitionerSearchBean.class));

        String actualPractitionersString = getResponseAsString(BASE_URL, null, MockMvcResultMatchers.status().isOk());
        List<Practitioner> actualPractitioners = gson.fromJson(actualPractitionersString, new TypeToken<List<Practitioner>>() {

                }.getType());

        assertListsAreSameIgnoringOrder(actualPractitioners, expectedPractitioners);
    }

    @Test
    public void testGetPractitionerByUniqueIdShouldReturnCorrectPractitioner() throws Exception {
        List<Practitioner> expectedPractitioners = new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitioners.add(expectedPractitioner);

        List<String> practitionerIdList = new ArrayList<>();
        practitionerIdList.add(expectedPractitioner.getIdentifier());

        doReturn(expectedPractitioner).when(practitionerService).getPractitioner(anyString());

        String actualPractitionersString = getResponseAsString(BASE_URL + "practitoner-1-identifier", null,
                MockMvcResultMatchers.status().isOk());
        Practitioner actualPractitioner = gson.fromJson(actualPractitionersString, new TypeToken<Practitioner>() {

        }.getType());

        assertNotNull(actualPractitioner);
        assertEquals(actualPractitioner.getIdentifier(), expectedPractitioner.getIdentifier());
        assertEquals(actualPractitioner.getUserId(), expectedPractitioner.getUserId());
        assertEquals(actualPractitioner.getName(), expectedPractitioner.getName());
        assertEquals(actualPractitioner.getUsername(), expectedPractitioner.getUsername());
        assertEquals(actualPractitioner.getActive(), expectedPractitioner.getActive());
        assertEquals(actualPractitioner.getDateCreated(), expectedPractitioner.getDateCreated());
        assertEquals(actualPractitioner.getDateEdited(), expectedPractitioner.getDateEdited());
        assertEquals(actualPractitioner.getServerVersion(), expectedPractitioner.getServerVersion());
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
    public void testGetPractitionerByUserIdShouldReturnCorrectPractitioner() throws Exception {
        List<Practitioner> expectedPractitioners = new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitioners.add(expectedPractitioner);

        List<String> practitionerIdList = new ArrayList<>();
        practitionerIdList.add(expectedPractitioner.getIdentifier());

        doReturn(expectedPractitioner).when(practitionerService).getPractitionerByUserId(anyString());

        String actualPractitionersString = getResponseAsString(BASE_URL + "/user/" + "user1", null,
                MockMvcResultMatchers.status().isOk());
        Practitioner actualPractitioner = gson.fromJson(actualPractitionersString, new TypeToken<Practitioner>() {

        }.getType());

        assertNotNull(actualPractitioner);
        assertEquals(actualPractitioner.getIdentifier(), expectedPractitioner.getIdentifier());
        assertEquals(actualPractitioner.getUserId(), expectedPractitioner.getUserId());
        assertEquals(actualPractitioner.getName(), expectedPractitioner.getName());
        assertEquals(actualPractitioner.getUsername(), expectedPractitioner.getUsername());
        assertEquals(actualPractitioner.getActive(), expectedPractitioner.getActive());
        assertEquals(actualPractitioner.getDateCreated(), expectedPractitioner.getDateCreated());
        assertEquals(actualPractitioner.getDateEdited(), expectedPractitioner.getDateEdited());
        assertEquals(actualPractitioner.getServerVersion(), expectedPractitioner.getServerVersion());
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

        String practitionerJson = gson.toJson(expectedPractitioner, new TypeToken<Practitioner>() {

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

    @Test
    public void testGetPractitionersByPractitionerRoleIdentifierAndCode() throws Exception {
        List<Practitioner> expectedPractitioners = new ArrayList<>();

        Practitioner expectedPractitioner = initTestPractitioner1();
        expectedPractitioners.add(expectedPractitioner);

        doReturn(expectedPractitioners).when(practitionerService)
                .getAssignedPractitionersByIdentifierAndCode("test", "testCode");

        String actualPractitionersString = getResponseAsString(
                BASE_URL + "/report-to?practitionerIdentifier=test&code=testCode", null,
                MockMvcResultMatchers.status().isOk());
        List<Practitioner> actualPractitioners = gson.fromJson(actualPractitionersString, new TypeToken<List<Practitioner>>() {

                }.getType());

        assertNotNull(actualPractitioners);
        assertEquals(actualPractitioners.get(0).getIdentifier(), expectedPractitioner.getIdentifier());
        assertEquals(actualPractitioners.get(0).getUserId(), expectedPractitioner.getUserId());
        assertEquals(actualPractitioners.get(0).getName(), expectedPractitioner.getName());
        assertEquals(actualPractitioners.get(0).getUsername(), expectedPractitioner.getUsername());
        assertEquals(actualPractitioners.get(0).getActive(), expectedPractitioner.getActive());
        assertEquals(actualPractitioners.get(0).getDateCreated(), expectedPractitioner.getDateCreated());
        assertEquals(actualPractitioners.get(0).getDateEdited(), expectedPractitioner.getDateEdited());
        assertEquals(actualPractitioners.get(0).getServerVersion(), expectedPractitioner.getServerVersion());

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

    @Test
    public void testCountAllPractitioners() throws Exception  {
        doReturn(2L).when(practitionerService).countAllPractitioners();
        MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + "count").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        assertNotNull(mvcResult);
        assertNotNull(mvcResult.getResponse());
        assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        assertEquals("2", mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testCountAllPractitionersWithException() throws Exception {
        doThrow(new IllegalArgumentException()).when(practitionerService).countAllPractitioners();
        MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + "count").accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError()).andReturn();
        assertNotNull(mvcResult);
        assertNotNull(mvcResult.getResponse());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), mvcResult.getResponse().getStatus());
    }

    private Practitioner initTestPractitioner1() {
        DateTime dateCreated = DateTime.now();
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitoner-1-identifier");
        practitioner.setActive(true);
        practitioner.setName("Practitioner");
        practitioner.setUsername("Practioner1");
        practitioner.setUserId("user1");
        practitioner.setDateCreated(dateCreated);
        practitioner.setDateEdited(dateCreated);
        practitioner.setServerVersion(1);
        return practitioner;
    }

    private Practitioner initTestPractitioner2() {
        DateTime dateCreated = DateTime.now();
        Practitioner practitioner = new Practitioner();
        practitioner.setIdentifier("practitoner-2-identifier");
        practitioner.setActive(false);
        practitioner.setName("Second Practitioner");
        practitioner.setUsername("Practioner2");
        practitioner.setUserId("user2");
        practitioner.setDateCreated(dateCreated);
        practitioner.setDateEdited(dateCreated);
        practitioner.setServerVersion(2);
        return practitioner;
    }

}
