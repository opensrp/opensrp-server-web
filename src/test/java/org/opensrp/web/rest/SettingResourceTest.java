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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.common.AllConstants.Event;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.repository.SettingRepository;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.SettingService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = { "jedis", "postgres", "basic_auth" })
public class SettingResourceTest {

	private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

	private final String BASE_URL = "/rest/settings/";

	private final String settingJson = "{\n" + "    \"_id\": \"1\",\n" + "    \"_rev\": \"v1\",\n"
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

	private final String settingJsonUpdate = "{\n" + "    \"_id\": \"settings-document-id-2\",\n" + "    \"_rev\": \"v1\",\n"
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

	private final String locationTreeString =
			"{\"locationsHierarchy\":{\"map\":{\"02ebbc84-5e29-4cd5-9b79-c594058923e9\":{\"id"
					+ "\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"label\":\"Uganda\",\"node\":{\"locationId\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"name\":\"Uganda\",\"tags\":[\"Country\"],\"voided\":false},\"children\":{\"8340315f-48e4-4768-a1ce-414532b4c49b\":{\"id\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"label\":\"Kampala\",\"node\":{\"locationId\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"name\":\"Kampala\",\"parentLocation\":{\"locationId\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"name\":\"Uganda\",\"voided\":false},\"tags\":[\"District\"],\"voided\":false},\"children\":{\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\":{\"id\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"label\":\"KCCA\",\"node\":{\"locationId\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"name\":\"KCCA\",\"parentLocation\":{\"locationId\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"name\":\"Kampala\",\"parentLocation\":{\"locationId\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"name\":\"Uganda\",\"voided\":false},\"voided\":false},\"tags\":[\"County\"],\"voided\":false},\"children\":{\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\":{\"id\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"label\":\"Central Division\",\"node\":{\"locationId\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"name\":\"Central Division\",\"parentLocation\":{\"locationId\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"name\":\"KCCA\",\"parentLocation\":{\"locationId\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"name\":\"Kampala\",\"voided\":false},\"voided\":false},\"tags\":[\"Sub-county\"],\"voided\":false},\"children\":{\"44de66fb-e6c6-4bae-92bb-386dfe626eba\":{\"id\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\",\"label\":\"Bukesa Urban Health Centre\",\"node\":{\"locationId\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\",\"name\":\"Bukesa Urban Health Centre\",\"parentLocation\":{\"locationId\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"name\":\"Central Division\",\"parentLocation\":{\"locationId\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"name\":\"KCCA\",\"voided\":false},\"voided\":false},\"tags\":[\"Health Facility\"],\"voided\":false},\"children\":{\"982eb3f3-b7e3-450f-a38e-d067f2345212\":{\"id\":\"982eb3f3-b7e3-450f-a38e-d067f2345212\",\"label\":\"Jambula Girls School\",\"node\":{\"locationId\":\"982eb3f3-b7e3-450f-a38e-d067f2345212\",\"name\":\"Jambula Girls School\",\"parentLocation\":{\"locationId\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\",\"name\":\"Bukesa Urban Health Centre\",\"parentLocation\":{\"locationId\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"name\":\"Central Division\",\"voided\":false},\"voided\":false},\"tags\":[\"School\"],\"voided\":false},\"parent\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\"}},\"parent\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\"}},\"parent\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\"}},\"parent\":\"8340315f-48e4-4768-a1ce-414532b4c49b\"}},\"parent\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\"}}}},\"parentChildren\":{\"8340315f-48e4-4768-a1ce-414532b4c49b\":[\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\"],\"02ebbc84-5e29-4cd5-9b79-c594058923e9\":[\"8340315f-48e4-4768-a1ce-414532b4c49b\"],\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\":[\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\"],\"44de66fb-e6c6-4bae-92bb-386dfe626eba\":[\"982eb3f3-b7e3-450f-a38e-d067f2345212\"],\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\":[\"44de66fb-e6c6-4bae-92bb-386dfe626eba\"]}}}";

	private final ArgumentCaptor<SettingConfiguration> settingConfigurationArgumentCaptor = ArgumentCaptor
			.forClass(SettingConfiguration.class);

	private final String EXPECTED_RESPONSE_SAVE_SETTING = "{\"validated_records\":[\"ID-12345\"]}";

	private final String EXPECTED_IDENTFIER = "ID-123";

	private final String EXPECTED_TEAM_ID = "TEAM-ID-123";

	@Autowired
	protected WebApplicationContext webApplicationContext;

	protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

	@Mock
	private SettingService settingService;

	@Mock
	private PhysicalLocationService physicalLocationService;

	@InjectMocks
	private SettingResource settingResource;

	private List<SettingConfiguration> listSettingConfigurations;

	private MockMvc mockMvc;

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

