package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.opensrp.search.BaseSearchBean;
import org.opensrp.search.PractitionerSearchBean;
import org.opensrp.service.PractitionerService;
import org.smartregister.domain.Practitioner;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opensrp.web.Constants.*;

@Controller
@RequestMapping(value = "/rest/practitioner")
public class PractitionerResource {

    public static final String IDENTIFIER = "identifier";
    public static final String USER_ID = "userId";
    public static final String GET_PRACTITIONER_BY_USER_URL = "/user/{userId}";
    private static final Logger logger = LogManager.getLogger(PractitionerResource.class.toString());
    public static Gson gson = new GsonBuilder().setDateFormat(DATETIME_IN_UTC_FORMAT_STRING)
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
    private PractitionerService practitionerService;

    @Autowired
    public void setPractitionerService(PractitionerService practitionerService) {
        this.practitionerService = practitionerService;
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getPractitionerByUniqueId(@PathVariable(IDENTIFIER) String identifier) {
        if (StringUtils.isBlank(identifier)) {
            return new ResponseEntity<>(gson.toJson("Practitioner Id is required"), RestUtils.getJSONUTF8Headers(),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(gson.toJson(
                practitionerService.getPractitioner(identifier)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getPractitioners(@RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
                                                   @RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
                                                   @RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
                                                   @RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName,
                                                   @RequestParam(value = SERVER_VERSION, required = false) String serverVersionParam) {

        Long serverVersion = null;
        if (serverVersionParam != null) {
            serverVersion = Long.parseLong(serverVersionParam);
        }

        PractitionerSearchBean practitionerSearchBean = createPractitionerSearchBean(pageNumber, pageSize, orderByType,
                orderByFieldName, serverVersion);
        return new ResponseEntity<>(gson.toJson(
                practitionerService.getAllPractitioners(practitionerSearchBean)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    /**
     * Gets a practitioner using the user id
     *
     * @param userId {@link String}, User id from Keycloak
     * @return practitioner {@link Practitioner}
     */
    @RequestMapping(value = GET_PRACTITIONER_BY_USER_URL, method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getPractitionerByUser(@PathVariable(value = USER_ID) String userId) {
        if (StringUtils.isNotBlank(userId)) {
            return new ResponseEntity<>(gson.toJson(practitionerService.getPractitionerByUserId(userId)),
                    RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(gson.toJson("The User Id is required"),
                    RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> create(@RequestBody String entity) {
        return savePractitioner(entity);
    }

    private ResponseEntity<String> savePractitioner(@RequestBody String payload) {
        try {
            Practitioner practitioner = gson.fromJson(payload, Practitioner.class);
            practitionerService.addOrUpdatePractitioner(practitioner);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> update(@RequestBody String entity) {
        return savePractitioner(entity);
    }

    @RequestMapping(value = "/delete/{identifier}", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> delete(@PathVariable("identifier") String identifier) {
        try {
            practitionerService.deletePractitioner(identifier);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/report-to", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<Practitioner> getPractitionersByPractitionerRoleIdentifierAndCode(
            @RequestParam(value = "practitionerIdentifier") String practitionerIdentifier,
            @RequestParam(value = "code") String code) {

        return practitionerService.getAssignedPractitionersByIdentifierAndCode(practitionerIdentifier, code);
    }

    private PractitionerSearchBean createPractitionerSearchBean(Integer pageNumber, Integer pageSize, String orderByType,
                                                                String orderByFieldName, Long serverVersion) {

        BaseSearchBean.OrderByType orderByTypeEnum;
        BaseSearchBean.FieldName fieldName;
        orderByTypeEnum =
                orderByType != null ? BaseSearchBean.OrderByType.valueOf(orderByType) : BaseSearchBean.OrderByType.DESC;
        fieldName =
                orderByFieldName != null ? BaseSearchBean.FieldName.valueOf(orderByFieldName) : BaseSearchBean.FieldName.id;

        return PractitionerSearchBean.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .orderByType(orderByTypeEnum)
                .orderByFieldName(fieldName)
                .serverVersion(serverVersion)
                .build();

    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> saveMultiplePractitioners(@RequestBody String payload) {
        try {
            Set<String> unprocessedIds = new HashSet<>();
            Type listType = new TypeToken<List<Practitioner>>() {

            }.getType();
            List<Practitioner> practitioners = gson.fromJson(payload, listType);

            for (Practitioner practitioner : practitioners) {
                try {
                    practitionerService.addOrUpdatePractitioner(practitioner);
                } catch (Exception exception) {
                    logger.error(exception.getMessage(), exception);
                    unprocessedIds.add(practitioner.getIdentifier());
                }
            }

            if (unprocessedIds.isEmpty())
                return new ResponseEntity<>("All Practitioners  processed", HttpStatus.CREATED);
            else
                return new ResponseEntity<>("Practitioners Ids not processed: " + String.join(",", unprocessedIds),
                        HttpStatus.CREATED);

        } catch (JsonSyntaxException e) {
            logger.error("The request doesnt contain a valid practitioner representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/count")
    public ResponseEntity<Long> getAllPractitionersCount() {
        try {
            return new ResponseEntity<>(practitionerService.countAllPractitioners(), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Error getting practitioners count", exception);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
