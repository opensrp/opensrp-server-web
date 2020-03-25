package org.opensrp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.service.CampaignService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.rest.CampaignResource;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class GlobalExceptionHandlerTest {

    @InjectMocks
    private CampaignResource campaignResource;

    @Mock
    private CampaignService campaignService;

    private MockMvc mockMvc;

    protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

    private String BASE_URL = "/rest/campaign/";
    private String MESSAGE = "The server encountered an error processing the request.";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(campaignResource)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    public void testExceptionHandlerForRuntimeException() throws Exception {

        when(campaignService.getCampaign(any(String.class)))
                .thenThrow(new RuntimeException());

        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "/{identifier}",
                "IRS_2018_S1")).
                andExpect(status().isInternalServerError())
                .andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            System.out.println("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);
        assertEquals(actualObj.get("message").asText(), MESSAGE);
    }
}
