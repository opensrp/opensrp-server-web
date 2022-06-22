package org.opensrp.web.rest.v2;

import static org.opensrp.web.Constants.LIMIT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

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
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.setting.Setting;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.search.SettingSearchBean;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.SettingService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "basic_auth"})
public class SettingResourceTest {

    private final static String METADATA_VERSION = "metadata_version";
    private final String BASE_URL = "/rest/v2/settings/";
    private final String EXPECTED_TEAM_ID = "TEAM-ID-123";
    private final String locationTreeString = "{\"locationsHierarchy\":{\"map\":{\"02ebbc84-5e29-4cd5-9b79-c594058923e9\":{\"id"
            + "\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"label\":\"Uganda\",\"node\":{\"locationId\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"name\":\"Uganda\",\"tags\":[\"Country\"],\"voided\":false},\"children\":{\"8340315f-48e4-4768-a1ce-414532b4c49b\":{\"id\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"label\":\"Kampala\",\"node\":{\"locationId\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"name\":\"Kampala\",\"parentLocation\":{\"locationId\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"name\":\"Uganda\",\"voided\":false},\"tags\":[\"District\"],\"voided\":false},\"children\":{\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\":{\"id\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"label\":\"KCCA\",\"node\":{\"locationId\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"name\":\"KCCA\",\"parentLocation\":{\"locationId\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"name\":\"Kampala\",\"parentLocation\":{\"locationId\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\",\"name\":\"Uganda\",\"voided\":false},\"voided\":false},\"tags\":[\"County\"],\"voided\":false},\"children\":{\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\":{\"id\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"label\":\"Central Division\",\"node\":{\"locationId\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"name\":\"Central Division\",\"parentLocation\":{\"locationId\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"name\":\"KCCA\",\"parentLocation\":{\"locationId\":\"8340315f-48e4-4768-a1ce-414532b4c49b\",\"name\":\"Kampala\",\"voided\":false},\"voided\":false},\"tags\":[\"Sub-county\"],\"voided\":false},\"children\":{\"44de66fb-e6c6-4bae-92bb-386dfe626eba\":{\"id\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\",\"label\":\"Bukesa Urban Health Centre\",\"node\":{\"locationId\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\",\"name\":\"Bukesa Urban Health Centre\",\"parentLocation\":{\"locationId\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"name\":\"Central Division\",\"parentLocation\":{\"locationId\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\",\"name\":\"KCCA\",\"voided\":false},\"voided\":false},\"tags\":[\"Health Facility\"],\"voided\":false},\"children\":{\"982eb3f3-b7e3-450f-a38e-d067f2345212\":{\"id\":\"982eb3f3-b7e3-450f-a38e-d067f2345212\",\"label\":\"Jambula Girls School\",\"node\":{\"locationId\":\"982eb3f3-b7e3-450f-a38e-d067f2345212\",\"name\":\"Jambula Girls School\",\"parentLocation\":{\"locationId\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\",\"name\":\"Bukesa Urban Health Centre\",\"parentLocation\":{\"locationId\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\",\"name\":\"Central Division\",\"voided\":false},\"voided\":false},\"tags\":[\"School\"],\"voided\":false},\"parent\":\"44de66fb-e6c6-4bae-92bb-386dfe626eba\"}},\"parent\":\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\"}},\"parent\":\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\"}},\"parent\":\"8340315f-48e4-4768-a1ce-414532b4c49b\"}},\"parent\":\"02ebbc84-5e29-4cd5-9b79-c594058923e9\"}}}},\"parentChildren\":{\"8340315f-48e4-4768-a1ce-414532b4c49b\":[\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\"],\"02ebbc84-5e29-4cd5-9b79-c594058923e9\":[\"8340315f-48e4-4768-a1ce-414532b4c49b\"],\"b1ef8a0b-275b-43fc-a580-1e21ceb34c78\":[\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\"],\"44de66fb-e6c6-4bae-92bb-386dfe626eba\":[\"982eb3f3-b7e3-450f-a38e-d067f2345212\"],\"4e188e6d-2ffb-4b25-85f9-b9fbf5010d40\":[\"44de66fb-e6c6-4bae-92bb-386dfe626eba\"]}}}";
    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();
    @Mock
    private SettingService settingService;
    @Mock
    private PhysicalLocationService physicalLocationService;
    @InjectMocks
    private SettingResource settingResource;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(this.settingResource).
                addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
        settingResource.setSettingService(settingService, physicalLocationService);
        settingResource.setObjectMapper(mapper);
    }

    @Test
    public void testGetByUniqueId() throws Exception {
        List<Setting> settingList = new ArrayList<>();
        Setting setting = new Setting();
        setting.setTeamId("TEAM-ID-123");
        setting.setId("11");
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
        LocationTree locationTree = new Gson().fromJson(locationTreeString, LocationTree.class);
        Mockito.when(physicalLocationService.buildLocationHierachyFromLocation(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyBoolean())).thenReturn(locationTree);
        Mockito.when(settingService.findSettings(ArgumentMatchers.any(SettingSearchBean.class), ArgumentMatchers.eq(null)))
                .thenReturn(settingConfig);

        MvcResult result =
                mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}", "11"))
                        .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        Mockito.verify(settingService)
                .findSettings(ArgumentMatchers.any(SettingSearchBean.class), ArgumentMatchers.eq(null));
        Mockito.verifyNoMoreInteractions(settingService);
        Mockito.verifyNoInteractions(physicalLocationService);

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            AssertionErrors.fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);

        Assert.assertEquals(actualObj.get(0).get("id").asText(), "11");
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
        config.setMetadataVersion(0L);
        config.setLimit(1000);
        settingConfig.add(config);

        SettingSearchBean settingSearchBean = new SettingSearchBean();
        settingSearchBean.setLocationId("123232");
        settingSearchBean.setTeamId(EXPECTED_TEAM_ID);
        settingSearchBean.setServerVersion(Long.valueOf("15421904649873"));

        LocationTree locationTree = new Gson().fromJson(locationTreeString, LocationTree.class);
        Mockito.when(physicalLocationService.buildLocationTreeHierachyWithAncestors(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyBoolean())).thenReturn(locationTree);
        Mockito.when(settingService.findSettings(ArgumentMatchers.any(SettingSearchBean.class), ArgumentMatchers.anyMap()))
                .thenReturn(settingConfig);

        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get(BASE_URL + "/").param(AllConstants.Event.TEAM_ID, EXPECTED_TEAM_ID)
                        .param(AllConstants.BaseEntity.SERVER_VERSIOIN, "15421904649873")
                        .param(AllConstants.Event.LOCATION_ID, "123232")
                        .param(LIMIT, "1000")
                        .param(METADATA_VERSION, "100")
                )
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        Mockito.verify(settingService)
                .findSettings(ArgumentMatchers.any(SettingSearchBean.class), ArgumentMatchers.anyMap());
        Mockito.verifyNoMoreInteractions(settingService);
        Mockito.verify(physicalLocationService).buildLocationTreeHierachyWithAncestors(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyBoolean());

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

        Mockito.when(settingService.findSettings(ArgumentMatchers.any(SettingSearchBean.class), ArgumentMatchers.eq(null)))
                .thenReturn(settingConfig);
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders.get(BASE_URL + "/")).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        Mockito.verifyNoInteractions(settingService);
        Assert.assertEquals("All parameters cannot be null for this endpoint", result.getResponse().getContentAsString());
    }

    @Test
    public void testCreateOrUpdate() throws Exception {
        ArgumentCaptor<Setting> argumentCaptor = ArgumentCaptor.forClass(Setting.class);
        String EXPECTED_SETTINGS = "{\"type\":\"Setting\",\"identifier\":\"setting_123\",\"teamId\":\"TEAM-ID-123\",\"key\":\"hiv_prevalence\",\"value\":\"true\",\"description\":\"The area HIV prevalence is more than 5%\"}";
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/").contentType(MediaType.APPLICATION_JSON)
                .content(EXPECTED_SETTINGS.getBytes())).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        Mockito.verify(settingService, Mockito.times(1)).addOrUpdateSettings(argumentCaptor.capture());
        Mockito.verifyNoMoreInteractions(settingService);
        Assert.assertEquals("setting_123", argumentCaptor.getValue().getIdentifier());
    }

    @Test
    public void testCreateOrUpdateWithWrongJson() throws Exception {
        Mockito.when(settingService.saveSetting(ArgumentMatchers.any(String.class))).thenReturn("setting_123");
        MvcResult result =
                mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/").contentType(MediaType.APPLICATION_JSON).content(
                                "EXPECTED_SETTINGS.getBytes()")).andExpect(MockMvcResultMatchers.status().isBadRequest())
                        .andReturn();
        Mockito.verifyNoInteractions(settingService);
        Assert.assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    public void testDelete() throws Exception {
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/{id}", 1))
                .andExpect(MockMvcResultMatchers.status().isNoContent()).andReturn();
        Mockito.verify(settingService, Mockito.times(1)).deleteSetting(argumentCaptor.capture());
        Assert.assertEquals(argumentCaptor.getValue().longValue(), 1);
    }

    @Test
    public void testDeleteWithNoIdentifier() throws Exception {
        MvcResult result =
                mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/{id}", "test"))
                        .andExpect(MockMvcResultMatchers.status().isBadRequest()).andReturn();
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
