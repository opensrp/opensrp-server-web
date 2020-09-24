package org.opensrp.web.rest;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.opensrp.dto.CsvBulkImportDataSummary;
import org.opensrp.service.ImportBulkDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rest/import/bulk-data")
public class ImportBulkDataController {

	private ImportBulkDataService importBulkDataService;

	@Autowired
	public void setImportBulkDataService(ImportBulkDataService importBulkDataService) {
		this.importBulkDataService = importBulkDataService;
	}

	@PostMapping(headers = { "Accept=multipart/form-data" }, produces = {
			MediaType.APPLICATION_JSON_VALUE }, value = "/organizations")
	public ResponseEntity<CsvBulkImportDataSummary> importOrganizationsData(@RequestParam("file") MultipartFile file)
			throws IOException {

		List<Map<String, String>> csvClients = readCSVFile(file);
		return new ResponseEntity<>(importBulkDataService.convertandPersistOrganizationdata(csvClients),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@PostMapping(headers = { "Accept=multipart/form-data" }, produces = {
			MediaType.APPLICATION_JSON_VALUE }, value = "/practitioners")
	public ResponseEntity<CsvBulkImportDataSummary> importPractitionersData(@RequestParam("file") MultipartFile file) throws
			IOException {

		List<Map<String, String>> csvClients = readCSVFile(file);
		return new ResponseEntity<>(importBulkDataService.convertandPersistPractitionerdata(csvClients),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
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

}
