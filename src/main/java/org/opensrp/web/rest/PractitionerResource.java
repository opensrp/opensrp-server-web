package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.domain.Practitioner;
import org.opensrp.service.PractitionerService;
import org.opensrp.util.DateTypeConverter;
import org.opensrp.util.TaskDateTimeTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/rest/practitioner")
public class PractitionerResource {

    private static Logger logger = LoggerFactory.getLogger(PractitionerResource.class.toString());

    public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
            .registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();

    private PractitionerService practitionerService;

    public static final String IDENTIFIER ="identifier";

    @Autowired
    public void setPractitionerService(PractitionerService practitionerService) {
        this.practitionerService = practitionerService;
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPractitionerByUniqueId(@PathVariable(IDENTIFIER) String identifier) {
        if (StringUtils.isBlank(identifier)) {
            return new ResponseEntity<>("Practitioner Id is required", HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(gson.toJson(
                    practitionerService.getPractitioner(identifier)),
                    RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPractitioners() {
        try {
            return new ResponseEntity<>(gson.toJson(
                    practitionerService.getAllPractitioners()),
                    RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<HttpStatus> create(@RequestBody String entity) {
        try {
            Practitioner practitioner = gson.fromJson(entity, Practitioner.class);
            practitionerService.addOrUpdatePractitioner(practitioner);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation" + entity);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<HttpStatus> update(@RequestBody String entity) {
        try {
            Practitioner practitioner = gson.fromJson(entity, Practitioner.class);
            practitionerService.addOrUpdatePractitioner(practitioner);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation" + entity);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<HttpStatus> delete(@RequestBody String entity) {
        try {
            Practitioner practitioner = gson.fromJson(entity, Practitioner.class);
            practitionerService.deletePractitioner(practitioner);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation" + entity);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
