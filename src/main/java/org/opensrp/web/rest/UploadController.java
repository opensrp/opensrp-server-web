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
import org.opensrp.domain.CSVRowConfig;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.repository.MultimediaRepository;
import org.opensrp.service.*;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.bean.UploadBean;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(value = "/rest/upload")
public class UploadController {

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    private static Logger logger = LoggerFactory.getLogger(UploadController.class.toString());
    private static ObjectMapper mapper = new ObjectMapper();

    @Value("#{opensrp['opensrp.config.global_id']}")
    private String globalID;

    public static final String BATCH_SIZE = "batch_size";
    public static final String OFFSET = "offset";
    public static final String TEAM_ID = "team_id";
    public static final String PROVIDER_ID = "provider_id";
    public static final String LOCATION = "location";
    public static final String LOCATION_HIERARCHY = "location_hierarchy";
    public static final String FILE_CATEGORY = "csv";

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

    @RequestMapping(headers = {"Accept=multipart/form-data"}, method = POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> uploadCSV(@RequestParam("event_name") String eventName, @RequestParam("file") MultipartFile file) {

        String entityId = UUID.randomUUID().toString();
        String providerId = getCurrentUser();
        MultimediaDTO multimediaDTO = new MultimediaDTO(entityId.trim(), providerId, file.getContentType().trim(), null, FILE_CATEGORY);

        String status = null;
        try {
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

            // get clients and events
            List<Pair<Client, Event>> event_clients = uploadService.getClientFromCSVBytes(csv_clients, eventName);

            int imported = 0;
            int updated = 0;

            // create an event and client
            for (Pair<Client, Event> eventClient : event_clients) {
                Client client = eventClient.getLeft();
                boolean newClient = false;

                // find client by unique id
                String baseEntityID = getClientsBaseEntityID(client, globalID);
                if (StringUtils.isBlank(baseEntityID)) {
                    baseEntityID = UUID.randomUUID().toString();
                    newClient = true;
                }
                client.setBaseEntityId(baseEntityID);
                if (newClient) {
                    assignClientUniqueID(client, globalID);
                    imported++;
                }else{
                    updated++;
                }
                clientService.addorUpdate(client);

                // update event details
                Event event = (eventClient.getRight() == null) ? new Event() : eventClient.getRight();
                String eventType = newClient ? eventName : "Update " + eventName;
                prepareEvent(event, baseEntityID, providerId, eventType);

                // save the event
                eventService.addorUpdateEvent(event);
            }

            Map<String, String> details = new HashMap<>();
            details.put("size", Long.toString(file.getSize()));
            details.put("imported", Integer.toString(imported));
            details.put("updated", Integer.toString(updated));
            multimediaDTO.withOriginalFileName(file.getOriginalFilename())
                    .withDateUploaded(new Date())
                    .withSummary(mapper.writeValueAsString(details));

            status = multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());

            return new ResponseEntity<>(gson.toJson(details), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("", e);
        }

        return new ResponseEntity<>(gson.toJson(status), HttpStatus.OK);
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
    }

    private String getClientsBaseEntityID(Client client, String uniqueIDKey) {
        if (client == null || StringUtils.isBlank(client.getIdentifier(uniqueIDKey)))
            return null;

        String uniqueID = client.getIdentifier(uniqueIDKey);
        if (StringUtils.isNotBlank(uniqueID)) {
            Client existing = clientService.find(uniqueID);
            if (existing != null)
                return existing.getId();
        }
        return null;
    }

    private void assignClientUniqueID(Client client, String uniqueIDKey) {
        if (StringUtils.isBlank(client.getIdentifier(uniqueIDKey))) {
            Map<String, String> identifiers = client.getIdentifiers();
            if (identifiers == null)
                identifiers = new LinkedHashMap<>();

            identifiers.put(uniqueIDKey, getNewUniqueID());

            client.setIdentifiers(identifiers);
        }
    }

    private String getNewUniqueID() {
        List<String> openMRSIDs = this.openmrsIDService.downloadOpenmrsIds(1);
        return openMRSIDs.get(0);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void validate(@RequestParam("event_name") String eventName, @RequestParam("file") MultipartFile file) {

    }

    @RequestMapping(value = "/history", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
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
                    uploadBean.setUrl(multimedia.getCaseId());
                    return uploadBean;
                })
                .collect(Collectors.toList());

        return new ResponseEntity<>(
                gson.toJson(uploadBeans),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/template", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void getUploadTemplate(@RequestParam("event_name") String eventName, HttpServletRequest request, HttpServletResponse response) {
        String teamID = getStringFilter(TEAM_ID, request);
        String providerID = getStringFilter(PROVIDER_ID, request);
        String locationID = getStringFilter(LOCATION, request);
        String locationHierarchy = getStringFilter(LOCATION_HIERARCHY, request);

        String csvFileName = eventName.replace(" ", "").toLowerCase() + ".csv";
        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                csvFileName);
        response.setHeader(headerKey, headerValue);

        String[] HEADERS = uploadService.getCSVConfig(eventName)
                .stream().map(CSVRowConfig::getColumnName)
                .toArray(String[]::new);

        try (CSVPrinter printer = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                .withHeader(HEADERS))) {
            /*
            AUTHOR_BOOK_MAP.forEach((author, title) -> {
                printer.printRecord(author, title);
            });
             */
        } catch (IOException e) {
            logger.error("CSV Export >>> " + e.toString());
        }
    }

    @RequestMapping(value = "/download/{fileName:.+}", method = RequestMethod.GET)
    public void downloadFile(HttpServletRequest request) {

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
