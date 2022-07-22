package org.opensrp.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.dto.CsvBulkImportDataSummary;
import org.opensrp.dto.FailedRecordSummary;
import org.opensrp.service.ImportBulkDataService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class ImportBulkDataResourceTest {

    private final String BASE_URL = "/rest/import";
    private MockMvc mockMvc;
    @InjectMocks
    private ImportBulkDataResource importBulkDataResource;
    @Mock
    private ImportBulkDataService importBulkDataService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(importBulkDataResource)
                .setControllerAdvice(new GlobalExceptionHandler()).
                addFilter(new CrossSiteScriptingPreventionFilter(), "/*").
                build();
    }

    @Test
    public void testImportOrganizationsData() throws Exception {

        String path = "src/test/resources/sample/organizations.csv";
        MockMultipartFile firstFile = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
                Files.readAllBytes(Paths.get(path)));

        CsvBulkImportDataSummary csvBulkImportDataSummary = new CsvBulkImportDataSummary();
        csvBulkImportDataSummary.setNumberOfCsvRows(2);
        csvBulkImportDataSummary.setNumberOfRowsProcessed(1);
        List<FailedRecordSummary> failedRecordSummaryList = new ArrayList<>();
        FailedRecordSummary failedRecordSummary = new FailedRecordSummary();
        List<String> failureReasons = new ArrayList<>();
        failureReasons.add("Validation failed, provided location name mismatches with the system");
        failedRecordSummary.setReasonOfFailure(failureReasons);
        failedRecordSummary.setRowNumber(1);
        failedRecordSummaryList.add(failedRecordSummary);
        csvBulkImportDataSummary.setFailedRecordSummaryList(failedRecordSummaryList);

        when(importBulkDataService
                .convertandPersistOrganizationdata(anyList())).thenReturn(csvBulkImportDataSummary);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart(BASE_URL + "/organizations")
                                .file(firstFile)
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testImportPractitionersData() throws Exception {

        String path = "src/test/resources/sample/practitioners.csv";
        MockMultipartFile firstFile = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
                Files.readAllBytes(Paths.get(path)));

        CsvBulkImportDataSummary csvBulkImportDataSummary = new CsvBulkImportDataSummary();
        csvBulkImportDataSummary.setNumberOfCsvRows(3);
        csvBulkImportDataSummary.setNumberOfRowsProcessed(2);
        List<FailedRecordSummary> failedRecordSummaryList = new ArrayList<>();
        FailedRecordSummary failedRecordSummary = new FailedRecordSummary();
        List<String> failureReasons = new ArrayList<>();
        failureReasons.add("Validation failed, provided organization name mismatches with the system");
        failedRecordSummary.setReasonOfFailure(failureReasons);
        failedRecordSummary.setRowNumber(1);
        failedRecordSummaryList.add(failedRecordSummary);
        csvBulkImportDataSummary.setFailedRecordSummaryList(failedRecordSummaryList);

        when(importBulkDataService
                .convertandPersistPractitionerdata(anyList())).thenReturn(csvBulkImportDataSummary);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart(BASE_URL + "/practitioners")
                                .file(firstFile)
                )
                .andExpect(status().isOk())
                .andReturn();
    }

}
