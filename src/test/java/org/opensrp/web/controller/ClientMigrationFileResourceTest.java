package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.http.entity.ContentType;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensrp.domain.ClientMigrationFile;
import org.opensrp.service.ClientMigrationFileService;
import org.opensrp.util.DateTimeDeserializer;
import org.opensrp.util.DateTimeSerializer;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.server.request.RequestPostProcessor;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.DateFormat;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "basic_auth"})
public class ClientMigrationFileResourceTest {

    private final String BASE_URL = "/rest/client-migration-file/";
    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
    private MockMvc mockMvc;
    private ClientMigrationFileService clientMigrationFileService;

    @Before
    public void setUp() {
        clientMigrationFileService = mock(ClientMigrationFileService.class);

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDateFormat(DateFormat.getDateTimeInstance());

        ClientMigrationFileResource clientMigrationFileResource = webApplicationContext.getBean(ClientMigrationFileResource.class);
        clientMigrationFileResource.setClientMigrationFileService(clientMigrationFileService);
        clientMigrationFileResource.setObjectMapper(mapper);

        mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext).
                addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();

        SimpleModule dateTimeModule = new SimpleModule("DateTimeModule");
        dateTimeModule.addDeserializer(DateTime.class, new DateTimeDeserializer());
        dateTimeModule.addSerializer(DateTime.class, new DateTimeSerializer());
        mapper.registerModule(dateTimeModule);
    }

    @Test
    public void testCreate() throws Exception {
        String identifier = "1.up.sql";
        String fileContent = "CREATE TABLE vaccines(id INTEGER, vaccine_name VARCHAR);";

        MockMultipartFile file = new MockMultipartFile("migration_file", identifier,
                ContentType.TEXT_PLAIN.getMimeType(), fileContent.getBytes());

        mockMvc.perform(
                fileUpload(BASE_URL)
                        .file(file)
                        .param("version", "1"))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientMigrationFile> clientMigrationFileArgumentCaptor = ArgumentCaptor.forClass(ClientMigrationFile.class);
        verify(clientMigrationFileService).addClientMigrationFile(clientMigrationFileArgumentCaptor.capture());

        ClientMigrationFile clientMigrationFile = clientMigrationFileArgumentCaptor.getValue();
        assertEquals(fileContent, clientMigrationFile.getFileContents());
        assertEquals(identifier, clientMigrationFile.getIdentifier());
        assertEquals(Integer.valueOf(1), clientMigrationFile.getVersion());
        assertEquals(identifier, clientMigrationFile.getFilename());
        assertEquals(null, clientMigrationFile.getJurisdiction());
        assertEquals(Boolean.FALSE, clientMigrationFile.getOnObjectStorage());
        assertEquals(null, clientMigrationFile.getObjectStoragePath());
        assertEquals(null, clientMigrationFile.getManifestId());

    }

    @Test
    public void testUpdate() throws Exception {
        String identifier = "1.up.sql";
        String fileContent = "CREATE TABLE vaccines(id INTEGER, vaccine_name VARCHAR);";

        MockMultipartFile file = new MockMultipartFile("migration_file", identifier,
                ContentType.TEXT_PLAIN.getMimeType(), fileContent.getBytes());

        MockMultipartHttpServletRequestBuilder builder = fileUpload(BASE_URL);
        builder.with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
                request.setMethod("PUT");
                return request;
            }
        });

        Mockito.doReturn(new ClientMigrationFile()).when(clientMigrationFileService).getClientMigrationFile(identifier);

        mockMvc.perform(
                builder.file(file)
                        .param("version", "1"))
                .andExpect(status().isCreated())
                .andReturn();

        ArgumentCaptor<ClientMigrationFile> clientMigrationFileArgumentCaptor = ArgumentCaptor.forClass(ClientMigrationFile.class);
        verify(clientMigrationFileService).updateClientMigrationFile(clientMigrationFileArgumentCaptor.capture());

        ClientMigrationFile clientMigrationFile = clientMigrationFileArgumentCaptor.getValue();
        assertEquals(fileContent, clientMigrationFile.getFileContents());
        assertEquals(identifier, clientMigrationFile.getIdentifier());
        assertEquals(Integer.valueOf(1), clientMigrationFile.getVersion());
        assertEquals(identifier, clientMigrationFile.getFilename());
        assertEquals(null, clientMigrationFile.getJurisdiction());
        assertEquals(Boolean.FALSE, clientMigrationFile.getOnObjectStorage());
        assertEquals(null, clientMigrationFile.getObjectStoragePath());
        assertEquals(null, clientMigrationFile.getManifestId());
    }

    @Test
    public void testGetClientMigrationFileByIdentifier() throws Exception {
        String identifier = "1.up.sql";
        String fileContent = "CREATE TABLE vaccines(id INTEGER, vaccine_name VARCHAR);";

        ClientMigrationFile clientMigrationFile = new ClientMigrationFile();
        clientMigrationFile.setFileContents(fileContent);
        clientMigrationFile.setIdentifier(identifier);
        clientMigrationFile.setJurisdiction(null);
        clientMigrationFile.setOnObjectStorage(Boolean.FALSE);
        clientMigrationFile.setObjectStoragePath(null);
        clientMigrationFile.setFilename(identifier);
        clientMigrationFile.setVersion(1);
        clientMigrationFile.setCreatedAt(new Date());
        clientMigrationFile.setId(Long.valueOf(2));
        clientMigrationFile.setManifestId(null);

        Mockito.doReturn(clientMigrationFile).when(clientMigrationFileService).getClientMigrationFile(identifier);

        MvcResult result = mockMvc.perform(get(BASE_URL + "/identifier?identifier=" + identifier))
                .andExpect(status().isOk())
                .andReturn();

        verify(clientMigrationFileService).getClientMigrationFile(identifier);

        String responseString = result.getResponse().getContentAsString();
        ClientMigrationFile actualResult = mapper.readValue(responseString, ClientMigrationFile.class);

        assertEquals(fileContent, actualResult.getFileContents());
        assertEquals(identifier, actualResult.getIdentifier());
        assertEquals(Integer.valueOf(1), actualResult.getVersion());
        assertEquals(identifier, actualResult.getFilename());
        assertEquals(null, actualResult.getJurisdiction());
        assertEquals(Boolean.FALSE, actualResult.getOnObjectStorage());
        assertEquals(null, actualResult.getObjectStoragePath());
        assertEquals(null, actualResult.getManifestId());
    }

    @Test
    public void testDelete() throws Exception {
        String identifier = "1.up.sql";

        ClientMigrationFile clientMigrationFile = new ClientMigrationFile();
        clientMigrationFile.setIdentifier(identifier);

        Mockito.doReturn(clientMigrationFile).when(clientMigrationFileService).getClientMigrationFile(identifier);

        mockMvc.perform(delete(BASE_URL + identifier))
                .andExpect(status().isAccepted())
                .andReturn();

        ArgumentCaptor<ClientMigrationFile> clientMigrationFileArgumentCaptor = ArgumentCaptor.forClass(ClientMigrationFile.class);
        verify(clientMigrationFileService).deleteClientMigrationFile(clientMigrationFileArgumentCaptor.capture());

        assertEquals(identifier, clientMigrationFileArgumentCaptor.getValue().getIdentifier());
    }
}
