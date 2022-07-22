package org.opensrp.web.controller;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.web.utils.TestResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public class LocationControllerTest extends TestResourceLoader {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private OpenmrsLocationService openmrsLocationService;

    private LocationController locationController;

    public LocationControllerTest() throws IOException {
        super();
    }

    @Before
    public void setUp() {
        locationController = new LocationController(openmrsLocationService);
    }

    @Test
    public void testLocationControllerWithParameters() throws JSONException {
        ResponseEntity<String> responseEntity = locationController
                .getLocationsWithinALevelAndTags("{\n" + "  \"locationTagsQueried\": [\n" + "    \"Facility\"\n" + "  ],\n"
                        + "  \"locationTopLevel\": \"Council\",\n"
                        + "  \"locationUUID\": \"bcf5a36d-fb53-4de9-9813-01f1d480e3fe\"\n" + "}");

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
