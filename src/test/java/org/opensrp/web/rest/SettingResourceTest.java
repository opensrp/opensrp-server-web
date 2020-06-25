package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.common.AllConstants.Event;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.SettingRepository;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.SettingService;
import org.smartregister.utils.DateTimeTypeConverter;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.Matchers;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = { "jedis", "postgres", "basic_auth" })
public class SettingResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Mock
	private SettingService settingService;

	@InjectMocks
	private SettingResource settingResource;

	protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
	
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	private String BASE_URL = "/rest/settings/";
	
	private String settingJson = "{\n" + "    \"_id\": \"1\",\n" + "    \"_rev\": \"v1\",\n"
	        + "    \"type\": \"SettingConfiguration\",\n" + "    \"identifier\": \"site_characteristics\",\n"
	        + "    \"documentId\": \"document-id\",\n" + "    \"locationId\": \"\",\n" + "    \"providerId\": \"\",\n"
	        + "    \"teamId\": \"my-team-id\",\n" + "    \"dateCreated\": \"1970-10-04T10:17:09.993+03:00\",\n"
	        + "    \"serverVersion\": 1,\n" + "    \"settings\": [\n" + "        {\n"
	        + "            \"key\": \"site_ipv_assess\",\n"
	        + "            \"label\": \"Minimum requirements for IPV assessment\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Are all of the following in place at your facility: \\r\\n\\ta. A protocol or standard operating procedure for Intimate Partner Violence (IPV); \\r\\n\\tb. A health worker trained on how to ask about IPV and how to provide the minimum response or beyond;\\r\\n\\tc. A private setting; \\r\\n\\td. A way to ensure confidentiality; \\r\\n\\te. Time to allow for appropriate disclosure; and\\r\\n\\tf. A system for referral in place. \"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_anc_hiv\",\n"
	        + "            \"label\": \"Generalized HIV epidemic\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is the HIV prevalence consistently > 1% in pregnant women attending antenatal clinics at your facility?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_ultrasound\",\n"
	        + "            \"label\": \"Ultrasound available\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is an ultrasound machine available and functional at your facility and a trained health worker available to use it?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_bp_tool\",\n"
	        + "            \"label\": \"Automated BP measurement tool\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Does your facility use an automated blood pressure (BP) measurement tool?\"\n"
	        + "        }\n" + "    ]\n" + "}";
	
	private String settingJsonUpdate = "{\n" + "    \"_id\": \"settings-document-id-2\",\n" + "    \"_rev\": \"v1\",\n"
	        + "    \"type\": \"SettingConfiguration\",\n" + "    \"identifier\": \"site_characteristics\",\n"
	        + "    \"documentId\": \"settings-document-id-2\",\n" 
	        + "    \"locationId\": \"\",\n" + "    \"providerId\": \"\",\n" + "    \"teamId\": \"my-team-id\",\n"
	        + "    \"dateCreated\": \"1970-10-04T10:17:09.993+03:00\",\n" + "    \"serverVersion\": 1,\n"
	        + "    \"settings\": [\n" + "        {\n" + "            \"key\": \"site_ipv_assess\",\n"
	        + "            \"label\": \"Minimum requirements for IPV assessment\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Are all of the following in place at your facility: \\r\\n\\ta. A protocol or standard operating procedure for Intimate Partner Violence (IPV); \\r\\n\\tb. A health worker trained on how to ask about IPV and how to provide the minimum response or beyond;\\r\\n\\tc. A private setting; \\r\\n\\td. A way to ensure confidentiality; \\r\\n\\te. Time to allow for appropriate disclosure; and\\r\\n\\tf. A system for referral in place. \"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_anc_hiv\",\n"
	        + "            \"label\": \"Generalized HIV epidemic\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is the HIV prevalence consistently > 1% in pregnant women attending antenatal clinics at your facility?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_ultrasound\",\n"
	        + "            \"label\": \"Ultrasound available\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Is an ultrasound machine available and functional at your facility and a trained health worker available to use it?\"\n"
	        + "        },\n" + "        {\n" + "            \"key\": \"site_bp_tool\",\n"
	        + "            \"label\": \"Automated BP measurement tool\",\n" + "            \"value\": null,\n"
	        + "            \"description\": \"Does your facility use an automated blood pressure (BP) measurement tool?\"\n"
	        + "        }\n" + "    ]\n" + "}";
	
	private List<SettingConfiguration> listSettingConfigurations;
	
	private ArgumentCaptor<SettingConfiguration> settingConfigurationArgumentCaptor = ArgumentCaptor
	        .forClass(SettingConfiguration.class);
	
	private MockMvc mockMvc;

	private String EXPECTED_RESPONSE_SAVE_SETTING = "{\"validated_records\":[\"ID-12345\"]}";
	private String EXPECTED_IDENTFIER = "ID-123";
	private String EXPECTED_TEAM_ID = "TEAM-ID-123";

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		listSettingConfigurations = new ArrayList<>();
		SettingConfiguration settingConfiguration = new SettingConfiguration();
		settingConfiguration.setIdentifier("site_characteristics");
		settingConfiguration.setTeamId("my-team-id");
		listSettingConfigurations.add(settingConfiguration);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(settingResource).
				addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
	}
	
	@Test
	public void testGetByUniqueId() throws Exception {
		List<SettingConfiguration> settingConfig = new ArrayList<>();
		SettingConfiguration config = new SettingConfiguration();
		config.setTeamId("TEAM-ID-123");
		config.setIdentifier("ID-123");
		settingConfig.add(config);

		Mockito.when(settingService.findSettings(any(SettingSearchBean.class))).thenReturn(settingConfig);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "0")
				.param(Event.TEAM_ID, "my-team-id").param(Event.PROVIDER_ID, "demo")).andExpect(status().isOk()).andReturn();

		verify(settingService).findSettings(any(SettingSearchBean.class));
		verifyNoMoreInteractions(settingService);
		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(actualObj.get(0).get("identifier").asText(), EXPECTED_IDENTFIER);
		assertEquals(actualObj.get(0).get("teamId").asText(), EXPECTED_TEAM_ID);
		assertEquals(actualObj.size(), 1);
	}

	@Test
	public void testValidValue() throws Exception {
		SettingConfiguration settingConfiguration = getSettingConfigurationObject();
		assertNotNull(settingConfiguration);
		assertEquals("site_characteristics", settingConfiguration.getIdentifier());
		assertEquals("my-team-id", settingConfiguration.getTeamId());
	}

	@Test
	public void testPostSaveSetting() throws Exception {
		String SETTINGS_JSON = "{\"settingConfigurations\":[\"Client1\"]}";

		when(settingService.saveSetting(Matchers.any(String.class))).thenReturn("ID-12345");
		MvcResult mvcResult = this.mockMvc.perform(
				post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(SETTINGS_JSON.getBytes()).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
		verify(settingService, times(1)).saveSetting(anyString());
		verifyNoMoreInteractions(settingService);
		assertEquals(mvcResult.getResponse().getContentAsString(), EXPECTED_RESPONSE_SAVE_SETTING);
	}

	@Test
	public void testFindSettingsByVersionAndTeamId() throws Exception {
		SettingService settingService  = Mockito.spy(new SettingService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService);
		SettingSearchBean sQB = new SettingSearchBean();
		sQB.setTeamId("my-team-id");
		sQB.setTeam(null);
		sQB.setLocationId(null);
		sQB.setProviderId(null);
		sQB.setServerVersion(1000L);

		settingService.findSettings(sQB);
		Mockito.verify(settingRepository, Mockito.times(1)).findSettings(sQB);
		Mockito.verifyNoMoreInteractions(settingRepository);

	}

	@Test
	public void testSaveSetting() throws Exception {
		SettingService settingService  = Mockito.spy(new SettingService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService);
		String documentId = "1";
		Mockito.doNothing().when(settingRepository).add(Matchers.any(SettingConfiguration.class));
		settingService.saveSetting(settingJson);

		Mockito.verify(settingRepository, Mockito.times(1)).add(settingConfigurationArgumentCaptor.capture());
		Mockito.verify(settingRepository, Mockito.times(1)).get(documentId);
		Mockito.verifyNoMoreInteractions(settingRepository);
	}

	@Test
	public void testUpdateSetting() throws Exception {
		SettingService settingService  = Mockito.spy(new SettingService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService);
		String documentId = "settings-document-id-2";
		Mockito.when(settingRepository.get("settings-document-id-2")).thenReturn(new SettingConfiguration());
		Mockito.doNothing().when(settingRepository).update(Matchers.any(SettingConfiguration.class));

		settingService.saveSetting(settingJsonUpdate);

		Mockito.verify(settingRepository, Mockito.times(1)).get(documentId);
		Mockito.verify(settingRepository, Mockito.times(1)).update(settingConfigurationArgumentCaptor.capture());
		Mockito.verifyNoMoreInteractions(settingRepository);
	}

	@Test
	public void testAddServerVersion() throws Exception {

		SettingService settingService  = Mockito.spy(new SettingService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService);
		settingService.addServerVersion();
		Mockito.verify(settingRepository, Mockito.times(1)).findByEmptyServerVersion();
		Mockito.verifyNoMoreInteractions(settingRepository);
	}

	private SettingConfiguration getSettingConfigurationObject() {
		return gson.fromJson(settingJson, new TypeToken<SettingConfiguration>() {}.getType());
	}
}
