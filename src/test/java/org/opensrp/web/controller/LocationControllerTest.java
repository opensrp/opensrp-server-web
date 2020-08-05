package org.opensrp.web.controller;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "basic_auth"})
public class LocationControllerTest {

    @InjectMocks
    private LocationController locationController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        initMocks(this);
        String teamLocations = "[{\"locations\":[{\"uuid\":\"2c3a0ebd-f79d-4128-a6d3-5dfbffbd01c8\",\"display\":\"Kabila Village\"}],\"team\":{\"location\":{\"display\":\"Huruma\",\"uuid\":\"718b2864-7d6a-44c8-b5b6-bb375f82654e\"}}},{\"locations\":[{\"uuid\":\"515c288e-5e95-4559-9de3-4192a73433d3\",\"display\":\"Mahaha Village\"}],\"team\":{\"location\":{\"display\":\"Mahaha\",\"uuid\":\"c99043c5-1d6f-4dec-967f-524a532654a9\"}}},{\"locations\":[{\"uuid\":\"718b2864-7d6a-44c8-b5b6-bb375f82654e\",\"display\":\"Huruma\"}],\"team\":{\"location\":{\"display\":\"Huruma\",\"uuid\":\"718b2864-7d6a-44c8-b5b6-bb375f82654e\"}}},{\"locations\":[{\"uuid\":\"c99043c5-1d6f-4dec-967f-524a532654a9\",\"display\":\"Mahaha\"}],\"team\":{\"location\":{\"display\":\"Mahaha\",\"uuid\":\"c99043c5-1d6f-4dec-967f-524a532654a9\"}}},{\"locations\":[{\"uuid\":\"515c288e-5e95-4559-9de3-4192a73433d3\",\"display\":\"Mahaha Village\"}],\"team\":{\"location\":{\"display\":\"Mahaha Village\",\"uuid\":\"515c288e-5e95-4559-9de3-4192a73433d3\"}}},{\"locations\":[{\"uuid\":\"aea836ea-1b7a-4671-af43-a57b26af9c2e\",\"display\":\"Ihanja Village\"}],\"team\":{\"location\":{\"display\":\"Ihanja Village\",\"uuid\":\"aea836ea-1b7a-4671-af43-a57b26af9c2e\"}}},{\"locations\":[{\"uuid\":\"84ed9853-219f-4032-8277-ca7efe006ac4\",\"display\":\"Isolo\"}],\"team\":{\"location\":{\"display\":\"Isolo\",\"uuid\":\"84ed9853-219f-4032-8277-ca7efe006ac4\"}}},{\"locations\":[{\"uuid\":\"74ebd30a-4a94-4265-b584-5f0d1ac0b7d0\",\"display\":\"Shishani Village\"}],\"team\":{\"location\":{\"display\":\"Shishani Village\",\"uuid\":\"74ebd30a-4a94-4265-b584-5f0d1ac0b7d0\"}}},{\"locations\":[{\"uuid\":\"953a6380-ef26-4453-a343-63216dd8862f\",\"display\":\"Ijinga Dispensary\"}],\"team\":{\"location\":{\"display\":\"Ijinga Dispensary\",\"uuid\":\"953a6380-ef26-4453-a343-63216dd8862f\"}}},{\"locations\":[{\"uuid\":\"2637756d-e025-401d-b0b4-69f8694f7d8c\",\"display\":\"Kisesa Dispensary\"}],\"team\":{\"location\":{\"display\":\"Kisesa Dispensary\",\"uuid\":\"2637756d-e025-401d-b0b4-69f8694f7d8c\"}}},{\"locations\":[{\"uuid\":\"2c3a0ebd-f79d-4128-a6d3-5dfbffbd01c8\",\"display\":\"Kabila Village\"}],\"team\":{\"location\":{\"display\":\"Kabila Village\",\"uuid\":\"2c3a0ebd-f79d-4128-a6d3-5dfbffbd01c8\"}}}]";
        String locationJson = "[{\"locationId\":\"457c129d-06fc-4e2c-9394-d7ea85b2249f\",\"name\":\"Dar es Salaam\",\"parentLocation\":{\"locationId\":\"b7b70074-b8f1-4901-9238-1908b5d1f7d4\",\"name\":\"Eastern Zone\",\"voided\":false},\"tags\":[\"Region\"],\"voided\":false},{\"locationId\":\"b7b70074-b8f1-4901-9238-1908b5d1f7d4\",\"name\":\"Eastern Zone\",\"parentLocation\":{\"locationId\":\"82781f37-5bfd-45f3-8f1b-dad0a55b0570\",\"name\":\"Tanzania\",\"voided\":false},\"tags\":[\"Zone\"],\"voided\":false},{\"locationId\":\"8f61076c-bcde-4dac-af52-5feeece5c48a\",\"name\":\"Ebrahim Haji\",\"parentLocation\":{\"locationId\":\"d8fca020-dcac-47e7-b9fe-9d728588294b\",\"name\":\"Ilala MC\",\"voided\":false},\"tags\":[\"Facility\"],\"voided\":false},{\"locationId\":\"d8fca020-dcac-47e7-b9fe-9d728588294b\",\"name\":\"Ilala MC\",\"parentLocation\":{\"locationId\":\"457c129d-06fc-4e2c-9394-d7ea85b2249f\",\"name\":\"Dar es Salaam\",\"voided\":false},\"tags\":[\"Council\"],\"voided\":false},{\"locationId\":\"25820e25-76c5-455a-812d-0934db2564f5\",\"name\":\"Madona\",\"parentLocation\":{\"locationId\":\"d8fca020-dcac-47e7-b9fe-9d728588294b\",\"name\":\"Ilala MC\",\"voided\":false},\"tags\":[\"Facility\"],\"voided\":false},{\"locationId\":\"846955fd-02cd-4fb4-bfcd-712dc2f8c922\",\"name\":\"Mchafukoge\",\"parentLocation\":{\"locationId\":\"d8fca020-dcac-47e7-b9fe-9d728588294b\",\"name\":\"Ilala MC\",\"voided\":false},\"tags\":[\"Ward\"],\"voided\":false},{\"locationId\":\"2b17d7a9-3305-4835-8ca4-9de36eabad19\",\"name\":\"Mnazi Mmoja\",\"parentLocation\":{\"locationId\":\"d8fca020-dcac-47e7-b9fe-9d728588294b\",\"name\":\"Ilala MC\",\"voided\":false},\"tags\":[\"Facility\"],\"voided\":false},{\"locationId\":\"1ea3645a-90b0-4ec4-8b43-f7d85ea9e1a3\",\"name\":\"Tabata\",\"parentLocation\":{\"locationId\":\"d8fca020-dcac-47e7-b9fe-9d728588294b\",\"name\":\"Ilala MC\",\"voided\":false},\"tags\":[\"Ward\"],\"voided\":false},{\"locationId\":\"c760f2de-1c92-4495-8003-868dd25fe410\",\"name\":\"Tabata Dampo\",\"parentLocation\":{\"locationId\":\"1ea3645a-90b0-4ec4-8b43-f7d85ea9e1a3\",\"name\":\"Tabata\",\"voided\":false},\"tags\":[\"Village\"],\"voided\":false},{\"locationId\":\"82781f37-5bfd-45f3-8f1b-dad0a55b0570\",\"name\":\"Tanzania\",\"tags\":[\"Country\"],\"voided\":false},{\"locationId\":\"c37c218a-2cad-449a-8c7b-352dffdb3a39\",\"name\":\"Uhuru\",\"parentLocation\":{\"locationId\":\"846955fd-02cd-4fb4-bfcd-712dc2f8c922\",\"name\":\"Mchafukoge\",\"voided\":false},\"tags\":[\"Village\"],\"voided\":false},{\"locationId\":\"8d6c993e-c2cc-11de-8d13-0010c6dffd0f\",\"name\":\"Unknown Location\",\"voided\":false},{\"locationId\":\"fa92a452-bc41-427c-b382-9ebdfc18e1b7\",\"name\":\"Zanaki\",\"parentLocation\":{\"locationId\":\"846955fd-02cd-4fb4-bfcd-712dc2f8c922\",\"name\":\"Mchafukoge\",\"voided\":false},\"tags\":[\"Village\"],\"voided\":false}]\n";