		Mockito.when(settingService.findSettings(any(SettingSearchBean.class), eq(null))).thenReturn(settingConfig);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "0")
				.param(Event.TEAM_ID, "my-team-id").param(Event.PROVIDER_ID, "demo")).andExpect(status().isOk()).andReturn();

		verify(settingService).findSettings(any(SettingSearchBean.class), eq(null));
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
	public void testGetByUniqueIdWithLocation() throws Exception {
		List<SettingConfiguration> settingConfig = new ArrayList<>();
		SettingConfiguration config = new SettingConfiguration();
		config.setTeamId("TEAM-ID-123");
		config.setIdentifier("ID-123");
		settingConfig.add(config);

		LocationTree locationTree = new Gson().fromJson(locationTreeString, LocationTree.class);
		Mockito.when(physicalLocationService.buildLocationHierachyFromLocation(anyString(), anyBoolean()))
				.thenReturn(locationTree);
		Mockito.when(settingService.findSettings(any(SettingSearchBean.class), anyMap())).thenReturn(settingConfig);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "0")
				.param(Event.TEAM_ID, "my-team-id").param(Event.PROVIDER_ID, "demo").param(Event.LOCATION_ID, "123123"))
				.andExpect(status().isOk()).andReturn();

		verify(settingService).findSettings(any(SettingSearchBean.class), anyMap());
		verifyNoMoreInteractions(settingService);
		verify(physicalLocationService).buildLocationHierachyFromLocation(anyString(), anyBoolean());

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
	public void findSettingsByVersionShouldReturn500IfServerVersionIsNotSpecified() throws Exception {
		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync")).andExpect(status().isBadRequest()).andReturn();
		assertEquals("{}", result.getResponse().getContentAsString());
	}

	@Test
	public void testValidValue() {
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
				post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(SETTINGS_JSON.getBytes())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn();
		verify(settingService, Mockito.times(1)).saveSetting(anyString());
		verifyNoMoreInteractions(settingService);
		assertEquals(mvcResult.getResponse().getContentAsString(), EXPECTED_RESPONSE_SAVE_SETTING);
	}

	@Test
	public void testFindSettingsByVersionAndTeamId() {
		SettingService settingService = Mockito.spy(new SettingService());
		PhysicalLocationService openmrsLocationService = Mockito.spy(new PhysicalLocationService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService, openmrsLocationService);
		SettingSearchBean sQB = new SettingSearchBean();
		sQB.setTeamId("my-team-id");
		sQB.setTeam(null);
		sQB.setLocationId(null);
		sQB.setProviderId(null);
		sQB.setServerVersion(1000L);

		settingService.findSettings(sQB, null);
		verify(settingRepository, Mockito.times(1)).findSettings(sQB, null);
		verifyNoMoreInteractions(settingRepository);

	}

	@Test
	public void testSaveSetting() {
		SettingService settingService = Mockito.spy(new SettingService());
		PhysicalLocationService openmrsLocationService = Mockito.spy(new PhysicalLocationService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService, openmrsLocationService);
		String documentId = "1";
		Mockito.doNothing().when(settingRepository).add(any(SettingConfiguration.class));
		settingService.saveSetting(settingJson);

		verify(settingRepository, Mockito.times(1)).add(settingConfigurationArgumentCaptor.capture());
		verify(settingRepository, Mockito.times(1)).get(documentId);
		verifyNoMoreInteractions(settingRepository);
	}

	@Test
	public void testUpdateSetting() {
		SettingService settingService = Mockito.spy(new SettingService());
		PhysicalLocationService openmrsLocationService = Mockito.spy(new PhysicalLocationService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService, openmrsLocationService);
		String documentId = "settings-document-id-2";
		SettingConfiguration setting = new SettingConfiguration();
		setting.setSettings(new ArrayList<>());
		Mockito.when(settingRepository.get("settings-document-id-2")).thenReturn(setting);
		Mockito.doNothing().when(settingRepository).update(any(SettingConfiguration.class));

		settingService.saveSetting(settingJsonUpdate);

		verify(settingRepository, Mockito.times(1)).get(documentId);
		verify(settingRepository, Mockito.times(1)).update(settingConfigurationArgumentCaptor.capture());
		verifyNoMoreInteractions(settingRepository);
	}

	@Test
	public void testAddServerVersion() {

		SettingService settingService = Mockito.spy(new SettingService());
		PhysicalLocationService openmrsLocationService = Mockito.spy(new PhysicalLocationService());
		SettingRepository settingRepository = Mockito.mock(SettingRepository.class);
		settingService.setSettingRepository(settingRepository);
		SettingResource settingResource = webApplicationContext.getBean(SettingResource.class);
		settingResource.setSettingService(settingService, openmrsLocationService);
		settingService.addServerVersion();
		verify(settingRepository, Mockito.times(1)).findByEmptyServerVersion();
		verifyNoMoreInteractions(settingRepository);
	}

	private SettingConfiguration getSettingConfigurationObject() {
		return gson.fromJson(settingJson, new TypeToken<SettingConfiguration>() {

		}.getType());
	}
}
