package org.opensrp.web.rest;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opensrp.util.DateTimeDeserializer;
import org.opensrp.util.DateTimeSerializer;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Created by Vincent Karuri on 06/05/2019
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml" })
@ActiveProfiles(profiles = { "jedis", "postgres", "basic_auth" })
@WithMockUser(username = "admin", roles = { "ADMIN" })
public abstract class BaseSecureResourceTest<T> {
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	protected MockMvc mockMvc;
	
	protected ObjectMapper mapper = new ObjectMapper();
	
	protected final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	
	@Before
	public void bootStrap() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
		        .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").apply(springSecurity()).build();
		
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setDateFormat(DateFormat.getDateTimeInstance());
		
		SimpleModule dateTimeModule = new SimpleModule("DateTimeModule");
		dateTimeModule.addDeserializer(DateTime.class, new DateTimeDeserializer());
		dateTimeModule.addSerializer(DateTime.class, new DateTimeSerializer());
		mapper.registerModule(dateTimeModule);
	}
	
	protected String getResponseAsString(String url, String parameter, ResultMatcher expectedStatus) throws Exception {
		
		String finalUrl = url;
		if (parameter != null && !parameter.isEmpty()) {
			finalUrl = finalUrl + "?" + parameter;
		}
		
		MvcResult mvcResult = this.mockMvc.perform(get(finalUrl).accept(MediaType.APPLICATION_JSON))
		        .andExpect(expectedStatus).andReturn();
		
		String responseString = mvcResult.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			return null;
		}
		return responseString;
	}
	
	protected JsonNode postRequestWithJsonContent(String url, String data, ResultMatcher expectedStatus) throws Exception {
		
		MvcResult mvcResult = this.mockMvc.perform(
		    post(url).contentType(MediaType.APPLICATION_JSON).content(data.getBytes()).accept(MediaType.APPLICATION_JSON))
		        .andExpect(expectedStatus).andReturn();
		
		String responseString = mvcResult.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			return null;
		}
		JsonNode actualObj = mapper.readTree(responseString);
		return actualObj;
	}
	
	protected String postRequestWithJsonContentAndReturnString(String url, String data, ResultMatcher expectedStatus)
	        throws Exception {
		
		MvcResult mvcResult = this.mockMvc.perform(
		    post(url).contentType(MediaType.APPLICATION_JSON).content(data.getBytes()).accept(MediaType.APPLICATION_JSON))
		        .andExpect(expectedStatus).andReturn();
		
		String responseString = mvcResult.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			return null;
		}
		return responseString;
	}
	
	protected JsonNode putRequestWithJsonContent(String url, String data, ResultMatcher expectedStatus) throws Exception {
		
		MvcResult mvcResult = this.mockMvc.perform(
		    put(url).contentType(MediaType.APPLICATION_JSON).content(data.getBytes()).accept(MediaType.APPLICATION_JSON))
		        .andExpect(expectedStatus).andReturn();
		
		String responseString = mvcResult.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			return null;
		}
		JsonNode actualObj = mapper.readTree(responseString);
		return actualObj;
	}
	
	protected String deleteRequestWithParams(String url, String parameter, ResultMatcher expectedStatus) throws Exception {
		
		String finalUrl = url;
		if (parameter != null && !parameter.isEmpty()) {
			finalUrl = finalUrl + "?" + parameter;
		}
		
		MvcResult mvcResult = this.mockMvc.perform(delete(finalUrl).accept(MediaType.APPLICATION_JSON))
		        .andExpect(expectedStatus).andReturn();
		
		String responseString = mvcResult.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			return null;
		}
		return responseString;
	}
	
	protected String deleteRequestWithJsonContent(String url, String data, ResultMatcher expectedStatus) throws Exception {
		MvcResult mvcResult = this.mockMvc.perform(
		    delete(url).contentType(MediaType.APPLICATION_JSON).content(data.getBytes()).accept(MediaType.APPLICATION_JSON))
		        .andExpect(expectedStatus).andReturn();
		
		String responseString = mvcResult.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			return null;
		}
		return responseString;
	}
	
	/** Objects in the list should have a unique uuid identifier field **/
	protected abstract void assertListsAreSameIgnoringOrder(List<T> expectedList, List<T> actualList);
	
	protected Date convertDate(String dateString, String dateFormat) {
		DateFormat format = new SimpleDateFormat(dateFormat);
		Date date = null;
		try {
			date = format.parse(dateString);
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
}
