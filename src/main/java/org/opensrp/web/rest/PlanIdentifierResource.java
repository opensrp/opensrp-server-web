package org.opensrp.web.rest;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Brian Mwasi
 */

@Controller
@RequestMapping(value = "/rest/planIdentifiers")
public class PlanIdentifierResource {

	private static Logger logger = LoggerFactory.getLogger(PlanIdentifierResource.class.toString());

	public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
			.registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();

	private PlanService planService;

	@Autowired
	public void setPlanService(PlanService planService) {
		this.planService = planService;
	}

	@RequestMapping(value = "/getByUserName/{userName}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getPlanIdentifiersByUserName(@PathVariable("userName") String userName) {
		try {
			return new ResponseEntity<>(gson.toJson(planService.getPlanIdentifiersByUsername(userName)),
					RestUtils.getJSONUTF8Headers(),
					HttpStatus.OK);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
