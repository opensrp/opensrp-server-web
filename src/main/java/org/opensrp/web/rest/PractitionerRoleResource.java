package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.search.BaseSearchBean;
import org.opensrp.search.PractitionerRoleSearchBean;
import org.opensrp.service.PractitionerRoleService;
import org.opensrp.util.DateTypeConverter;
import org.smartregister.domain.PractitionerRole;
import org.smartregister.utils.TaskDateTimeTypeConverter;
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
@RequestMapping(value = "/rest/practitionerRole")
public class PractitionerRoleResource {

    public static final String IDENTIFIER = "identifier";
    private static final Logger logger = LogManager.getLogger(PractitionerRoleResource.class.toString());
    public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
            .registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();
    private PractitionerRoleService practitionerRoleService;

    @Autowired
    public void setPractitionerRoleService(PractitionerRoleService practitionerRoleService) {
        this.practitionerRoleService = practitionerRoleService;
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getPractitionerRoleByUniqueId(@PathVariable(IDENTIFIER) String identifier) {
        if (StringUtils.isBlank(identifier)) {
            return new ResponseEntity<>("Practitioner Role Id is required", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(gson.toJson(
                practitionerRoleService.getPractitionerRole(identifier)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getPractitionerRoles(@RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
                                                       @RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
                                                       @RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
                                                       @RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName) {

        PractitionerRoleSearchBean practitionerRoleSearchBean = createPractitionerRoleSearchBean(pageNumber, pageSize, orderByType, orderByFieldName);
        return new ResponseEntity<>(gson.toJson(
                practitionerRoleService.getAllPractitionerRoles(practitionerRoleSearchBean)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> create(@RequestBody String entity) {
        return savePractitionerRole(entity);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> update(@RequestBody String entity) {
        return savePractitionerRole(entity);
    }

    private ResponseEntity<String> savePractitionerRole(@RequestBody String entity) {
        try {
            PractitionerRole practitionerRole = gson.fromJson(entity, PractitionerRole.class);
            practitionerRoleService.addOrUpdatePractitionerRole(practitionerRole);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner role representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> batchSave(@RequestBody String entity) {
        try {
            Set<String> unprocessedIds = new HashSet<>();
            Type listType = new TypeToken<List<PractitionerRole>>() {
            }.getType();

            List<PractitionerRole> practitionerRoles = gson.fromJson(entity, listType);

            for (PractitionerRole practitionerRole : practitionerRoles) {
                try {
                    practitionerRoleService.addOrUpdatePractitionerRole(practitionerRole);
                } catch (Exception exception) {
                    logger.error(exception.getMessage(), exception);
                    unprocessedIds.add(practitionerRole.getIdentifier());
                }
            }
            if (unprocessedIds.isEmpty())
                return new ResponseEntity<>("All Practitioner Roles processed", HttpStatus.CREATED);
            else
                return new ResponseEntity<>("Practitioner IDs NOT processed: " + String.join(",", unprocessedIds),
                        HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner role representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/delete/{identifier}", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> delete(@PathVariable("identifier") String identifier) {
        try {
            practitionerRoleService.deletePractitionerRole(identifier);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/deleteByPractitioner", method = RequestMethod.DELETE, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteByPractitioner(@RequestParam(value = "organization", required = true) String organizationIdentifier,
                                                       @RequestParam(value = "practitioner", required = true) String practitionerIdentifier) {
        try {
            practitionerRoleService.deletePractitionerRole(organizationIdentifier, practitionerIdentifier);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private PractitionerRoleSearchBean createPractitionerRoleSearchBean(Integer pageNumber, Integer pageSize,
                                                                        String orderByType, String orderByFieldName) {
        BaseSearchBean.OrderByType orderByTypeEnum;
        BaseSearchBean.FieldName fieldName;
        orderByTypeEnum = orderByType != null ? BaseSearchBean.OrderByType.valueOf(orderByType) : BaseSearchBean.OrderByType.DESC;
        fieldName = orderByFieldName != null ? BaseSearchBean.FieldName.valueOf(orderByFieldName) : BaseSearchBean.FieldName.id;

        return PractitionerRoleSearchBean.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .orderByType(orderByTypeEnum)
                .orderByFieldName(fieldName).build();
    }

    @GetMapping(value = "/count")
    public ResponseEntity<Long> getAllPractitionerRolesCount() {
        try {
            return new ResponseEntity<>(practitionerRoleService.countAllPractitionerRoles(), HttpStatus.OK);
        } catch (Exception exception) {
            logger.error("Error getting practitioners role count", exception);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
