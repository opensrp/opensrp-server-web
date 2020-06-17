package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.opensrp.util.JSONCSVUtil;
import org.opensrp.web.bean.UploadBean;
import org.opensrp.web.exceptions.BusinessLogicException;
import org.opensrp.web.utils.OpenMRSUniqueIDProvider;
import org.opensrp.web.utils.UniqueIDProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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

import static org.opensrp.web.controller.MultimediaController.FILE_NAME_ERROR_MESSAGE;
import static org.opensrp.web.utils.MultimediaUtil.hasSpecialCharacters;

@Controller
@RequestMapping(value = "/rest/upload")
public class UploadController {

	private ObjectMapper objectMapper;

	private static final Logger logger = LoggerFactory.getLogger(UploadController.class.toString());

	@Value("#{opensrp['opensrp.config.global_id'] ?: 'opensrp_id'}")
	private String globalID;

	@Value("#{opensrp['multimedia.directory.name']}")
	private String multiMediaDir;

	public static final String EVENT_NAME_ERROR_MESSAGE = "Sorry. Event name should not contain any special character";

	public static final String FILE_CATEGORY = "csv";

	public static final String DEFAULT_RESIDENCE = "default_residence";

	private MultimediaService multimediaService;

	private UploadService uploadService;

	private MultimediaRepository multimediaRepository;

	private OpenmrsIDService openmrsIDService;

	private ClientService clientService;

	private EventService eventService;

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

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

	@PostMapping(headers = { "Accept=multipart/form-data" }, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> uploadCSV(@RequestParam("event_name") String eventName,
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "team_id", required = false) String teamID,
			@RequestParam(value = "team_name", required = false) String teamName,
			@RequestParam(value = "location_id", required = false) String locationID,
			Authentication authentication
	) throws IOException,
			BusinessLogicException {
		if (hasSpecialCharacters(eventName)) {
			logger.error(EVENT_NAME_ERROR_MESSAGE);
			throw new IllegalArgumentException(EVENT_NAME_ERROR_MESSAGE);
		}

		String entityId = UUID.randomUUID().toString();
		String providerId = authentication.getName();
		MultimediaDTO multimediaDTO = new MultimediaDTO(entityId.trim(), providerId, file.getContentType().trim(), null,
				FILE_CATEGORY);

		List<Map<String, String>> csvClients = readCSVFile(file);

		UploadValidationBean validationBean = uploadService.validateFieldValues(csvClients, eventName, globalID);
		if (validationBean.getErrors() != null && validationBean.getErrors().size() > 0) {
			validationBean.setAnalyzedData(null);
			throw new BusinessLogicException(objectMapper.writeValueAsString(validationBean));
		}

		saveClients(validationBean,locationID,providerId,eventName,teamID,teamName);

		Map<String, Object> details = new HashMap<>();
		details.put("size", Long.toString(file.getSize()));
		details.put("imported", Integer.toString(validationBean.getRowsToCreate()));
		details.put("updated", Integer.toString(validationBean.getRowsToUpdate()));
		multimediaDTO.withOriginalFileName(file.getOriginalFilename())
				.withDateUploaded(new Date())
				.withSummary(objectMapper.writeValueAsString(details));

		multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());

		return details;
	}

	private void saveClients(UploadValidationBean validationBean,
			String locationID,
			String providerId,
			String eventName,
			String teamID,
			String teamName){
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
	}

	@PostMapping(headers = { "Accept=multipart/form-data" }, value = "/validate", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public UploadValidationBean validateFile(@RequestParam("event_name") String eventName,
			@RequestParam("file") MultipartFile file) throws IOException {
		if (hasSpecialCharacters(eventName)) {
			logger.error(EVENT_NAME_ERROR_MESSAGE);
			throw new IllegalArgumentException(EVENT_NAME_ERROR_MESSAGE);
		}

		List<Map<String, String>> csvClients = readCSVFile(file);
		UploadValidationBean validationBean = uploadService.validateFieldValues(csvClients, eventName, globalID);
		validationBean.setAnalyzedData(null);
		return validationBean;
	}

	private void prepareEvent(Event event, String baseEntityID, String providerId, String eventType) {
		if (StringUtils.isBlank(event.getFormSubmissionId()))
			event.setFormSubmissionId(UUID.randomUUID().toString());

		if (StringUtils.isBlank(event.getBaseEntityId()))
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

	@GetMapping(value = "/history", produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<UploadBean> getUploadHistory(
			@RequestParam(value = "batch_size", required = false, defaultValue = "50") Integer batchSize,
			@RequestParam(value = "offset", required = false, defaultValue = "0") Integer offset,
			Authentication authentication
	) {
		return multimediaRepository.getByProviderID(authentication.getName(), FILE_CATEGORY, offset, batchSize)
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
	}

	@GetMapping(value = "/template")
	public void getUploadTemplate(HttpServletResponse response,
			@RequestParam("event_name") String eventName,
			@RequestParam(value = "location_id", required = false) String locationID) throws IOException {

		if (hasSpecialCharacters(eventName)) {
			logger.error(EVENT_NAME_ERROR_MESSAGE);
			throw new IllegalArgumentException(EVENT_NAME_ERROR_MESSAGE);
		}

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
				jsonObject.put("client", new JSONObject(objectMapper.writeValueAsString(client)));
				printer.printRecord(JSONCSVUtil.jsonToString(jsonObject, fieldMappings));
			}
		}
	}

	@GetMapping(value = "/download/{fileName:.+}")
	public void downloadFile(@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException {
		if (StringUtils.isBlank(fileName))
			throw new IllegalArgumentException("Missing file name");

		if (hasSpecialCharacters(fileName)) {
			logger.error(FILE_NAME_ERROR_MESSAGE);
			throw new IllegalArgumentException(FILE_NAME_ERROR_MESSAGE);
		}

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

}
