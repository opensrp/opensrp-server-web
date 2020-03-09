package org.opensrp.web.controller;

import java.io.IOException;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.web.utils.TestResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


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
}
