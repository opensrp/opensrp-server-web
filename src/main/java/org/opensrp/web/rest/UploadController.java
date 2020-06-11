package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.domain.CSVRowConfig;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.repository.MultimediaRepository;
import org.opensrp.search.UploadValidationBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.OpenmrsIDService;
import org.opensrp.service.UploadService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.util.JSONCSVUtil;
import org.opensrp.web.bean.UploadBean;
import org.opensrp.web.utils.OpenMRSUniqueIDProvider;
import org.opensrp.web.utils.UniqueIDProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(value = "/rest/upload")
public class UploadController {

	private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

	private static final Logger logger = LoggerFactory.getLogger(UploadController.class.toString());

	private static final ObjectMapper mapper = new ObjectMapper();

	@Value("#{opensrp['opensrp.config.global_id'] ?: 'opensrp_id'}")
	private String globalID;

	@Value("#{opensrp['multimedia.directory.name']}")
	private String multiMediaDir;

	public static final String BATCH_SIZE = "batch_size";

	public static final String OFFSET = "offset";

	public static final String TEAM_ID = "team_id";

	public static final String TEAM_NAME = "team_name";

	public static final String LOCATION_ID = "location_id";

	public static final String FILE_CATEGORY = "csv";

	public static final String DEFAULT_RESIDENCE = "default_residence";

	private MultimediaService multimediaService;

	private UploadService uploadService;

	private MultimediaRepository multimediaRepository;

	private OpenmrsIDService openmrsIDService;

	private ClientService clientService;

	private EventService eventService;

	@Autowired
	public void setMultimediaService(MultimediaService multimediaService) {
		this.multimediaService = multimediaService;
	}

	@Autowired
	public void setUploadService(UploadService uploadService) {
		this.uploadService = uploadService;
	}

	@Autowired
	public void setMultimediaRepository(MultimediaRepository multimediaRepository) {
		this.multimediaRepository = multimediaRepository;
	}

	@Autowired
	public void setOpenmrsIDService(OpenmrsIDService openmrsIDService) {
		this.openmrsIDService = openmrsIDService;
	}

