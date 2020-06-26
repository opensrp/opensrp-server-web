package org.opensrp.web.rest.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensrp.common.AllConstants;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.AssertionErrors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith (SpringJUnit4ClassRunner.class)
@ContextConfiguration (loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class SettingResourceTest {
	
	private final String BASE_URL = "/rest/v2/settings/";
	private final String EXPECTED_TEAM_ID = "TEAM-ID-123";
	@Autowired
	protected WebApplicationContext webApplicationContext;
	protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
	@Mock
	private SettingService settingService;
	@Mock
	private OpenmrsLocationService openmrsLocationService;
	@InjectMocks
	private SettingResource settingResource;
	private MockMvc mockMvc;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(this.settingResource).
				addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		settingResource.setSettingService(settingService, openmrsLocationService);
		settingResource.setObjectMapper(mapper);
	}
	
	@Test
	public void testGetByUniqueId() throws Exception {
		List<Setting> settingList = new ArrayList<>();
		Setting setting = new Setting();
		setting.setTeamId("TEAM-ID-123");
		setting.setIdentifier("setting_123");
		setting.setKey("hiv_prevalence");
		setting.setValue("true");
		setting.setDescription("The area HIV prevalence is more than 5%");
		settingList.add(setting);
		
		List<SettingConfiguration> settingConfig = new ArrayList<>();
		SettingConfiguration config = new SettingConfiguration();
		config.setTeamId("TEAM-ID-123");
		config.setIdentifier("setting_123");
		config.setSettings(settingList);
		settingConfig.add(config);
		
		when(settingService.findSettings(ArgumentMatchers.any(SettingSearchBean.class), null)).thenReturn(settingConfig);
		
		MvcResult result =
				mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{identifier}", "setting_123")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		
		Mockito.verify(settingService).findSettings(ArgumentMatchers.any(SettingSearchBean.class), null);
		Mockito.verifyNoMoreInteractions(settingService);
		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			AssertionErrors.fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		
		Assert.assertEquals(actualObj.get(0).get("identifier").asText(), "setting_123");
		Assert.assertEquals(actualObj.get(0).get("teamId").asText(), EXPECTED_TEAM_ID);
		Assert.assertEquals(actualObj.get(0).get("key").asText(), "hiv_prevalence");
		Assert.assertEquals(actualObj.get(0).get("value").asText(), "true");
		Assert.assertEquals(1, actualObj.size());
	}
	
	@Test
	public void testGetAllSettings() throws Exception {
		List<SettingConfiguration> settingConfig = new ArrayList<>();
		SettingConfiguration config = new SettingConfiguration();
		config.setTeamId("TEAM-ID-123");
		config.setIdentifier("setting_123");
		config.setSettings(createSettingsList());
		settingConfig.add(config);
		
		when(settingService.findSettings(ArgumentMatchers.any(SettingSearchBean.class),null)).thenReturn(settingConfig);
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.get(BASE_URL + "/").param(AllConstants.Event.TEAM_ID, EXPECTED_TEAM_ID).param(AllConstants.BaseEntity.SERVER_VERSIOIN, "15421904649873"))
				.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
		Mockito.verify(settingService).findSettings(ArgumentMatchers.any(SettingSearchBean.class),null);
		Mockito.verifyNoMoreInteractions(settingService);
		JSONArray response = new JSONArray(result.getResponse().getContentAsString());
		Assert.assertEquals(2, response.length());
		Assert.assertEquals("setting_987", ((JSONObject) response.get(1)).get("identifier"));
		Assert.assertEquals("setting_123", ((JSONObject) response.get(0)).get("identifier"));
	}
	
	@Test
	public void testGetAllSettingsWithoutAnyParams() throws Exception {
		List<SettingConfiguration> settingConfig = new ArrayList<>();
		SettingConfiguration config = new SettingConfiguration();
		config.setTeamId("TEAM-ID-123");
		config.setIdentifier("setting_123");
		config.setSettings(createSettingsList());
		settingConfig.add(config);
		
		Mockito.when(settingService.findSettings(ArgumentMatchers.any(SettingSearchBean.class),null)).thenReturn(settingConfig);
		MvcResult result = mockMvc
				.perform(MockMvcRequestBuilders.get(BASE_URL + "/")).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
		Mockito.verifyNoInteractions(settingService);
		JSONObject response = new JSONObject(result.getResponse().getContentAsString());
		Assert.assertEquals(0, response.length());
	}
	
	@Test
	public void testCreateOrUpdate() throws Exception {
		ArgumentCaptor<Setting> argumentCaptor = ArgumentCaptor.forClass(Setting.class);
		String EXPECTED_SETTINGS = "{\"type\":\"Setting\",\"identifier\":\"setting_123\",\"teamId\":\"TEAM-ID-123\",\"key\":\"hiv_prevalence\",\"value\":\"true\",\"description\":\"The area HIV prevalence is more than 5%\"}";
		mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/").contentType(MediaType.APPLICATION_JSON).content(EXPECTED_SETTINGS.getBytes())).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
		Mockito.verify(settingService, Mockito.times(1)).addOrUpdateSettings(argumentCaptor.capture());
		Mockito.verifyNoMoreInteractions(settingService);
		Assert.assertEquals("setting_123", argumentCaptor.getValue().getIdentifier());
	}
	
	@Test
	public void testCreateOrUpdateWithWrongJson() throws Exception {
		Mockito.when(settingService.saveSetting(ArgumentMatchers.any(String.class))).thenReturn("setting_123");
		MvcResult result =
				mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/").contentType(MediaType.APPLICATION_JSON).content(
						"EXPECTED_SETTINGS.getBytes()")).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
		Mockito.verifyNoInteractions(settingService);
		Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
	}
	
	@Test
	public void testDelete() throws Exception {
		ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
		mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/{id}",1)).andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn();
		Mockito.verify(settingService, Mockito.times(1)).deleteSetting(argumentCaptor.capture());
		assertEquals(argumentCaptor.getValue().longValue(), 1);
	}
	@Test
	public void testDeleteWithNoIdentifier() throws Exception {
		MvcResult result =
				mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/{id}","test")).andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
		Mockito.verifyNoInteractions(settingService);
		Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
	}
	
	private List<Setting> createSettingsList() {
		List<Setting> settingList = new ArrayList<>();
		Setting setting = new Setting();
		setting.setTeamId("TEAM-ID-123");
		setting.setIdentifier("setting_123");
		setting.setKey("hiv_prevalence");
		setting.setValue("true");
		setting.setDescription("The area HIV prevalence is more than 5%");
		
		Setting setting1 = new Setting();
		setting1.setTeamId("TEAM-ID-123");
		setting1.setIdentifier("setting_987");
		setting1.setKey("malaria_prevalence");
		setting1.setValue("false");
		setting1.setDescription("Malaria prevalence is more  5%");
		
		settingList.add(setting);
		settingList.add(setting1);
		return settingList;
	}
}
