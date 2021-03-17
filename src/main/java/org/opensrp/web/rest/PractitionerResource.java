package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.search.BaseSearchBean;
import org.opensrp.search.PractitionerSearchBean;
import org.opensrp.service.PractitionerService;
import org.opensrp.util.DateTypeConverter;
import org.smartregister.domain.Practitioner;
import org.smartregister.utils.TaskDateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.opensrp.web.Constants.ORDER_BY_FIELD_NAME;
import static org.opensrp.web.Constants.ORDER_BY_TYPE;
import static org.opensrp.web.Constants.PAGE_NUMBER;
import static org.opensrp.web.Constants.PAGE_SIZE;

@Controller
@RequestMapping(value = "/rest/practitioner")
public class PractitionerResource {

    private static Logger logger = LogManager.getLogger(PractitionerResource.class.toString());

    public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
            .registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();

    private PractitionerService practitionerService;

    public static final String IDENTIFIER = "identifier";

    public static final String USER_ID = "userId";

    public static final String GET_PRACTITIONER_BY_USER_URL = "/user/{userId}";

    @Autowired
    public void setPractitionerService(PractitionerService practitionerService) {
        this.practitionerService = practitionerService;
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPractitionerByUniqueId(@PathVariable(IDENTIFIER) String identifier) {
        if (StringUtils.isBlank(identifier)) {
            return new ResponseEntity<>(gson.toJson("Practitioner Id is required"), RestUtils.getJSONUTF8Headers(),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(gson.toJson(
                practitionerService.getPractitioner(identifier)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPractitioners(@RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
            @RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName) {

        PractitionerSearchBean practitionerSearchBean = createPractitionerSearchBean(pageNumber, pageSize, orderByType,
                orderByFieldName);
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
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPractitionerByUser(@PathVariable(value = USER_ID) String userId) {
        if (StringUtils.isNotBlank(userId)) {
            return new ResponseEntity<>(gson.toJson(practitionerService.getPractitionerByUserId(userId)),
                    RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(gson.toJson("The User Id is required"),
                    RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> create(@RequestBody String entity) {
        try {
            Practitioner practitioner = gson.fromJson(entity, Practitioner.class);
            practitionerService.addOrUpdatePractitioner(practitioner);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> update(@RequestBody String entity) {
        try {
            Practitioner practitioner = gson.fromJson(entity, Practitioner.class);
            practitionerService.addOrUpdatePractitioner(practitioner);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/delete/{identifier}", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> delete(@PathVariable("identifier") String identifier) {
        try {
            practitionerService.deletePractitioner(identifier);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "/report-to", produces = { MediaType.APPLICATION_JSON_VALUE })
    public List<Practitioner> getPractitionersByPractitionerRoleIdentifierAndCode(@RequestParam(value = "practitionerIdentifier") String practitionerIdentifier,
            @RequestParam(value = "code") String code) {

        return practitionerService.getAssignedPractitionersByIdentifierAndCode(practitionerIdentifier, code);
    }

	private PractitionerSearchBean createPractitionerSearchBean(Integer pageNumber, Integer pageSize, String orderByType,
            String orderByFieldName) {

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
                .build();

    }

}
