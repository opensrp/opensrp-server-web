package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.PlanDefinition;
import org.opensrp.service.PlanService;
import org.opensrp.util.DateTypeConverter;
import org.opensrp.util.TaskDateTimeTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

/**
 * @author Vincent Karuri
 */

@Controller
@RequestMapping(value = "/rest/plans")
public class PlanResource {

    private static Logger logger = LoggerFactory.getLogger(PlanResource.class.toString());

    public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
            .registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();

    private PlanService planService;

    public static final String OPERATIONAL_AREA_ID = "operational_area_id";

    @Autowired
    public void setPlanService(PlanService planService) {
        this.planService = planService;
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPlanByUniqueId(@PathVariable("identifier") String identifier) {
        try {
            String result = gson.toJson(planService.getPlan(identifier));
            result = "null".equals(result) ? new JsonObject().toString() : result;
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> getPlans() {
        try {
            return new ResponseEntity<>(gson.toJson(planService.getAllPlans()), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity<HttpStatus> create(@RequestBody String entity) {
        try {
            PlanDefinition plan = gson.fromJson(entity, PlanDefinition.class);
            planService.addPlan(plan);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid plan representation" + entity);
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
            PlanDefinition plan = gson.fromJson(entity, PlanDefinition.class);
            planService.updatePlan(plan);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesn't contain a valid plan representation" + entity);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> syncByServerVersionAndOperationalArea(HttpServletRequest request,
            @RequestParam(value=OPERATIONAL_AREA_ID, required=false) List<String> operationalAreaIds) {
        String serverVersion = getStringFilter(AllConstants.BaseEntity.SERVER_VERSIOIN, request);
        long currentServerVersion = 0;
        try {
            currentServerVersion = Long.parseLong(serverVersion);
        } catch (NumberFormatException e) {
            logger.error("server version not a number");
        }
        try {
            return new ResponseEntity<>(gson.toJson(planService.getPlansByServerVersionAndOperationalArea(currentServerVersion, operationalAreaIds)),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
