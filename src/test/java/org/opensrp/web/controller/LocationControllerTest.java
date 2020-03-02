package org.opensrp.web.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.web.utils.TestResourceLoader;
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

    @Test(expected = JSONException.class)
    public void testLocationControllerWithEmptyParameter() throws JSONException {
        locationController.getLocationsWithinALevelAndTags(new JSONObject().toString());
    }

    @Test
    public void testLocationControllerWithParameters() throws JSONException {
        ResponseEntity<String> responseEntity = locationController.getLocationsWithinALevelAndTags(new JSONObject("{\n" +
                "  \"locationTagsQueried\": [\n" +
                "    \"Facility\"\n" +
                "  ],\n" +
                "  \"locationTopLevel\": \"Council\",\n" +
                "  \"locationUUID\": \"bcf5a36d-fb53-4de9-9813-01f1d480e3fe\"\n" +
                "}").toString());

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
