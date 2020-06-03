package org.opensrp.web.rest;



import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.opensrp.web.Constants.DEFAULT_LIMIT;
import static org.opensrp.web.Constants.LIMIT;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.domain.LocationDetail;
import org.opensrp.domain.PlanDefinition;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.util.DateTypeConverter;
import org.opensrp.util.TaskDateTimeTypeConverter;
import org.opensrp.web.bean.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

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
	
	private PhysicalLocationService locationService;

	private static final String IS_DELETED = "is_deleted";

	private static final String FALSE = "false";

	public static final String OPERATIONAL_AREA_ID = "operational_area_id";
	
	public static final String IDENTIFIERS = "identifiers";
	
	public static final String FIELDS = "fields";
	
	public static final String USERNAME = "username";
	
	@Autowired
	public void setPlanService(PlanService planService) {
		this.planService = planService;
	}
	
	@Autowired
	public void setLocationService(PhysicalLocationService locationService) {
		this.locationService = locationService;
	}
	
	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getPlanByUniqueId(@PathVariable("identifier") String identifier,
	        @RequestParam(value = FIELDS, required = false) List<String> fields) {
		if (identifier == null) {
			return new ResponseEntity<>("Plan Id is required", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(
				gson.toJson(
						planService.getPlansByIdsReturnOptionalFields(Collections.singletonList(identifier), fields)),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

	}
	
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getPlans() {
		return new ResponseEntity<>(gson.toJson(planService.getAllPlans()), RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);

	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> create(@RequestBody String entity) {
		try {
			PlanDefinition plan = gson.fromJson(entity, PlanDefinition.class);
			planService.addPlan(plan);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesn't contain a valid plan representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}
	
	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> update(@RequestBody String entity) {
		try {
			PlanDefinition plan = gson.fromJson(entity, PlanDefinition.class);
			planService.updatePlan(plan);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesn't contain a valid plan representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}
	
	@RequestMapping(value = "/sync", method = RequestMethod.POST, consumes = {
	        MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> syncByServerVersionAndAssignedPlansOnOrganization(
	        @RequestBody PlanSyncRequestWrapper planSyncRequestWrapper, Authentication authentication) {
		
		List<String> operationalAreaIds = planSyncRequestWrapper.getOperationalAreaId();
		String username = null;
		if (authentication != null)
			username = authentication.getName();
		if ((operationalAreaIds==null || operationalAreaIds.isEmpty()) && StringUtils.isBlank(username)) {
			return new ResponseEntity<>("Sync Params missing", RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
		}

		List<PlanDefinition> plans;
		if (planSyncRequestWrapper.getOrganizations() != null && !planSyncRequestWrapper.getOrganizations().isEmpty()) {
			plans = planService.getPlansByOrganizationsAndServerVersion(planSyncRequestWrapper.organizations,
					planSyncRequestWrapper.getServerVersion());
		} else if (username != null) {
			plans = planService.getPlansByUsernameAndServerVersion(username, planSyncRequestWrapper.getServerVersion());
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(gson.toJson(plans), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

	}
	
	// here for backward compatibility
	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> syncByServerVersionAndOperationalAreaTwo(HttpServletRequest request,
	        @RequestParam(value = OPERATIONAL_AREA_ID) List<String> operationalAreaIds) {
		String serverVersion = getStringFilter(SERVER_VERSIOIN, request);
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(serverVersion);
		}
		catch (NumberFormatException e) {
			logger.error("server version not a number");
		}
		if (operationalAreaIds.isEmpty()) {
			return new ResponseEntity<>("Juridiction Ids required", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(
				gson.toJson(
						planService.getPlansByServerVersionAndOperationalArea(currentServerVersion, operationalAreaIds)),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}
	
	/**
	 * This method provides an API endpoint that searches for plans using a list of provided plan
	 * identifiers and returns a subset of fields determined by the list of provided fields If no
	 * plan identifier(s) are provided the method returns all available plans If no fields are
	 * provided the method returns all the available fields
	 * 
	 * @param identifiers list of plan identifiers
	 * @param fields list of fields to return
	 * @return plan definitions whose identifiers match the provided params
	 */
	@RequestMapping(value = "/findByIdsWithOptionalFields", method = RequestMethod.GET, produces = {
	        MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> findByIdentifiersReturnOptionalFields(HttpServletRequest request,
	        @RequestParam(value = IDENTIFIERS) List<String> identifiers,
	        @RequestParam(value = FIELDS, required = false) List<String> fields) {

		if (fields != null && !fields.isEmpty()) {
			for (String fieldName : fields) {
				if (!doesObjectContainField(new PlanDefinition(), fieldName)) {
					return new ResponseEntity<>(fieldName + " field is invalid", HttpStatus.BAD_REQUEST);
				}
			}
		}
		return new ResponseEntity<>(gson.toJson(planService.getPlansByIdsReturnOptionalFields(identifiers, fields)),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}
	
	/**
	 * This method provides an endpoint that searches for location details i.e. identifier and name
	 * using a provided plan identifier
	 *
	 * @param planIdentifier plan identifier
	 * @return A list of location names and identifiers
	 */
	@RequestMapping(value = "/findLocationNames/{planIdentifier}", method = RequestMethod.GET, produces = {
	        MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<LocationDetail>> findLocationDetailsByPlanId(
	        @PathVariable("planIdentifier") String planIdentifier) {

		return new ResponseEntity<>(locationService.findLocationDetailsByPlanId(planIdentifier),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	/**
	 * Fetch plans ordered by serverVersion ascending
	 *
	 * @param serverVersion serverVersion using to filter by
	 * @param limit upper limit on number os plas to fetch
	 * @return A list of plan definitions
	 */
	@RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getAll(
			@RequestParam(value = SERVER_VERSIOIN)  long serverVersion,
			@RequestParam(value = LIMIT, required = false)  Integer limit) {

		Integer pageLimit = limit == null ? DEFAULT_LIMIT : limit;
		return new ResponseEntity<>(gson.toJson(planService.getAllPlans(serverVersion, pageLimit)),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

	}

	/**
	 * This methods provides an API endpoint that searches for all plan Identifiers
	 *
	 * @return A list of plan Identifiers
	 */
	@RequestMapping(value = "/findIds", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Identifier> findIds(@RequestParam(value = SERVER_VERSIOIN, required = false)  long serverVersion,
										  @RequestParam(value = IS_DELETED, defaultValue = FALSE, required = false ) boolean isDeleted) {

		Pair<List<String>, Long> planIdsPair = planService.findAllIds(serverVersion, DEFAULT_GET_ALL_IDS_LIMIT, isDeleted);
		Identifier identifiers = new Identifier();
		identifiers.setIdentifiers(planIdsPair.getLeft());
		identifiers.setLastServerVersion(planIdsPair.getRight());

		return new ResponseEntity<>(identifiers, HttpStatus.OK);

	}

	/**
	 * This method provides an API endpoint that searches for plans using a
	 *  provided username
	 *
	 * @param username
	 * @return plan definitions whose identifiers match the provided param
	 */
	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> fetchPlansForUser(@PathVariable(USERNAME) String username,
			@RequestParam(value = SERVER_VERSIOIN, required = false) String serverVersion) {
		
		if (StringUtils.isBlank(username)) {
			return new ResponseEntity<>("Request Param missing", RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
		}
		
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(serverVersion);
		}
		catch (NumberFormatException e) {
			logger.error("server version not a number");
		}

		List<PlanDefinition> plans;

		plans = planService.getPlansByUsernameAndServerVersion(username, currentServerVersion);

		return new ResponseEntity<>(gson.toJson(plans), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

	}
	
	public boolean doesObjectContainField(Object object, String fieldName) {
		Class<?> objectClass = object.getClass();
		for (Field field : objectClass.getDeclaredFields()) {
			SerializedName sName = field.getAnnotation(SerializedName.class);
			if (sName != null && sName.value().equals(fieldName))
				return true;
			else if (sName == null && field.getName().equals(fieldName)) {
				return true;
			}
		}
		return false;
	}
	
	static class PlanSyncRequestWrapper {
		
		@JsonProperty("operational_area_id")
		private List<String> operationalAreaId;
		
		@JsonProperty
		private long serverVersion;
		
		@JsonProperty
		private List<Long> organizations;
		
		public List<String> getOperationalAreaId() {
			return operationalAreaId;
		}
		
		public long getServerVersion() {
			return serverVersion;
		}
		
		public List<Long> getOrganizations() {
			return organizations;
		}
	}
	
}
