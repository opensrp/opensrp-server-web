package org.opensrp.web.rest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.opensrp.dto.CsvBulkImportDataSummary;
import org.opensrp.dto.FailedRecordSummary;
import org.opensrp.service.ImportBulkDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rest/import")
public class ImportBulkDataResource {

    private static final String SAMPLE_CSV_FILE = "/importsummaryreport.csv";
    private ImportBulkDataService importBulkDataService;

    @Autowired
    public void setImportBulkDataService(ImportBulkDataService importBulkDataService) {
        this.importBulkDataService = importBulkDataService;
    }

    @PostMapping(headers = {"Accept=multipart/form-data"}, produces = {
            MediaType.APPLICATION_JSON_VALUE}, value = "/organizations")
    public ResponseEntity importOrganizationsData(@RequestParam("file") MultipartFile file)
            throws IOException {

        List<Map<String, String>> csvClients = readCSVFile(file);
        CsvBulkImportDataSummary csvBulkImportDataSummary = importBulkDataService
                .convertandPersistOrganizationdata(csvClients);

        String timestamp = String.valueOf(new Date().getTime());
        URI uri = File.createTempFile(SAMPLE_CSV_FILE + "-" + timestamp, "").toURI();
        generateCSV(csvBulkImportDataSummary, uri);
        File csvFile = new File(uri);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + "importsummaryreport-" + timestamp + ".csv")
                .contentLength(csvFile.length())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new FileSystemResource(csvFile));
    }

    @PostMapping(headers = {"Accept=multipart/form-data"}, produces = {
            MediaType.APPLICATION_JSON_VALUE}, value = "/practitioners")
    public ResponseEntity importPractitionersData(@RequestParam("file") MultipartFile file) throws
            IOException {

        List<Map<String, String>> csvClients = readCSVFile(file);
        CsvBulkImportDataSummary csvBulkImportDataSummary = importBulkDataService
                .convertandPersistPractitionerdata(csvClients);

        String timestamp = String.valueOf(new Date().getTime());
        URI uri = File.createTempFile(SAMPLE_CSV_FILE + "-" + timestamp, "").toURI();
        generateCSV(csvBulkImportDataSummary, uri);
        File csvFile = new File(uri);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + "importsummaryreport-" + timestamp + ".csv")
                .contentLength(csvFile.length())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new FileSystemResource(csvFile));
    }

    private List<Map<String, String>> readCSVFile(MultipartFile file) throws IOException {
        List<Map<String, String>> csvClients = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            List<CSVRecord> records = parser.getRecords();
            for (CSVRecord record : records) {
                csvClients.add(record.toMap());
            }
        }
        return csvClients;
    }

    private void generateCSV(CsvBulkImportDataSummary csvBulkImportDataSummary, URI uri) throws IOException {

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(uri));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);

        csvPrinter.printRecord("Total Number of Rows in the CSV ", csvBulkImportDataSummary.getNumberOfCsvRows());
        csvPrinter.printRecord("Rows processed ", csvBulkImportDataSummary.getNumberOfRowsProcessed());
        csvPrinter.printRecord("\n");

        csvPrinter.printRecord("Row Number", "Reason of Failure");
        for (FailedRecordSummary failedRecordSummary : csvBulkImportDataSummary.getFailedRecordSummaryList()) {
            csvPrinter.printRecord(failedRecordSummary.getRowNumber(), failedRecordSummary.getReasonOfFailure());
        }
        csvPrinter.flush();
    }

}
