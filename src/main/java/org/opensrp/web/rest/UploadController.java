package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.opensrp.domain.CSVRowConfig;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.repository.MultimediaRepository;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.UploadService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.bean.UploadBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public static final String BATCH_SIZE = "batch_size";
    public static final String OFFSET = "offset";
    public static final String EVENT_NAME = "event_name";
    public static final String TEAM_ID = "team_id";
    public static final String PROVIDER_ID = "provider_id";
    public static final String LOCATION = "location";
    public static final String LOCATION_HIERARCHY = "location_hierarchy";
    public static final String FILE_CATEGORY = "csv";

    private MultimediaService multimediaService;
    private UploadService uploadService;
    private MultimediaRepository multimediaRepository;

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

    @RequestMapping(headers = {"Accept=multipart/form-data"}, method = POST)
    public ResponseEntity<String> uploadCSV(@RequestParam("event_name") String eventName, @RequestParam("file") MultipartFile file) {

        String entityId = UUID.randomUUID().toString();
        String providerId = getCurrentUser();
        MultimediaDTO multimediaDTO = new MultimediaDTO(entityId.trim(), providerId, file.getContentType().trim(), null, FILE_CATEGORY);

        String status = null;
        try {
            Map<String, String> details = new HashMap<>();
            details.put("size", Long.toString(file.getSize()));
            multimediaDTO.withOriginalFileName(file.getOriginalFilename())
                    .withDateUploaded(new Date())
                    .withSummary(mapper.writeValueAsString(details));

            status = multimediaService.saveFile(multimediaDTO, file.getBytes(), file.getOriginalFilename());
        } catch (Exception e) {
            logger.error("", e);
        }

        return new ResponseEntity<>(new Gson().toJson(status), HttpStatus.OK);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public void validate(@RequestParam("file") MultipartFile file) {

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
    public ResponseEntity<String>  getUploadTemplate(HttpServletRequest request) {
        String eventName = getStringFilter(EVENT_NAME, request);
        String teamID = getStringFilter(TEAM_ID, request);
        String providerID = getStringFilter(PROVIDER_ID, request);
        String locationID = getStringFilter(LOCATION, request);
        String locationHierarchy = getStringFilter(LOCATION_HIERARCHY, request);

        List<CSVRowConfig> values = uploadService.getCSVConfig(eventName);


        return new ResponseEntity<>(
                gson.toJson(values),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
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