        OpenmrsLocationService locationService = Mockito.spy(
                new OpenmrsLocationService("http://localhost:8080/openmrs/", "someuser", "somepass"));
        Whitebox.setInternalState(locationService, "OPENMRS_VERSION", "2.1.4");
        Mockito.doReturn(new JSONArray(teamLocations)).when(locationService).getAllTeamMemberLocations(Mockito.any(JSONArray.class), Mockito.anyInt());
//        List<Location> allLocations = new Gson().fromJson(locationJson, new TypeToken<List<Location>>() {
//        }.getType());
//        Mockito.doReturn(allLocations).when(locationService).getAllLocations(Mockito.anyList(), Mockito.anyInt());
        locationController = new LocationController(locationService);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(locationController)
                .addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
                .build();
    }

    @Test
    public void testLocationControllerWithParameters() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/location/by-level-and-tags")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"locationTagsQueried\":[\"Facility\"],\"locationTopLevel\":\"Council\",\"locationUUID\":\"bcf5a36d-fb53-4de9-9813-01f1d480e3fe\"}"))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Assert.assertNotNull(contentAsString);
    }

    @Test
    public void getLocationsByTeamIds() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/location/by-team-ids")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("[\"718b2864-7d6a-44c8-b5b6-bb375f82654e\"]"))
                .andExpect(status().isOk()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Assert.assertNotNull(contentAsString);
        Assert.assertTrue(contentAsString.contains("Kabila Village"));
    }
}
