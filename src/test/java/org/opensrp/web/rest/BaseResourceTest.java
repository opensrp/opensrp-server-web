package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opensrp.domain.PlanDefinition;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.ResultMatcher;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.server.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.server.result.MockMvcResultHandlers.print;

/**
 * Created by Vincent Karuri on 06/05/2019
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml"})
public abstract class BaseResourceTest<T> {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("openSRPDataSource")
    private DataSource opensrpDataSource;

    protected MockMvc mockMvc;

    protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

    protected static List<String> tableNames = new ArrayList<>();

    @Before
    public void bootStrap() {
        this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.webApplicationContext).build();
    }

    protected void truncateTables() {
        Connection connection = null;
        try {
            for (String tableName : tableNames) {
                connection = DataSourceUtils.getConnection(opensrpDataSource);
                Statement statement = connection.createStatement();
                statement.executeUpdate("TRUNCATE " + tableName);
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected List<Object> getResponseAsList(String url, String parameter, ResultMatcher expectedStatus) throws Exception {

        String finalUrl = url;
        if (parameter != null &&!parameter.isEmpty()) {
            finalUrl = finalUrl + "?" + parameter;
        }

        MvcResult mvcResult = this.mockMvc.perform(get(finalUrl).accept(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }

        List<Object> result = new Gson().fromJson(responseString, new TypeToken<List<PlanDefinition>>(){}.getType()); // todo: make this more generic
        return result;
    }

    protected String getResponseAsString(String url, String parameter, ResultMatcher expectedStatus) throws Exception {

        String finalUrl = url;
        if (parameter != null &&!parameter.isEmpty()) {
            finalUrl = finalUrl + "?" + parameter;
        }

        MvcResult mvcResult = this.mockMvc.perform(get(finalUrl).accept(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        return  responseString;
    }

    protected byte[] getRequestAsByteArray(String url, String parameterQuery, ResultMatcher expectedStatus) throws Exception {

        String finalUrl = url;
        if (!parameterQuery.isEmpty()) {
            finalUrl = finalUrl + "?" + parameterQuery;
        }

        MvcResult mvcResult = this.mockMvc.perform(get(finalUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus).andReturn();

        return mvcResult.getResponse().getContentAsByteArray();
    }

    protected JsonNode postRequestWithJsonContent(String url, String data, ResultMatcher expectedStatus) throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(
                post(url).contentType(MediaType.APPLICATION_JSON).body(data.getBytes()).accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        JsonNode actualObj = mapper.readTree(responseString);
        return actualObj;
    }

    protected JsonNode putRequestWithJsonContent(String url, String data, ResultMatcher expectedStatus) throws Exception {

        MvcResult mvcResult = this.mockMvc.perform(
                put(url).contentType(MediaType.APPLICATION_JSON).body(data.getBytes()).accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        JsonNode actualObj = mapper.readTree(responseString);
        return actualObj;
    }

    protected MvcResult postRequestWithFormUrlEncode(String url, Map<String, String> parameters, ResultMatcher expectedStatus)
            throws Exception {

        List<BasicNameValuePair> paramList = new ArrayList<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        MvcResult mvcResult = this.mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                .body(EntityUtils.toString(new UrlEncodedFormEntity(paramList)).getBytes())
                .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(expectedStatus).andReturn();

        return mvcResult;
    }

    protected JsonNode postRequestWithBasicAuthorizationHeader(String url, String userName, String password,
                                                               ResultMatcher expectedStatus) throws Exception {

        String basicAuthCredentials = new String(Base64.encode((userName + ":" + password).getBytes()));
        System.out.println(basicAuthCredentials);
        MvcResult mvcResult = this.mockMvc.perform(
                post(url).contentType(MediaType.APPLICATION_JSON).header("Authorization", "Basic " + basicAuthCredentials)
                        .accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(expectedStatus).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            return null;
        }
        JsonNode actualObj = mapper.readTree(responseString);
        return actualObj;
    }

    protected <T> List<T> createObjectListFromJson(JsonNode jsonList, Class<T> classOfT) throws IOException {

        final List<T> objectList = new ArrayList<>();
        for (int i = 0; i < jsonList.size(); i++) {
            T object = mapper.treeToValue(jsonList.get(i), classOfT);
            objectList.add(object);
        }
        return objectList;
    }

    /** Objects in the list should have a unique uuid identifier field **/
    abstract void assertListsAreSameIgnoringOrder(List<T> expectedList, List<T> actualList);
}