	@Autowired
	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	private List<Map<String, String>> readCSVFile(MultipartFile file) throws IOException {
		List<Map<String, String>> csv_clients = new ArrayList<>();
		CSVParser parser;
		try (Reader reader = new InputStreamReader(file.getInputStream())) {
			parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader());
			List<CSVRecord> records = parser.getRecords();
			for (CSVRecord record : records) {
				csv_clients.add(record.toMap());
			}
			parser.close();
		}
		return csv_clients;
	}

	@RequestMapping(headers = { "Accept=multipart/form-data" }, method = POST, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> uploadCSV(@RequestParam("event_name") String eventName,
			@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		String teamID = getStringFilter(TEAM_ID, request);
		String teamName = getStringFilter(TEAM_NAME, request);
		String locationID = getStringFilter(LOCATION_ID, request);

		String entityId = UUID.randomUUID().toString();
		String providerId = getCurrentUser();
		MultimediaDTO multimediaDTO = new MultimediaDTO(entityId.trim(), providerId, file.getContentType().trim(), null,
				FILE_CATEGORY);

		String status = null;
		try {
			List<Map<String, String>> csv_clients = readCSVFile(file);

			UploadValidationBean validationBean = uploadService.validateFieldValues(csv_clients, eventName, globalID);
			if (validationBean.getErrors() != null && validationBean.getErrors().size() > 0) {
				validationBean.setAnalyzedData(null);
				Map<String, Object> map = new HashMap<>();
				map.put("status", "A number of errors were found during validation");
				map.put("summary", validationBean);
				return new ResponseEntity<>(gson.toJson(map), HttpStatus.BAD_REQUEST);
			}

			UniqueIDProvider uniqueIDProvider = new OpenMRSUniqueIDProvider(openmrsIDService,
					validationBean.getRowsToCreate());

			for (Pair<Client, Event> eventClient : validationBean.getAnalyzedData()) {
				Client client = eventClient.getLeft();

				String baseEntityID = client.getBaseEntityId();
				boolean newClient = false;
				if (StringUtils.isBlank(baseEntityID)) {
					baseEntityID = UUID.randomUUID().toString();
					client.setBaseEntityId(baseEntityID);
					newClient = true;
				}

				client.addAttribute(DEFAULT_RESIDENCE, locationID);

				assignClientUniqueID(client, globalID, uniqueIDProvider);
				clientService.addorUpdate(client);

				// update event details
				Event event = (eventClient.getRight() == null) ? new Event() : eventClient.getRight();
				String eventType = newClient ? eventName : "Update " + eventName;
				prepareEvent(event, baseEntityID, providerId, eventType);

				if (StringUtils.isNotBlank(teamID))
					event.setTeamId(teamID);

				if (StringUtils.isNotBlank(teamName))
					event.setTeam(teamName);

				if (StringUtils.isNotBlank(locationID))
					event.setLocationId(locationID);

				// save the event
				eventService.addorUpdateEvent(event);
			}

			Map<String, String> details = new HashMap<>();
			details.put("size", Long.toString(file.getSize()));
			details.put("imported", Integer.toString(validationBean.getRowsToCreate()));
			details.put("updated", Integer.toString(validationBean.getRowsToUpdate()));
			multimediaDTO.withOriginalFileName(file.getOriginalFilename())
					.withDateUploaded(new Date())
					.withSummary(mapper.writeValueAsString(details));

			status = multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());

			return new ResponseEntity<>(gson.toJson(details), HttpStatus.OK);
		}
		catch (Exception e) {
			logger.error("", e);
		}

		return new ResponseEntity<>(gson.toJson(status), HttpStatus.OK);
	}

	@RequestMapping(headers = { "Accept=multipart/form-data" }, method = POST, value = "/validate", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> validateFile(@RequestParam("event_name") String eventName,
			@RequestParam("file") MultipartFile file) {
		try {
			List<Map<String, String>> csv_clients = readCSVFile(file);
			UploadValidationBean validationBean = uploadService.validateFieldValues(csv_clients, eventName, globalID);
			validationBean.setAnalyzedData(null);
			return new ResponseEntity<>(gson.toJson(validationBean), HttpStatus.OK);
		}
		catch (Exception e) {
			logger.error("", e);
		}
		return new ResponseEntity<>(gson.toJson("An error occurred while reading the file"), HttpStatus.EXPECTATION_FAILED);
	}

	private void prepareEvent(Event event, String baseEntityID, String providerId, String eventType) {
		if (event.getFormSubmissionId() == null)
			event.setFormSubmissionId(UUID.randomUUID().toString());

		if (event.getBaseEntityId() == null)
			event.setBaseEntityId(baseEntityID);

		event.setDateCreated(new DateTime());
		event.setEventDate(new DateTime());
		event.setProviderId(providerId);
		event.setEventType(eventType);
		event.setType("Event");
	}

	private void assignClientUniqueID(Client client, String uniqueIDKey, UniqueIDProvider uniqueIDProvider) {
		if (StringUtils.isBlank(client.getIdentifier(uniqueIDKey))) {
			Map<String, String> identifiers = client.getIdentifiers();
			if (identifiers == null)
				identifiers = new LinkedHashMap<>();

			identifiers.put(uniqueIDKey, uniqueIDProvider.getNewUniqueID());

			client.setIdentifiers(identifiers);
		}
	}

	@RequestMapping(value = "/history", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getUploadHistory(HttpServletRequest request) {
		Integer batchSize = getIntegerFilter(BATCH_SIZE, request);
		if (batchSize == null)
			batchSize = 50;

		Integer offset = getIntegerFilter(OFFSET, request);
		if (offset == null)
			offset = 0;

		String providerID = getCurrentUser();

		List<UploadBean> uploadBeans = multimediaRepository.getByProviderID(providerID, FILE_CATEGORY, offset, batchSize)
				.stream()
				.map(multimedia -> {
					UploadBean uploadBean = new UploadBean();
					uploadBean.setFileName(multimedia.getOriginalFileName());
					uploadBean.setIdentifier(multimedia.getCaseId());
					uploadBean.setProviderID(multimedia.getProviderId());
					uploadBean.setUploadDate(multimedia.getDateUploaded());
					uploadBean.setUrl(multimedia.getCaseId() + "." + FILE_CATEGORY);
					return uploadBean;
				})
				.collect(Collectors.toList());

		return new ResponseEntity<>(
				gson.toJson(uploadBeans),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@RequestMapping(value = "/template", method = RequestMethod.GET)
	public void getUploadTemplate(@RequestParam("event_name") String eventName, HttpServletRequest request,
			HttpServletResponse response) {
		String locationID = getStringFilter(LOCATION_ID, request);

		String csvFileName = eventName.replace(" ", "").toLowerCase() + ".csv";
		response.setContentType("text/csv");
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"",
				csvFileName);
		response.setHeader(headerKey, headerValue);

		List<CSVRowConfig> configs = uploadService.getCSVConfig(eventName);

		String[] HEADERS = configs.stream().map(CSVRowConfig::getColumnName)
				.toArray(String[]::new);

		List<String> fieldMappings = configs.stream().map(CSVRowConfig::getFieldMapping)
				.collect(Collectors.toList());

		List<Client> clients = clientService.findAllByAttribute(DEFAULT_RESIDENCE, locationID);

		try (CSVPrinter printer = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
				.withHeader(HEADERS))) {
			for (Client client : clients) {

				JSONObject jsonObject = new JSONObject();
				jsonObject.put("client", new JSONObject(gson.toJson(client)));
				printer.printRecord(JSONCSVUtil.jsonToString(jsonObject, fieldMappings));
			}
		}
		catch (IOException e) {
			logger.error("CSV Export >>> " + e.toString());
		}
	}

	@RequestMapping(value = "/download/{fileName:.+}", method = RequestMethod.GET)
	public void downloadFile(@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException {
		File file = multimediaService
				.retrieveFile(multiMediaDir + File.separator + FILE_CATEGORY + File.separator + fileName.trim());
		if (file != null) {
			response.setContentType("text/csv");
			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"",
					fileName);
			response.setHeader(headerKey, headerValue);
			response.setContentLength((int) file.length());
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		} else {
			String errorMessage = "Sorry. The file you are looking for does not exist";
			logger.info(errorMessage);
			OutputStream outputStream = response.getOutputStream();
			outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
			outputStream.close();
		}
	}

	private String getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			return ((UserDetails) principal).getUsername();
		} else {
			return principal.toString();
		}
	}
}
