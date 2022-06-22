package org.opensrp.web.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.connector.openmrs.service.OpenmrsReportingService;
import org.opensrp.web.utils.TestResourceLoader;

import java.io.IOException;


public class OpenmrsReportingTest extends TestResourceLoader {

    private ReportController reportController;

    public OpenmrsReportingTest() throws IOException {
        super();
    }

    @Before
    public void setup() {
        reportController = new ReportController(new OpenmrsReportingService(openmrsOpenmrsUrl, openmrsUsername, openmrsPassword));
    }

    @Test
    public void testReportDefinitionOutput() throws JSONException {
        if (pushToOpenmrsForTest) {
            System.out.println(reportController.reportDefinitions());
        }
    }

    @Test
    public void testReportData() throws JSONException {
        if (pushToOpenmrsForTest) {
            JSONArray json = new JSONArray(reportController.reportDefinitions().getBody());
            System.out.println(json);
            for (int i = 0; i < json.length(); i++) {
                try {
                    System.out.println(reportController.reportData(json.getJSONObject(i).getString("uuid"), null));
                } catch (Exception e) {

                }
            }
        }
    }
}
