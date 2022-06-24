package org.opensrp.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml"})
public class CustomErrorResourceTest {

    private final String ERROR_ENDPOINT = "/error";
    private MockMvc mockMvc;
    @InjectMocks
    private CustomErrorResource customErrorResource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(customErrorResource)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testErrorEndpointReturnsJsonString() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(ERROR_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        assertEquals("{\"message\":\"OK\",\"status\":\"200 OK\",\"data\":null,\"success\":true}", responseString);
    }
}
