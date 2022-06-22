package org.opensrp.web.rest;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.Campaign;
import org.opensrp.service.CampaignService;
import org.opensrp.util.DateTypeConverter;
import org.smartregister.utils.TaskDateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/rest/campaign")
public class CampaignResource {
    public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
            .registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();
    private static Logger logger = LogManager.getLogger(CampaignResource.class.toString());
    private CampaignService campaignService;

    @Autowired
    public void setCampaignService(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getByUniqueId(@PathVariable("identifier") String identifier) {
        return new ResponseEntity<>(gson.toJson(campaignService.getCampaign(identifier)), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getCampaigns() {
        return new ResponseEntity<>(gson.toJson(campaignService.getAllCampaigns()), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<HttpStatus> create(@RequestBody String entity) {
        try {
            Campaign campaign = gson.fromJson(entity, Campaign.class);
            campaignService.addCampaign(campaign);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesnt contain a valid campaign representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<HttpStatus> update(@RequestBody String entity) {
        try {
            Campaign campaign = gson.fromJson(entity, Campaign.class);
            campaignService.updateCampaign(campaign);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (JsonSyntaxException e) {
            logger.error("The request doesnt contain a valid campaign representation", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/sync", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> syncByServerVersion(HttpServletRequest request) {
        String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
        long currentServerVersion = 0;
        try {
            currentServerVersion = Long.parseLong(serverVersion);
        } catch (NumberFormatException e) {
            logger.error("server version not a number");
        }
        return new ResponseEntity<>(gson.toJson(campaignService.getCampaignsByServerVersion(currentServerVersion)),
                HttpStatus.OK);
    }

}
