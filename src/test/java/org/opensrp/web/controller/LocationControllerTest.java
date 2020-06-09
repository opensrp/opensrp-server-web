package org.opensrp.web.controller;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.web.utils.TestResourceLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class LocationControllerTest extends TestResourceLoader {

    private LocationController locationController;

    public LocationControllerTest() throws IOException {
        super();
    }

    @Before
    public void setUp() {
        locationController = new LocationController(new OpenmrsLocationService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword));
    }

    @Test
    public void testLocationControllerWithParameters() throws JSONException {
        ResponseEntity<String> responseEntity = locationController.getLocationsWithinALevelAndTags("{\n" +
                "  \"locationTagsQueried\": [\n" +
                "    \"Facility\"\n" +
                "  ],\n" +
                "  \"locationTopLevel\": \"Council\",\n" +
                "  \"locationUUID\": \"bcf5a36d-fb53-4de9-9813-01f1d480e3fe\"\n" +
                "}");

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void getLocationsByTeamIds(){
        String teamLocations = "{\"results\":[{\"locations\":[{\"uuid\":\"2c3a0ebd-f79d-4128-a6d3-5dfbffbd01c8\",\"display\":\"Kabila Village\"}],\"team\":{\"location\":{\"display\":\"Huruma\",\"uuid\":\"718b2864-7d6a-44c8-b5b6-bb375f82654e\"}}},{\"locations\":[{\"uuid\":\"515c288e-5e95-4559-9de3-4192a73433d3\",\"display\":\"Mahaha Village\"}],\"team\":{\"location\":{\"display\":\"Mahaha\",\"uuid\":\"c99043c5-1d6f-4dec-967f-524a532654a9\"}}},{\"locations\":[{\"uuid\":\"718b2864-7d6a-44c8-b5b6-bb375f82654e\",\"display\":\"Huruma\"}],\"team\":{\"location\":{\"display\":\"Huruma\",\"uuid\":\"718b2864-7d6a-44c8-b5b6-bb375f82654e\"}}},{\"locations\":[{\"uuid\":\"c99043c5-1d6f-4dec-967f-524a532654a9\",\"display\":\"Mahaha\"}],\"team\":{\"location\":{\"display\":\"Mahaha\",\"uuid\":\"c99043c5-1d6f-4dec-967f-524a532654a9\"}}},{\"locations\":[{\"uuid\":\"515c288e-5e95-4559-9de3-4192a73433d3\",\"display\":\"Mahaha Village\"}],\"team\":{\"location\":{\"display\":\"Mahaha Village\",\"uuid\":\"515c288e-5e95-4559-9de3-4192a73433d3\"}}},{\"locations\":[{\"uuid\":\"aea836ea-1b7a-4671-af43-a57b26af9c2e\",\"display\":\"Ihanja Village\"}],\"team\":{\"location\":{\"display\":\"Ihanja Village\",\"uuid\":\"aea836ea-1b7a-4671-af43-a57b26af9c2e\"}}},{\"locations\":[{\"uuid\":\"84ed9853-219f-4032-8277-ca7efe006ac4\",\"display\":\"Isolo\"}],\"team\":{\"location\":{\"display\":\"Isolo\",\"uuid\":\"84ed9853-219f-4032-8277-ca7efe006ac4\"}}},{\"locations\":[{\"uuid\":\"74ebd30a-4a94-4265-b584-5f0d1ac0b7d0\",\"display\":\"Shishani Village\"}],\"team\":{\"location\":{\"display\":\"Shishani Village\",\"uuid\":\"74ebd30a-4a94-4265-b584-5f0d1ac0b7d0\"}}},{\"locations\":[{\"uuid\":\"953a6380-ef26-4453-a343-63216dd8862f\",\"display\":\"Ijinga Dispensary\"}],\"team\":{\"location\":{\"display\":\"Ijinga Dispensary\",\"uuid\":\"953a6380-ef26-4453-a343-63216dd8862f\"}}},{\"locations\":[{\"uuid\":\"2637756d-e025-401d-b0b4-69f8694f7d8c\",\"display\":\"Kisesa Dispensary\"}],\"team\":{\"location\":{\"display\":\"Kisesa Dispensary\",\"uuid\":\"2637756d-e025-401d-b0b4-69f8694f7d8c\"}}},{\"locations\":[{\"uuid\":\"2c3a0ebd-f79d-4128-a6d3-5dfbffbd01c8\",\"display\":\"Kabila Village\"}],\"team\":{\"location\":{\"display\":\"Kabila Village\",\"uuid\":\"2c3a0ebd-f79d-4128-a6d3-5dfbffbd01c8\"}}}]}";
        OpenmrsLocationService locationService = Mockito.spy(
                new OpenmrsLocationService("http://localhost:8080/openmrs/", "someuser", "somepass"));
        Whitebox.setInternalState(locationService, "OPENMRS_VERSION", "2.1.4");
        locationController = new LocationController(locationService);
        Mockito.doReturn(new HttpResponse(true, 200, teamLocations)).when(locationService).getAllTeamMembersHttpResponse();
        ResponseEntity<String> locationsByTeamIds = locationController.getLocationsByTeamIds("[\"718b2864-7d6a-44c8-b5b6-bb375f82654e\"]");
        Assert.assertEquals(HttpStatus.OK, locationsByTeamIds.getStatusCode());
        Assert.assertTrue(locationsByTeamIds.hasBody());
        Assert.assertTrue(locationsByTeamIds.getBody().contains("Kabila Village"));
    }
}
