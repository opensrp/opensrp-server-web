package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.domain.IdentifierSource;
import org.opensrp.service.IdentifierSourceService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})

public class IdentifierSourceResourceTest {

    private final String BASE_URL = "/rest/identifier-source";
    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
    @Mock
    private IdentifierSourceService identifierSourceService;
    @InjectMocks
    private IdentifierSourceResource identifierSourceResource;
    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(identifierSourceResource)
                .setControllerAdvice(new GlobalExceptionHandler()).
                        addFilter(new CrossSiteScriptingPreventionFilter(), "/*").
                        build();
    }

    @Test
    public void testGetAll() throws Exception {
        List<IdentifierSource> identifierSourceList = new ArrayList();
        identifierSourceList.add(createIdentifierSource());
        when(identifierSourceService.findAllIdentifierSources()).thenReturn(identifierSourceList);
        MvcResult result = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);
        assertEquals(actualObj.size(), 1);
        assertEquals(actualObj.get(0).get("id").asInt(), 1);
    }

    @Test
    public void testFindByIdentifier() throws Exception {
        IdentifierSource identifierSource = createIdentifierSource();
        when(identifierSourceService.findByIdentifier(anyString())).thenReturn(identifierSource);
        MvcResult result = mockMvc.perform(get(BASE_URL + "/{identifier}", "Test-1"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);
        assertEquals(actualObj.get("id").asInt(), 1);
    }

    @Test
    public void testCreateIdentifierSourceShouldInvokeAddMethod() throws Exception {
        IdentifierSource identifierSource = createIdentifierSource();
        doNothing().when(identifierSourceService).add(any(IdentifierSource.class));
        MvcResult result = mockMvc.perform(post(BASE_URL, new Gson().toJson(identifierSource))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    @Test
    public void testUpdateIdentifierSourceShouldInvokeUpdateMethod() throws Exception {
        IdentifierSource identifierSource = createIdentifierSource();
        doNothing().when(identifierSourceService).update(any(IdentifierSource.class));
        MvcResult result = mockMvc.perform(put(BASE_URL, new Gson().toJson(identifierSource))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
    }

    private IdentifierSource createIdentifierSource() {
        IdentifierSource identifierSource = new IdentifierSource();
        identifierSource.setId(1l);
        identifierSource.setIdentifier("Test-1");
        identifierSource.setBaseCharacterSet("AB12");
        identifierSource.setFirstIdentifierBase("ab12");
        identifierSource.setMinLength(4);
        identifierSource.setMaxLength(4);
        return identifierSource;
    }


}
