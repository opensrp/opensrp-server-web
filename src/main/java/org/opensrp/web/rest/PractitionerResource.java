package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.domain.Practitioner;
import org.opensrp.search.BaseSearchBean;
import org.opensrp.search.PractitionerSearchBean;
import org.opensrp.service.PractitionerService;
import org.opensrp.util.DateTypeConverter;
import org.smartregister.utils.TaskDateTimeTypeConverter;
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
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/rest/practitioner")
public class PractitionerResource {

    private static Logger logger = LoggerFactory.getLogger(PractitionerResource.class.toString());

    public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
            .registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();

    private PractitionerService practitionerService;

    public static final String IDENTIFIER ="identifier";

    public static final String PAGE_NUMBER = "pageNumber";

    public static final String PAGE_SIZE = "pageSize";

    public static final String ORDER_BY_TYPE = "orderByType";

    public static final String ORDER_BY_FIELD_NAME = "orderByFieldName";


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
        return new ResponseEntity<>(gson.toJson(
                practitionerService.getPractitioner(identifier)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPractitioners(@RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
            @RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName) {

        PractitionerSearchBean practitionerSearchBean = createPractitionerSearchBean(pageNumber, pageSize, orderByType, orderByFieldName);
        return new ResponseEntity<>(gson.toJson(
                practitionerService.getAllPractitioners(practitionerSearchBean)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<String> create(@RequestBody String entity) {
        try {
            Practitioner practitioner = gson.fromJson(entity, Practitioner.class);
            practitionerService.addOrUpdatePractitioner(practitioner);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation" + entity);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }  catch (IllegalArgumentException e) {
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
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid practitioner representation" + entity);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }  catch (IllegalArgumentException e) {
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
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private PractitionerSearchBean createPractitionerSearchBean(Integer pageNumber, Integer pageSize, String orderByType,
            String orderByFieldName) {

        BaseSearchBean.OrderByType orderByTypeEnum;
        BaseSearchBean.FieldName fieldName;
        orderByTypeEnum = orderByType != null ? BaseSearchBean.OrderByType.valueOf(orderByType) : BaseSearchBean.OrderByType.DESC;
        fieldName = orderByFieldName != null ? BaseSearchBean.FieldName.valueOf(orderByFieldName) : BaseSearchBean.FieldName.id;

        return PractitionerSearchBean.builder()
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .orderByType(orderByTypeEnum)
                .orderByFieldName(fieldName).build();

    }

}
