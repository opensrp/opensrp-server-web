package org.opensrp.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.http.entity.ContentType;
import org.hamcrest.core.StringStartsWith;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensrp.TestFileContent;
import org.opensrp.domain.IdVersionTuple;
import org.opensrp.domain.Manifest;
import org.opensrp.domain.postgres.ClientForm;
import org.opensrp.domain.postgres.ClientFormMetadata;
import org.opensrp.service.ClientFormService;
import org.opensrp.service.ManifestService;
import org.opensrp.util.DateTimeDeserializer;
import org.opensrp.util.DateTimeSerializer;
import org.opensrp.web.Constants;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles (profiles = { "jedis", "postgres", "basic_auth" })
public class ClientFormResourceTest {

    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
    private MockMvc mockMvc;
    private ClientFormService clientFormService;
    private ManifestService manifestService;

    private final String BASE_URL = "/rest/clientForm/";

    @Before
    public void setUp() {
        clientFormService = mock(ClientFormService.class);
        manifestService = mock(ManifestService.class);

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDateFormat(DateFormat.getDateTimeInstance());

        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        clientFormResource.setClientFormService(clientFormService, manifestService);
        clientFormResource.setObjectMapper(mapper);

        mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext).
                addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();

        SimpleModule dateTimeModule = new SimpleModule("DateTimeModule");
        dateTimeModule.addDeserializer(DateTime.class, new DateTimeDeserializer());
        dateTimeModule.addSerializer(DateTime.class, new DateTimeSerializer());
        mapper.registerModule(dateTimeModule);
    }

    @Test
    public void testSearchForFormByFormVersionShouldReturnSpecificJsonFormVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.0.3";
        String currentFormVersion = "0.0.1";

        ClientForm clientForm = new ClientForm();
        clientForm.setJson("{}");
        clientForm.setId(3L);

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setId(3L);
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setVersion("0.0.3");

        when(clientFormService.isClientFormExists(formIdentifier, false)).thenReturn(true);
        when(clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, false)).thenReturn(clientFormMetadata);
        when(clientFormService.getClientFormById(3L)).thenReturn(clientForm);
        when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

        MvcResult result = mockMvc.perform(get(BASE_URL)
                .param("form_identifier", formIdentifier)
                .param("form_version", formVersion)
                .param("current_form_version", currentFormVersion)
                .param("strict", "true"))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(responseString);
        assertEquals("{}", jsonNode.get("clientForm").get("json").textValue());
        assertEquals("opd/reg.json", jsonNode.get("clientFormMetadata").get("identifier").textValue());
        assertEquals("0.0.3", jsonNode.get("clientFormMetadata").get("version").textValue());
    }


    @Test
    public void testSearchForFormByFormVersionShouldReturnSpecificJsonValidatorVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.0.3";
        String currentFormVersion = "0.0.1";

        ClientForm clientForm = new ClientForm();
        clientForm.setJson("{}");
        clientForm.setId(3L);

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setId(3L);
        clientFormMetadata.setIsJsonValidator(true);
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setVersion("0.0.3");

        when(clientFormService.isClientFormExists(formIdentifier, true)).thenReturn(true);
        when(clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, true)).thenReturn(clientFormMetadata);
        when(clientFormService.getClientFormById(3L)).thenReturn(clientForm);
        when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

        MvcResult result = mockMvc.perform(get(BASE_URL)
                .param("form_identifier", formIdentifier)
                .param("form_version", formVersion)
                .param("current_form_version", currentFormVersion)
                .param("strict", "true")
                .param("is_json_validator", "true"))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(responseString);
        assertEquals("{}", jsonNode.get("clientForm").get("json").textValue());
        assertEquals("opd/reg.json", jsonNode.get("clientFormMetadata").get("identifier").textValue());
        assertEquals("0.0.3", jsonNode.get("clientFormMetadata").get("version").textValue());
    }

    @Test
    public void testSearchForFormByFormVersionShouldReturnNoContentWhenVersionHasntChangedAndStrictIsTrue() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String currentFormVersion = "0.0.3";
        List<IdVersionTuple> idVersionTuples = new ArrayList<>();
        idVersionTuples.add(new IdVersionTuple(1, "0.0.1"));
        idVersionTuples.add(new IdVersionTuple(2, "0.0.2"));
        idVersionTuples.add(new IdVersionTuple(3, "0.0.3"));

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setId(3L);
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setVersion("0.0.3");

        when(clientFormService.isClientFormExists(formIdentifier, false)).thenReturn(true);
        when(clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, false)).thenReturn(null);
        when(clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, false)).thenReturn(idVersionTuples);
        when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

        MvcResult result = mockMvc.perform(get(BASE_URL)
                .param("form_identifier", formIdentifier)
                .param("form_version", formVersion)
                .param("strict", "True")
                .param("current_form_version", currentFormVersion))
                .andExpect(status().isNoContent())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        assertEquals("", responseString);
    }

    @Test
    public void testSearchForFormByFormVersionShouldReturn404WhenStrictIsTrueAndCurrentFormVersionIsOutOfDate() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String currentFormVersion = "0.0.2";
        List<IdVersionTuple> idVersionTuples = new ArrayList<>();
        idVersionTuples.add(new IdVersionTuple(1, "0.0.1"));
        idVersionTuples.add(new IdVersionTuple(2, "0.0.2"));
        idVersionTuples.add(new IdVersionTuple(3, "0.0.3"));

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setId(3L);
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setVersion("0.0.3");

        when(clientFormService.isClientFormExists(formIdentifier, false)).thenReturn(true);
        when(clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, false)).thenReturn(null);
        when(clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, false)).thenReturn(idVersionTuples);
        when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

        MvcResult result = mockMvc.perform(get(BASE_URL)
                .param("form_identifier", formIdentifier)
                .param("form_version", formVersion)
                .param("strict", "True")
                .param("current_form_version", currentFormVersion))
                .andExpect(status().isNotFound())
                .andReturn();

        assertEquals("", result.getResponse().getContentAsString());

        verify(clientFormService).isClientFormExists(formIdentifier, false);
        verify(clientFormService).getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, false);
        verify(clientFormService).getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, false);
        verify(clientFormService).getClientFormMetadataById(3L);
    }

    @Test
    public void testSearchForFormByFormVersionShouldReturnNextJsonFormVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String currentFormVersion = "0.0.1";
        List<IdVersionTuple> idVersionTuples = new ArrayList<>();
        idVersionTuples.add(new IdVersionTuple(1, "0.0.1"));
        idVersionTuples.add(new IdVersionTuple(2, "0.0.2"));
        idVersionTuples.add(new IdVersionTuple(3, "0.0.3"));

        ClientForm clientForm = new ClientForm();
        clientForm.setJson("{}");
        clientForm.setId(3L);

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setId(3L);
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setVersion("0.0.3");

        when(clientFormService.isClientFormExists(formIdentifier, false)).thenReturn(true);
        when(clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, false)).thenReturn(null);
        when(clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, false)).thenReturn(idVersionTuples);
        when(clientFormService.getClientFormById(3L)).thenReturn(clientForm);
        when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

        MvcResult result = mockMvc.perform(get(BASE_URL)
                .param("form_identifier", formIdentifier)
                .param("form_version", formVersion)
                .param("current_form_version", currentFormVersion))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(responseString);
        assertEquals("{}", jsonNode.get("clientForm").get("json").textValue());
        assertEquals("opd/reg.json", jsonNode.get("clientFormMetadata").get("identifier").textValue());
        assertEquals("0.0.3", jsonNode.get("clientFormMetadata").get("version").textValue());


        verify(clientFormService).isClientFormExists(formIdentifier, false);
        verify(clientFormService).getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, false);
        verify(clientFormService).getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, false);
        verify(clientFormService).getClientFormById(3L);
        verify(clientFormService).getClientFormMetadataById(3L);
    }


    @Test
    public void testSearchForFormByFormVersionShouldReturnNextJsonValidatorVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String currentFormVersion = "0.0.1";
        List<IdVersionTuple> idVersionTuples = new ArrayList<>();
        idVersionTuples.add(new IdVersionTuple(1, "0.0.1"));
        idVersionTuples.add(new IdVersionTuple(2, "0.0.2"));
        idVersionTuples.add(new IdVersionTuple(3, "0.0.3"));

        ClientForm clientForm = new ClientForm();
        clientForm.setJson("{}");
        clientForm.setId(3L);

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setId(3L);
        clientFormMetadata.setIsJsonValidator(true);
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setVersion("0.0.3");

        when(clientFormService.isClientFormExists(formIdentifier, true)).thenReturn(true);
        when(clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, true)).thenReturn(null);
        when(clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, true)).thenReturn(idVersionTuples);
        when(clientFormService.getClientFormById(3L)).thenReturn(clientForm);
        when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

        MvcResult result = mockMvc.perform(get(BASE_URL)
                .param("form_identifier", formIdentifier)
                .param("form_version", formVersion)
                .param("current_form_version", currentFormVersion)
                .param("is_json_validator", "true"))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        JsonNode jsonNode = mapper.readTree(responseString);
        assertEquals("{}", jsonNode.get("clientForm").get("json").textValue());
        assertEquals("opd/reg.json", jsonNode.get("clientFormMetadata").get("identifier").textValue());
        assertEquals("0.0.3", jsonNode.get("clientFormMetadata").get("version").textValue());
        assertTrue(jsonNode.get("clientFormMetadata").get("isJsonValidator").booleanValue());
    }

    @Test
    public void testAddClientFormWhenGivenJSON() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestList());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.JSON_FORM_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.2", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenJSONAndFormVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "10.0.1";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestList());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());

        assertEquals(TestFileContent.JSON_FORM_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals(formVersion, clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenJSONValidatorFile() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formName = "REGISTRATION FORM VALIDATOR";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_VALIDATOR_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(new ArrayList<>());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName)
                        .param("is_json_validator", "true"))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(),
                clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.JSON_VALIDATOR_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.1", clientFormMetadata.getVersion());
        assertTrue(clientFormMetadata.getIsJsonValidator());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenJSONAndNoPreviousManifest() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(new ArrayList<>());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.JSON_FORM_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.1", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenJSONValidatorFileAndFormVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "10.0.1";
        String formName = "REGISTRATION FORM VALIDATOR";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_VALIDATOR_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(new ArrayList<>());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName)
                        .param("is_json_validator", "true"))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verifyNoInteractions(manifestService);

        assertEquals(TestFileContent.JSON_VALIDATOR_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals(formVersion, clientFormMetadata.getVersion());
        assertTrue(clientFormMetadata.getIsJsonValidator());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenJSONAndWithMinorVersionGreaterThan999() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formName = "REGISTRATION FORM";
    MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
            "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

    when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestMinorVersionGreaterThan1000());
    when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
    fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
            .andExpect(status().isCreated())
            .andReturn();

    ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
    ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
    verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
    verify(manifestService).getAllManifest(anyInt());

    assertEquals(TestFileContent.JSON_FORM_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
    ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
    assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
    assertEquals("1.0.0", clientFormMetadata.getVersion());
    assertEquals(formName, clientFormMetadata.getLabel());
    assertNull(clientFormMetadata.getModule());
}

    @Test
    public void testAddClientFormWhenGivenJSONAndWithNoFormVersion() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestWithNoForMVersion());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService, times(4)).getAllManifest(anyInt());

        assertEquals(TestFileContent.JSON_FORM_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.1", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenJSONAndWithPatchVersionGreaterThan999() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestPatchVersionGreaterThan1000());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.JSON_FORM_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.2.0", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    private List<Manifest> getManifestList() {
        Manifest manifest = new Manifest();
        manifest.setAppId("org.smartregister.anc");
        manifest.setAppVersion("1.2.13");
        manifest.setCreatedAt(DateTime.now());
        manifest.setJson("{\"forms_version\":\"0.0.1\",\n" +
                "            \"identifiers\":[\n" +
                "                \"anc/member_registration.json\",\n" +
                "                \"anc/pregnancy_outcome.json\"]}");
        manifest.setIdentifier("0.0.1");

        List<Manifest> manifestList = new ArrayList<>();
        manifestList.add(manifest);
        return manifestList;
    }

    private List<Manifest> getManifestMinorVersionGreaterThan1000() {
        Manifest manifest1 = new Manifest();
        manifest1.setAppId("org.smartregister.anc");
        manifest1.setAppVersion("1.2.13");
        manifest1.setCreatedAt(DateTime.now());
        manifest1.setJson("{\"forms_version\":\"0.1000.1000\",\n" +
                "            \"identifiers\":[\n" +
                "                \"anc/member_registration.json\",\n" +
                "                \"anc/pregnancy_outcome.json\"]}");
        manifest1.setIdentifier("0.0.1");
        List<Manifest> manifestList = new ArrayList<>();
        manifestList.add(manifest1);
        return manifestList;
    }

    private List<Manifest> getManifestPatchVersionGreaterThan1000() {
        Manifest manifest1 = new Manifest();
        manifest1.setAppId("org.smartregister.anc");
        manifest1.setAppVersion("1.2.13");
        manifest1.setCreatedAt(DateTime.now());
        manifest1.setJson("{\"forms_version\":\"0.1.1000\",\n" +
                "            \"identifiers\":[\n" +
                "                \"anc/member_registration.json\",\n" +
                "                \"anc/pregnancy_outcome.json\"]}");
        manifest1.setIdentifier("0.0.1");
        List<Manifest> manifestList = new ArrayList<>();
        manifestList.add(manifest1);
        return manifestList;
    }

    private List<Manifest> getManifestWithNoForMVersion() {
        Manifest manifest1 = new Manifest();
        manifest1.setAppId("org.smartregister.anc");
        manifest1.setAppVersion("1.2.13");
        manifest1.setCreatedAt(DateTime.now());
        manifest1.setJson("{\"form_version\":\"0.1.1000\",\n" +
                "            \"identifiers\":[\n" +
                "                \"anc/member_registration.json\",\n" +
                "                \"anc/pregnancy_outcome.json\"]}");
        manifest1.setIdentifier("0.0.1");
        List<Manifest> manifestList = new ArrayList<>();
        manifestList.add(manifest1);
        return manifestList;
    }



    @Test
    public void testAddClientFormWhenGivenJSONWithMissingReferencesShouldReturn400() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.PHYSICAL_EXAM_FORM_FILE.getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        MvcResult mvcResult = mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(clientFormService, times(16)).isClientFormExists(anyString());

        String errorMessage = mvcResult.getResponse().getContentAsString();
        assertTrue(errorMessage.contains("physical-exam-relevance-rules.yml"));
        assertTrue(errorMessage.contains("physical-exam-calculations-rules.yml"));
    }

    @Test
    public void testAddClientFormWhenGivenInvalidJSONShouldReturn400() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.substring(0, 20).getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        MvcResult result = mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName))
                .andExpect(status().isBadRequest())
                .andReturn();

        verifyNoInteractions(clientFormService);

        String errorMessage = result.getResponse().getContentAsString();
        assertEquals("File content error:", errorMessage.substring(0, 19));
    }

    @Test
    public void testAddClientFormWhenGivenJSONWithMissingFieldsShouldReturn400() throws Exception {
        String formIdentifier = "opd/reg.json";
        String formVersion = "0.1.1";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        ClientForm formValidator = new ClientForm();
        formValidator.setJson("\"{\"cannot_remove\":{\"title\":\"Fields you cannot remove\",\"fields\":[\"reaction_vaccine_duration\",\"reaction_vaccine_dosage\",\"aefi_form\"]}}\"");

        Mockito.doReturn(formValidator)
                .when(clientFormService).getMostRecentFormValidator(formIdentifier);

        MvcResult mvcResult = mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(clientFormService, times(0)).addClientForm(any(ClientForm.class), any(ClientFormMetadata.class));
        String response = mvcResult.getResponse().getContentAsString();


        assertThat(response, StringStartsWith.startsWith("Kindly make sure that the following fields are still in the form : "));
        assertTrue(response.contains("reaction_vaccine_duration"));
        assertTrue(response.contains("reaction_vaccine_dosage"));
    }

    @Test
    public void testAddClientFormWhenGivenRulesYaml() throws Exception {
        String formIdentifier = "opd/calculation.yaml";
        String formName = "Calculation file";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/calculation.yaml",
                "application/x-yaml", TestFileContent.CALCULATION_YAML_FILE_CONTENT.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestList());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.CALCULATION_YAML_FILE_CONTENT, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.2", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenYamlWithPropertiesReference() throws Exception {
        String formIdentifier = "opd/attention_flag.yml";
        String formName = "Relevance file";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/attention_flag.yml",
                "application/x-yaml", TestFileContent.ATTENTION_FLAGS_YAML_FILE.getBytes());

        Mockito.doReturn(true).when(clientFormService).isClientFormExists("attention_flags.properties");
        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestList());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.ATTENTION_FLAGS_YAML_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.2", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenYamlWithMissingPropertiesReferenceReturns400() throws Exception {
        String formIdentifier = "opd/attention_flag.yml";
        String formName = "Relevance file";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/attention_flag.yml",
                "application/x-yaml", TestFileContent.ATTENTION_FLAGS_YAML_FILE.getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        MvcResult result =  mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(clientFormService, times(2)).isClientFormExists(anyString());

        String errorMessage = result.getResponse().getContentAsString();
        assertTrue(errorMessage.contains("attention_flags"));
    }

    @Test
    public void testAddClientFormWhenGivenInvalidYamlShouldReturn400() throws Exception {
        String formIdentifier = "opd/calculation.yaml";
        String formVersion = "0.1.1";
        String formName = "Calculation file";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/calculation.yaml",
                "application/x-yaml", TestFileContent.INVALID_YAML_FILE_CONTENT.getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        MvcResult result = mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName))
                .andExpect(status().isBadRequest())
                .andReturn();

        verifyNoInteractions(clientFormService);

        String errorMessage = result.getResponse().getContentAsString();
        assertEquals("File content error:", errorMessage.substring(0, 19));
    }

    @Test
    public void testAddClientFormWhenGivenPropertiesFile() throws Exception {
        String formIdentifier = "opd/opd_register.properties";
        String formName = "Registration properties file";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/opd_register.properties",
                "application/octet-stream", TestFileContent.JMAG_PROPERTIES_FILE_CONTENT.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestList());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.JMAG_PROPERTIES_FILE_CONTENT, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.2", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testAddClientFormWhenGivenInvalidPropertiesFileShouldReturn400() throws Exception {
        String formIdentifier = "opd/opd_register.properties";
        String formVersion = "0.1.1";
        String formName = "Registration properties file";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/opd_register.properties",
                "application/octet-stream", (TestFileContent.JMAG_PROPERTIES_FILE_CONTENT + "\\uxxxx").getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        MvcResult result = mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_identifier", formIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName))
                .andExpect(status().isBadRequest())
                .andReturn();

        verifyNoInteractions(clientFormService);

        String errorMessage = result.getResponse().getContentAsString();
        assertEquals("File content error:", errorMessage.substring(0, 19));
    }

    @Test
    public void testAddClientFormWithoutIdentifierDefaultsToFilenameAsIdentifier() throws Exception {
        String formIdentifier = "reg.json";
        String formName = "REGISTRATION FORM";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/reg.json",
                "application/json", TestFileContent.JSON_FORM_FILE.getBytes());

        when(manifestService.getAllManifest(anyInt())).thenReturn(getManifestList());
        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());
        verify(manifestService).getAllManifest(anyInt());

        assertEquals(TestFileContent.JSON_FORM_FILE, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(formIdentifier, clientFormMetadata.getIdentifier());
        assertEquals("0.0.2", clientFormMetadata.getVersion());
        assertEquals(formName, clientFormMetadata.getLabel());
        assertNull(clientFormMetadata.getModule());
    }

    @Test
    public void testGetAllFilesRelatedToReleaseWithoutIdentifier() throws Exception {
	    MvcResult result = mockMvc.perform(get(BASE_URL + "release-related-files")
			    .param("identifier", ""))
			    .andExpect(status().isBadRequest())
			    .andReturn();
	    assertEquals("Request parameter cannot be empty", result.getResponse().getContentAsString());
    }

	@Test
	public void testGetAllFilesRelatedToReleaseWithIdentifierAndNoValidManifest() throws Exception {
		String identifier = "0.0.5";

		when(manifestService.getManifest(identifier)).thenReturn(new Manifest());

		MvcResult result = mockMvc.perform(get(BASE_URL + "release-related-files")
				.param("identifier", identifier))
				.andExpect(status().isNotFound())
				.andReturn();

		assertEquals("This manifest does not have any files related to it", result.getResponse().getContentAsString());
	}

	@Test
	public void testGetAllFilesRelatedToReleaseWithIdentifierAndNoFormIdentifierInManifest() throws Exception {
		String identifier = "0.0.5";

		when(manifestService.getManifest(identifier)).thenReturn(initTestManifest());

		MvcResult result = mockMvc.perform(get(BASE_URL + "release-related-files")
				.param("identifier", identifier))
				.andExpect(status().isNoContent())
				.andReturn();

		assertEquals("This manifest does not have any files related to it", result.getResponse().getContentAsString());
	}

	@Test
	public void testGetAllFilesRelatedToReleaseWithIdentifierAndEmptyFormIdentifierList() throws Exception {
		String identifier = "0.0.5";

		when(manifestService.getManifest(identifier)).thenReturn(initTestManifest2());

		MvcResult result = mockMvc.perform(get(BASE_URL + "release-related-files")
				.param("identifier", identifier))
				.andExpect(status().isNoContent())
				.andReturn();

		assertEquals("This manifest does not have any files related to it", result.getResponse().getContentAsString());
	}

	@Test
	public void testGetAllFilesRelatedToRelease() throws Exception {
		String identifier = "0.0.5";
		String formIdentifier = "opd/reg.json";

		List<IdVersionTuple> idVersionTuples = new ArrayList<>();
		idVersionTuples.add(new IdVersionTuple(1, "0.0.1"));
		idVersionTuples.add(new IdVersionTuple(2, "0.0.2"));
		idVersionTuples.add(new IdVersionTuple(3, "0.0.3"));

		ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
		clientFormMetadata.setId(3L);
		clientFormMetadata.setIdentifier(formIdentifier);
		clientFormMetadata.setVersion("0.0.3");

		when(manifestService.getManifest(identifier)).thenReturn(initTestManifest3());
		when(clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, false)).thenReturn(idVersionTuples);
		when(clientFormService.getClientFormMetadataById(3L)).thenReturn(clientFormMetadata);

		MvcResult result = mockMvc.perform(get(BASE_URL + "release-related-files")
				.param("identifier", identifier))
				.andExpect(status().isOk())
				.andReturn();

		String responseString = result.getResponse().getContentAsString();
		JsonNode jsonNode = mapper.readTree(responseString);
		assertEquals("opd/reg.json", jsonNode.get(0).get("identifier").textValue());
		assertEquals("0.0.3", jsonNode.get(0).get("version").textValue());
	}

	private static Manifest initTestManifest() {
		Manifest manifest = new Manifest();
		String identifier = "mani1234";
		String appVersion = "1234234";
		String json = "{\"name\":\"test\"}";
		String appId = "1234567op";

		manifest.setAppId(appId);
		manifest.setAppVersion(appVersion);
		manifest.setIdentifier(identifier);
		manifest.setJson(json);

		return manifest;
	}

	private static Manifest initTestManifest2() {
		Manifest manifest = new Manifest();
		String identifier = "mani1234";
		String appVersion = "1234234";
		String json = "{\"forms_version\":\"0.0.1\",\n"
				+ "            \"identifiers\":[]}";
		String appId = "1234567op";

		manifest.setAppId(appId);
		manifest.setAppVersion(appVersion);
		manifest.setIdentifier(identifier);
		manifest.setJson(json);

		return manifest;
	}

	private static Manifest initTestManifest3() {
		Manifest manifest = new Manifest();
		String identifier = "mani1234";
		String appVersion = "1234234";
		String json = "{\"forms_version\":\"0.0.1\",\"identifiers\":[\"opd/reg.json\"]}";
		String appId = "1234567op";

		manifest.setAppId(appId);
		manifest.setAppVersion(appVersion);
		manifest.setIdentifier(identifier);
		manifest.setJson(json);

		return manifest;
	}

    @Test
    public void testCanAddClientFormWithRelation() throws Exception{
        String relatedJsonIdentifier = "json.form/child-registration.json";
        String formVersion = "0.1.1";
        String formName = "CHILD CALCULATION";

        MockMultipartFile file = new MockMultipartFile("form", "path/to/opd/calculation.yml",
                "application/x-yaml", TestFileContent.CALCULATION_YAML_FILE_CONTENT.getBytes());

        when(clientFormService.addClientForm(any(ClientForm.class), any(ClientFormMetadata.class))).thenReturn(mock(ClientFormService.CompleteClientForm.class));

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("form_relation", relatedJsonIdentifier)
                        .param("form_version", formVersion)
                        .param("form_name", formName))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientForm> clientFormArgumentCaptor = ArgumentCaptor.forClass(ClientForm.class);
        ArgumentCaptor<ClientFormMetadata> clientFormMetadataArgumentCaptor = ArgumentCaptor.forClass(ClientFormMetadata.class);
        verify(clientFormService).addClientForm(clientFormArgumentCaptor.capture(), clientFormMetadataArgumentCaptor.capture());

        assertEquals(TestFileContent.CALCULATION_YAML_FILE_CONTENT, clientFormArgumentCaptor.getValue().getJson().toString());
        ClientFormMetadata clientFormMetadata = clientFormMetadataArgumentCaptor.getValue();
        assertEquals(relatedJsonIdentifier, clientFormMetadata.getRelation());
    }

    @Test
    public void testIsClientFormContentTypeValidShouldReturnTrueWhenGivenJSON() throws Exception {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertTrue(clientFormResource.isClientFormContentTypeValid(ContentType.APPLICATION_JSON.getMimeType(), "anc_register.json"));
    }

    @Test
    public void testIsClientFormContentTypeValidShouldReturnTrueWhenGivenApplicationYaml() throws Exception {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertTrue(clientFormResource.isClientFormContentTypeValid(Constants.ContentType.APPLICATION_YAML, "anc_register.yml"));
    }

    @Test
    public void testIsClientFormContentTypeValidShouldReturnTrueWhenGivenTextYamlContentTypeForYamlFile() throws Exception {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertTrue(clientFormResource.isClientFormContentTypeValid(Constants.ContentType.TEXT_YAML,"anc_register.yml"));
    }

    @Test
    public void testIsPropertiesFileShouldReturnTrue() {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertTrue(clientFormResource.isPropertiesFile("application/octet-stream", "anc_register.properties"));
    }

    @Test
    public void testIsPropertiesFileShouldReturnFalse() {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertFalse(clientFormResource.isPropertiesFile("application/octet-stream", "anc_register"));
    }

    @Test
    public void testCheckValidContentShouldReturnErrorMessageWhenGivenInvalidJSONStructure() {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertNotNull(clientFormResource.checkValidJsonYamlPropertiesStructure(TestFileContent.JSON_FORM_FILE.substring(0, 10), "application/json", "anc_register.json"));
    }

    @Test
    public void testCheckValidJsonYamlPropertiesStructureShouldReturnErrorMessageWhenGivenInvalidYamlStructure() {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertNotNull(clientFormResource.checkValidJsonYamlPropertiesStructure(TestFileContent.INVALID_YAML_FILE_CONTENT, "application/x-yaml", "anc_register.yml"));
    }

    @Test
    public void testCheckValidJsonYamlPropertiesStructureShouldReturnErrorMessageWhenGivenInvalidPropertiesStructure() {
        ClientFormResource clientFormResource = webApplicationContext.getBean(ClientFormResource.class);
        assertNotNull(clientFormResource.checkValidJsonYamlPropertiesStructure(TestFileContent.JMAG_PROPERTIES_FILE_CONTENT.substring(0, 378) + "\\uxxxx", ContentType.APPLICATION_OCTET_STREAM.getMimeType(), "anc_register.properties"));
    }

    @Test
    public void testGetClientFormMetadataListShouldReturnAllForms() throws Exception {
        int count = 11;
        String formIdentifier = "opd/opd_register.properties";
        List<ClientFormMetadata> clientFormMetadataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
            clientFormMetadata.setId((long) (i + 1));
            clientFormMetadata.setIdentifier(formIdentifier);
            clientFormMetadata.setVersion("0.0." + (i + 1));

            clientFormMetadataList.add(clientFormMetadata);
        }

        when(clientFormService.getAllClientFormMetadata()).thenReturn(clientFormMetadataList);

        MvcResult result = mockMvc.perform(get(BASE_URL + "metadata"))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        ArrayNode arrayNode = (ArrayNode) mapper.readTree(responseString);
        assertEquals(count, arrayNode.size());
        assertEquals(1L, arrayNode.get(0).get("id").longValue());
        assertEquals("0.0.1", arrayNode.get(0).get("version").textValue());
    }

    @Test
    public void testGetClientFormMetadataListShouldReturnDraftForms() throws Exception {
        int count = 7;
        String formIdentifier = "opd/opd_register.properties";
        List<ClientFormMetadata> clientFormMetadataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
            clientFormMetadata.setId((long) (i + 1));
            clientFormMetadata.setIdentifier(formIdentifier);
            clientFormMetadata.setIsDraft(true);
            clientFormMetadata.setVersion("0.0." + (i + 1));

            clientFormMetadataList.add(clientFormMetadata);
        }

        when(clientFormService.getDraftsClientFormMetadata(true)).thenReturn(clientFormMetadataList);

        MvcResult result = mockMvc.perform(get(BASE_URL + "metadata")
                .param("is_draft", "true"))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        ArrayNode arrayNode = (ArrayNode) mapper.readTree(responseString);
        assertEquals(count, arrayNode.size());
        assertEquals(1L, arrayNode.get(0).get("id").longValue());
        assertEquals("0.0.1", arrayNode.get(0).get("version").textValue());
        assertTrue(arrayNode.get(0).get("isDraft").booleanValue());
    }

    @Test
    public void testGetClientFormMetadataListShouldReturnJsonWidgetValidators() throws Exception {
        int count = 7;
        String formIdentifier = "opd/opd_register.properties";
        List<ClientFormMetadata> clientFormMetadataList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
            clientFormMetadata.setId((long) (i + 1));
            clientFormMetadata.setIdentifier(formIdentifier);
            clientFormMetadata.setIsDraft(true);
            clientFormMetadata.setIsJsonValidator(true);
            clientFormMetadata.setVersion("0.0." + (i + 1));

            clientFormMetadataList.add(clientFormMetadata);
        }

        when(clientFormService.getJsonWidgetValidatorClientFormMetadata(true)).thenReturn(clientFormMetadataList);

        MvcResult result = mockMvc.perform(get(BASE_URL + "metadata")
                .param("is_json_validator", "true"))
                .andExpect(status().isOk())
                .andReturn();
        String responseString = result.getResponse().getContentAsString();
        ArrayNode arrayNode = (ArrayNode) mapper.readTree(responseString);
        assertEquals(count, arrayNode.size());
        assertEquals(1L, arrayNode.get(0).get("id").longValue());
        assertEquals("0.0.1", arrayNode.get(0).get("version").textValue());
        assertTrue(arrayNode.get(0).get("isJsonValidator").booleanValue());
    }
}
