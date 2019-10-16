package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.ResultMatcher;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.server.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.put;

/**
 * Created by Vincent Karuri on 06/05/2019
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml"})
public abstract class BaseResourceTest<T> {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

    @Before
    public void bootStrap() {
        this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.webApplicationContext).build();
    }

    protected String getResponseAsString(String url, String parameter, ResultMatcher expectedStatus) throws Exception {

        String finalUrl = url;
        if (parameter != null &&!parameter.isEmpty()) {
            finalUrl = finalUrl + "?" + parameter;
        }

        MvcResult mvcResult = this.mockMvc.perform(get(finalUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        return  responseString;
    }

    protected JsonNode postRequestWithJsonContent(String url, String data, ResultMatcher expectedStatus) throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(
                post(url).contentType(MediaType.APPLICATION_JSON).body(data.getBytes()).accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        JsonNode actualObj = mapper.readTree(responseString);
        return actualObj;
    }

    protected String postRequestWithJsonContentAndReturnString(String url, String data, ResultMatcher expectedStatus) throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(
                post(url).contentType(MediaType.APPLICATION_JSON).body(data.getBytes()).accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        return responseString;
    }

    protected JsonNode putRequestWithJsonContent(String url, String data, ResultMatcher expectedStatus) throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(
                put(url).contentType(MediaType.APPLICATION_JSON).body(data.getBytes()).accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        JsonNode actualObj = mapper.readTree(responseString);
        return actualObj;
    }

    protected String deleteRequestWithJsonContent(String url, String parameter, ResultMatcher expectedStatus) throws Exception {

        String finalUrl = url;
        if (parameter != null &&!parameter.isEmpty()) {
            finalUrl = finalUrl + "?" + parameter;
        }

        MvcResult mvcResult = this.mockMvc.perform(delete(finalUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        return  responseString;
    }

    /** Objects in the list should have a unique uuid identifier field **/
    protected abstract void assertListsAreSameIgnoringOrder(List<T> expectedList, List<T> actualList);
}

