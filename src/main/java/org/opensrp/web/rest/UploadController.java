package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.domain.CSVRowConfig;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.repository.MultimediaRepository;
import org.opensrp.search.UploadValidationBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.IdentifierSourceService;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.UniqueIdentifierService;
import org.opensrp.service.UploadService;
import org.opensrp.util.JSONCSVUtil;
import org.opensrp.web.bean.UploadBean;
import org.opensrp.web.exceptions.UploadValidationException;
import org.opensrp.web.uniqueid.UniqueIDProvider;
import org.opensrp.web.uniqueid.UniqueIdentifierProvider;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.domain.PhysicalLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.opensrp.web.controller.MultimediaController.FILE_NAME_ERROR_MESSAGE;
import static org.opensrp.web.utils.MultimediaUtil.hasSpecialCharacters;

@Controller
@RequestMapping(value = "/rest/upload")
public class UploadController {

	private ObjectMapper objectMapper;

	private static final Logger logger = LogManager.getLogger(UploadController.class.toString());

	@Value("#{opensrp['opensrp.config.global_id'] ?: 'opensrp_id'}")
	private String globalID;

	@Value("#{opensrp['opensrp.config.id_source'] ?: '1'}")
	private String IDSource;

	@Value("#{opensrp['multimedia.directory.name']}")
	private String multiMediaDir;

	public static final String EVENT_NAME_ERROR_MESSAGE = "Sorry. Event name should not contain any special character";

	public static final String FILE_CATEGORY = "csv";

	public static final String DEFAULT_RESIDENCE = "default_residence";

	public static final String DEFAULT = "default";

	private MultimediaService multimediaService;

	private UploadService uploadService;

	private MultimediaRepository multimediaRepository;

	private ClientService clientService;

	private EventService eventService;

	private IdentifierSourceService identifierSourceService;

	private UniqueIdentifierService uniqueIdentifierService;

	private PhysicalLocationService physicalLocationService;

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
	public void setIdentifierSourceService(IdentifierSourceService identifierSourceService) {
		this.identifierSourceService = identifierSourceService;
	}

	@Autowired
	public void setUniqueIdentifierService(UniqueIdentifierService uniqueIdentifierService) {
		this.uniqueIdentifierService = uniqueIdentifierService;
	}

	@Autowired
	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	@Autowired
	public void setLocationService(PhysicalLocationService physicalLocationService) {
		this.physicalLocationService = physicalLocationService;
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
			UploadValidationException {
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
			throw new UploadValidationException(objectMapper.writeValueAsString(validationBean));
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
		UniqueIDProvider uniqueIDProvider = new UniqueIdentifierProvider(uniqueIdentifierService, identifierSourceService,
				IDSource,
				validationBean.getRowsToCreate());
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		for (Pair<Client, Event> eventClient : validationBean.getAnalyzedData()) {
			Client client = eventClient.getLeft();

			Client found = clientService.findClient(client);
			client.setBaseEntityId(found.getBaseEntityId());

			boolean newClient = StringUtils.isBlank(client.getBaseEntityId());
			String baseEntityID = StringUtils.defaultIfBlank(client.getBaseEntityId(), UUID.randomUUID().toString());
			client.setBaseEntityId(baseEntityID);

			if (StringUtils.isNotBlank(locationID))
				client.addAttribute(DEFAULT_RESIDENCE, locationID);

			assignClientUniqueID(client, globalID, uniqueIDProvider);
			clientService.addorUpdate(client);

			// update event details
			Event event = (eventClient.getRight() == null) ? new Event() : eventClient.getRight();
			String eventType = newClient ? eventName : "Update " + eventName;
			prepareEvent(event, baseEntityID, providerId, eventType);

			event.setTeamId(StringUtils.defaultIfBlank(teamID,event.getTeamId()));
			event.setTeam(StringUtils.defaultIfBlank(teamName,event.getTeam()));
			event.setLocationId(StringUtils.defaultIfBlank(locationID,event.getLocationId()));

			// save the event
			String userName = RestUtils.currentUser(authentication) != null ? RestUtils.currentUser(authentication).getUsername() : "";
			eventService.addorUpdateEvent(event, userName);
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
		event.setFormSubmissionId(StringUtils.defaultIfBlank(event.getFormSubmissionId(),UUID.randomUUID().toString()));
		event.setBaseEntityId(StringUtils.defaultIfBlank(event.getBaseEntityId(),baseEntityID));
		event.setTeamId(StringUtils.defaultIfBlank(event.getTeamId(),DEFAULT));
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

		Set<String> locations = Arrays.stream(locationID.split(",")).filter(StringUtils::isNotBlank).collect(
				Collectors.toSet());

		List<PhysicalLocation> physicalLocations = physicalLocationService
				.findLocationByIdsWithChildren(false, new HashSet<>(locations), Integer.MAX_VALUE);
		for (PhysicalLocation location : physicalLocations)
			locations.add(location.getId());

		List<PhysicalLocation> structureIds = physicalLocationService
				.findStructuresByParentAndServerVersion(String.join(",", locations), 0);
		for (PhysicalLocation location : structureIds)
			locations.add(location.getId());

		List<Client> clients = new ArrayList<>(clientService.findAllByAttributes(DEFAULT_RESIDENCE, new ArrayList<>(locations)));

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
