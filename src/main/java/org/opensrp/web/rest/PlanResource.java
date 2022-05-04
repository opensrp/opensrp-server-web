package org.opensrp.web.rest;

import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.opensrp.web.Constants.DEFAULT_LIMIT;
import static org.opensrp.web.Constants.LIMIT;
import static org.opensrp.web.Constants.RETURN_COUNT;
import static org.opensrp.web.Constants.TOTAL_RECORDS;
import static org.opensrp.web.Constants.ORDER_BY_FIELD_NAME;
import static org.opensrp.web.Constants.ORDER_BY_TYPE;
import static org.opensrp.web.Constants.PAGE_NUMBER;
import static org.opensrp.web.Constants.PAGE_SIZE;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.lang.reflect.Field;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.domain.LocationDetail;
import org.opensrp.domain.Template;
import org.opensrp.domain.postgres.PlanProcessingStatus;
import org.opensrp.domain.PlanTaskCount;
import org.opensrp.search.PlanSearchBean;
import org.opensrp.service.EventService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanProcessingStatusService;
import org.opensrp.service.PlanService;
import org.opensrp.service.TemplateService;
import org.opensrp.util.DateTypeConverter;
import org.opensrp.util.constants.PlanConstants;
import org.opensrp.util.constants.PlanProcessingStatusConstants;
import org.opensrp.web.Constants;
import org.opensrp.web.bean.Identifier;
import org.opensrp.web.utils.Utils;
import org.smartregister.domain.Event;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.PlanDefinition;
import org.smartregister.utils.TaskDateTimeTypeConverter;
import org.smartregister.utils.TimingRepeatTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
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
	
	private static Logger logger = LogManager.getLogger(PlanResource.class.toString());
	
	public static Gson gson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter("yyyy-MM-dd"))
	        .registerTypeAdapter(LocalDate.class, new DateTypeConverter())
			.registerTypeAdapter(Time.class, new TimingRepeatTimeTypeConverter()).create();
	
	private PlanService planService;
	
	private PhysicalLocationService locationService;

	private PlanProcessingStatusService processingStatusService;

	private EventService eventService;

	private TemplateService templateService;

	private static final String IS_DELETED = "is_deleted";

	private static final String FALSE = "false";

	private static final String TRUE = "true";

	public static final String OPERATIONAL_AREA_ID = "operational_area_id";
	
	public static final String IDENTIFIERS = "identifiers";
	
	public static final String FIELDS = "fields";
	
	public static final String USERNAME = "username";

	public static final String IS_TEMPLATE = "is_template";

	public static final String REVOKE_ASSIGNMENTS = "revoke_assignments";

	public static final String PLAN_STATUS = "planStatus";

	public static final String USE_CONTEXT = "useContext";
	
	public static final String OPENSRP_EVENT_ID = "opensrpEventId";

	@Autowired
	public void setPlanService(PlanService planService) {
		this.planService = planService;
	}
	
	@Autowired
	public void setLocationService(PhysicalLocationService locationService) {
		this.locationService = locationService;
	}

	@Autowired
	public void setProcessingStatusService(PlanProcessingStatusService processingStatusService) {
		this.processingStatusService = processingStatusService;
	}

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	@Autowired
	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}

	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getPlanByUniqueId(@PathVariable("identifier") String identifier,
	        @RequestParam(value = FIELDS, required = false) List<String> fields , @RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam) {
		if (identifier == null) {
			return new ResponseEntity<>("Plan Id is required", HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(
		        gson.toJson(planService.getPlansByIdsReturnOptionalFields(Collections.singletonList(identifier), fields, isTemplateParam)),
		        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

	}
	
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getPlans(@RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam,
			@RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
			@RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName,
			@RequestParam(value = PLAN_STATUS, required = false) String planStatus,
			@RequestParam(value = USE_CONTEXT, required = false)  List<String> useContextList) {

		Map<String, String> useContextFilters = null;
		if (useContextList != null) {
			useContextFilters = new HashMap<>();
			for (String useContext : useContextList) {
				String[] filterArray = useContext.split(":");
				if (filterArray.length == 2) {
					useContextFilters.put(filterArray[0], filterArray[1]);
				}
			}
		}
		PlanSearchBean planSearchBean = createPlanSearchBean(isTemplateParam, pageNumber, pageSize, orderByType, orderByFieldName, planStatus, useContextFilters);
		return new ResponseEntity<>(gson.toJson(planService.getAllPlans(planSearchBean)),RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> create(@RequestBody String entity, Authentication authentication) {
		try {
			PlanDefinition plan = gson.fromJson(entity, PlanDefinition.class);
			
				//check whether a case triggered plan with a given opensrp event id exists
			PlanDefinition.UseContext opensrpEventIdUseContext = getUseContextWithCode(plan, OPENSRP_EVENT_ID);
			if (opensrpEventIdUseContext != null && !isValidCaseTriggeredPlan(opensrpEventIdUseContext)) {
				return new ResponseEntity<>("Case triggered plan with opensrpEventId " + opensrpEventIdUseContext.getValueCodableConcept() + " already exists", HttpStatus.CONFLICT);
			}
				
			planService.addPlan(plan, RestUtils.currentUser(authentication).getUsername());
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesn't contain a valid plan representation",e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> update(@RequestBody String entity, Authentication authentication,
			@RequestParam(value = REVOKE_ASSIGNMENTS, defaultValue = TRUE, required = false) boolean revokeAssignments) {
		try {
			PlanDefinition plan = gson.fromJson(entity, PlanDefinition.class);
			planService.updatePlan(plan, RestUtils.currentUser(authentication).getUsername(), revokeAssignments);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesn't contain a valid plan representation", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

	}
	
	@RequestMapping(value = "/sync", method = RequestMethod.POST, consumes = {
	        MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> syncByServerVersionAndAssignedPlansOnOrganization(
	        @RequestBody PlanSyncRequestWrapper planSyncRequestWrapper, Authentication authentication, @RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam) {
		
		List<String> operationalAreaIds = planSyncRequestWrapper.getOperationalAreaId();
		String username = null;
		if (authentication != null)
			username = authentication.getName();
		if ((operationalAreaIds == null || operationalAreaIds.isEmpty()) && StringUtils.isBlank(username)) {
			return new ResponseEntity<>("Sync Params missing", RestUtils.getJSONUTF8Headers(), HttpStatus.BAD_REQUEST);
		}

		List<PlanDefinition> plans;
		Long planCount = 0l;
		if (planSyncRequestWrapper.getOrganizations() != null && !planSyncRequestWrapper.getOrganizations().isEmpty()) {
			plans = planService.getPlansByOrganizationsAndServerVersion(planSyncRequestWrapper.organizations,
					planSyncRequestWrapper.getServerVersion(), isTemplateParam);
			if (planSyncRequestWrapper.isReturnCount()) {
				planCount = planService.countPlansByOrganizationsAndServerVersion(planSyncRequestWrapper.organizations,
					planSyncRequestWrapper.getServerVersion());
			}
		} else if (username != null) {
			plans = planService.getPlansByUsernameAndServerVersion(username, planSyncRequestWrapper.getServerVersion(), isTemplateParam);
			if (planSyncRequestWrapper.isReturnCount()) {
				planCount = planService.countPlansByUsernameAndServerVersion(username, planSyncRequestWrapper.getServerVersion());
			}
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		HttpHeaders headers = RestUtils.getJSONUTF8Headers();
		if (planSyncRequestWrapper.isReturnCount()){
			headers.add(TOTAL_RECORDS, String.valueOf(planCount));
		}

		return new ResponseEntity<>(gson.toJson(plans), headers, HttpStatus.OK);

	}
	
	// here for backward compatibility
	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> syncByServerVersionAndOperationalAreaTwo(HttpServletRequest request,
	        @RequestParam(value = OPERATIONAL_AREA_ID) List<String> operationalAreaIds , @RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam) {
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
		        gson.toJson(planService.getPlansByServerVersionAndOperationalArea(currentServerVersion, operationalAreaIds, isTemplateParam)),
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
	        @RequestParam(value = FIELDS, required = false) List<String> fields,
			@RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam) {

		if (fields != null && !fields.isEmpty()) {
			for (String fieldName : fields) {
				if (!doesObjectContainField(new PlanDefinition(), fieldName)) {
					return new ResponseEntity<>(fieldName + " field is invalid", HttpStatus.BAD_REQUEST);
				}
			}
		}
		return new ResponseEntity<>(gson.toJson(planService.getPlansByIdsReturnOptionalFields(identifiers, fields, isTemplateParam)),
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
	public ResponseEntity<Set<LocationDetail>> findLocationDetailsByPlanId(
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
	@RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getAll(@RequestParam(value = SERVER_VERSIOIN) long serverVersion,
	        @RequestParam(value = LIMIT, required = false) Integer limit, @RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam) {

		Integer pageLimit = limit == null ? DEFAULT_LIMIT : limit;
		return new ResponseEntity<>(gson.toJson(planService.getAllPlans(serverVersion, pageLimit, isTemplateParam)),
		        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);

	}

	/**
	 * Fetch count of plans
	 *
	 * @param serverVersion serverVersion using to filter by
	 * @return A list of plan definitions
	 */
	@RequestMapping(value = "/countAll", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<ModelMap> countAll(@RequestParam(value = SERVER_VERSIOIN) long serverVersion,
			@RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam) {
		Long countOfPlanDefinitions = planService.countAllPlans(serverVersion, isTemplateParam);
		ModelMap modelMap = new ModelMap();
		modelMap.put("count", countOfPlanDefinitions != null ? countOfPlanDefinitions : 0);
		return new ResponseEntity<>(modelMap,
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	/**
	 * This methods provides an API endpoint that searches for all plan Identifiers
	 *
	 * @return A list of plan Identifiers
	 */
	@RequestMapping(value = "/findIds", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Identifier> findIds(@RequestParam(value = SERVER_VERSIOIN, required = false) long serverVersion,
	        @RequestParam(value = IS_DELETED, defaultValue = FALSE, required = false) boolean isDeleted,
											  @RequestParam(value = "fromDate", required = false) String fromDate,
											  @RequestParam(value = "toDate", required = false) String toDate) {

		Pair<List<String>, Long> planIdsPair = planService.findAllIds(serverVersion, DEFAULT_GET_ALL_IDS_LIMIT, isDeleted,
				Utils.getDateTimeFromString(fromDate), Utils.getDateTimeFromString(toDate));
		Identifier identifiers = new Identifier();
		identifiers.setIdentifiers(planIdsPair.getLeft());
		identifiers.setLastServerVersion(planIdsPair.getRight());

		return new ResponseEntity<>(identifiers, HttpStatus.OK);

	}

	/**
	 * This method provides an API endpoint that searches for plans using a provided username
	 *
	 * @param username
	 * @return plan definitions whose identifiers match the provided param
	 */
	@RequestMapping(value = "/user/{username:.+}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> fetchPlansForUser(@PathVariable(USERNAME) String username,
	        @RequestParam(value = SERVER_VERSIOIN, required = false) String serverVersion,
			@RequestParam(value = IS_TEMPLATE, required = false) boolean isTemplateParam) {
		
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

		plans = planService.getPlansByUsernameAndServerVersion(username, currentServerVersion, isTemplateParam);

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
		
		@JsonProperty(RETURN_COUNT)
		private boolean returnCount;

		public List<String> getOperationalAreaId() {
			return operationalAreaId;
		}
		
		public long getServerVersion() {
			return serverVersion;
		}
		
		public List<Long> getOrganizations() {
			return organizations;
		}


		public boolean isReturnCount() {
			return returnCount;
		}

	}

	private PlanSearchBean createPlanSearchBean(boolean isTemplateParam, Integer pageNumber, Integer pageSize,
			String orderByType,
			String orderByFieldName, String planStatus, Map<String, String> useContextFilters) {
		PlanSearchBean planSearchBean = new PlanSearchBean();
		planSearchBean.setExperimental(isTemplateParam);
		planSearchBean.setPageNumber(pageNumber);
		planSearchBean.setPageSize(pageSize);
		if (orderByType != null) {
			planSearchBean.setOrderByType(PlanSearchBean.OrderByType.valueOf(orderByType));
		}
		if (orderByFieldName != null) {
			planSearchBean.setOrderByFieldName(PlanSearchBean.FieldName.valueOf(orderByFieldName));
		}
		if (planStatus != null) {
			planSearchBean.setPlanStatus(PlanDefinition.PlanStatus.valueOf(planStatus));
		}
		planSearchBean.setUseContexts(useContextFilters);

		return planSearchBean;
	}
	
	/**
	 * This method retrieves a usecontext with a particular code from a plan
	 * @param plan
	 * @return PlanDefinition.UseContext
	 */
	private PlanDefinition.UseContext getUseContextWithCode(PlanDefinition plan, String useContextCode) {
		for (PlanDefinition.UseContext useContext: plan.getUseContext() ) {
			if (useContext.getCode().equalsIgnoreCase(useContextCode)) {
				return useContext;
			}
		}
		return null;
	}

	/**
	 * This method validates whether there is an existing case triggered
	 * plan for a given opensrpEventId
	 * @param useContext useContext that contains the opensrpEventId
	 * @return boolean whether the plan is valid (it's not a duplicate)
	 */
	private boolean isValidCaseTriggeredPlan(PlanDefinition.UseContext useContext) {
		Map<String, String> useContextFilters = new HashMap<>();
		useContextFilters.put(useContext.getCode(),useContext.getValueCodableConcept());
		PlanSearchBean planSearchBean = createPlanSearchBean(false, 0, 5, null, null, null, useContextFilters);

		List<PlanDefinition> plans = planService.getAllPlans(planSearchBean);
		return (plans != null && !plans.isEmpty()) ? false : true;
	}


	/**
	 * This method provides an endpoint that searches for plans with missed task generation
	 * and returns the details
	 *
	 * @return A list of plan identifiers and missed task generation information
	 */
	@RequestMapping(value = "/findMissingTaskGeneration", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<PlanTaskCount>> findMissingTaskGeneration(
			@RequestParam(value = IDENTIFIERS, required = false) List<String> identifiers,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate) {

		return new ResponseEntity<>(planService.getPlanTaskCounts(identifiers,
				Utils.getDateTimeFromString(fromDate), Utils.getDateTimeFromString(toDate)),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	public void generateCaseTriggeredPlans() {
		logger.info("++++++++++++++ starting  generateCaseTriggeredPlans ++++++++++++");
		// Get plan processing record where status is 0 / initial
		List<PlanProcessingStatus> planProcessingStatusList = processingStatusService.getProcessingStatusByStatus(0);

		for (PlanProcessingStatus processingStatus: planProcessingStatusList ) {
			logger.info("++++++++++++++ finding event with id : " + processingStatus.getEventId());
			//Get case details event
			Event event = eventService.findByDbId(processingStatus.getEventId(), false);
			if (event == null){
				processingStatusService.updatePlanProcessingStatus(processingStatus, null,null,
						PlanProcessingStatusConstants.FAILED, Constants.Plan.MISSING_CASE_DETAILS_EVENT);
				continue;
			}
			logger.info("++++++++++++++ Update plan processing status to 1 / processing : " + processingStatus.getEventId());
			//Update plan processing status to 1 / processing
			processingStatusService.updatePlanProcessingStatus(processingStatus, null,null,
					PlanProcessingStatusConstants.PROCESSING, null);
			logger.info("++++++++++++++ //Validate case details event properties : " + processingStatus.getEventId());
			//Validate case details event properties
			boolean isValidCaseDetailsEvent = planService.validateCaseDetailsEvent(event);
			if (!isValidCaseDetailsEvent) {
				processingStatusService.updatePlanProcessingStatus(processingStatus, null,null,
						PlanProcessingStatusConstants.FAILED, Constants.Plan.INVALID_CASE_DETAILS_EVENT);
				continue;
			}
			logger.info("++++++++++++++ Validate OpenSRP jurisdiction exists for the Biophics operational area id provided in the index case : " + event.getDetails().get("bfid"));
			//Validate OpenSRP jurisdiction exists for the Biophics operational area id provided in the index case
			String biophicsJurisdictionId = event.getDetails() != null ? event.getDetails().get(Constants.Plan.BFID) : null;
			if (biophicsJurisdictionId == null){
				processingStatusService.updatePlanProcessingStatus(processingStatus, null,null,
						PlanProcessingStatusConstants.FAILED, Constants.Plan.BIOPHICS_ID_MISSING);
				continue;
			}
			Map<String, String> properties = new HashMap<>();
			properties.put(Constants.Plan.EXTERNAL_ID, biophicsJurisdictionId);
			List<PhysicalLocation> jurisdictionList = locationService.findLocationsByProperties(false, null, properties);
			if (jurisdictionList == null || jurisdictionList.isEmpty()){
				processingStatusService.updatePlanProcessingStatus(processingStatus, null,null,
						PlanProcessingStatusConstants.FAILED, Constants.Plan.JURISDICTION_NOT_FOUND);
				continue;
			}
			logger.info("++++++++++++++ // Run logic to select the template type before generating the plan : " + processingStatus.getEventId());
			// Run logic to select the template type before generating the plan
			Integer templateId = planService.getPlanTemplate(event);
			if (templateId == null) {
				processingStatusService.updatePlanProcessingStatus(processingStatus, null,null,
						PlanProcessingStatusConstants.FAILED, Constants.Plan.TEMPLATE_NOT_FOUND);
				continue;
			}
			logger.info("++++++++++++++ // Fetch template : " + templateId);
			//Fetch template
			Template template = templateService.getTemplateByTemplateId(templateId);
			logger.info("++++++++++++++ // Create plan from template : " + templateId);
			// Create plan from template
			String planTemplateString = gson.toJson(template.getTemplate());
			PlanDefinition plan = createPlanFromTemplate(planTemplateString, event);
			logger.info("++++++++++++++ // Save plan : " + plan.getIdentifier());
			//Save plan
			planService.addPlan(plan, Constants.Plan.PLAN_USER);
			logger.info("++++++++++++++ // update plan processing status to complete : " + processingStatus.getEventId());
			//update plan processing status to 2 / complete
			processingStatusService.updatePlanProcessingStatus(processingStatus, null,plan.getIdentifier(),
					PlanProcessingStatusConstants.COMPLETE, null);
		}
	}

	public Date getCurrentDate(){
		return new Date();
	}

	public PlanDefinition createPlanFromTemplate(String templateString, Event caseDetailsEvent) {
		// Build map
		Map<String, String> valuesMap = new HashMap<>();
		String planIdentifier = caseDetailsEvent.getDetails() != null &&
				!StringUtils.isBlank(caseDetailsEvent.getDetails().get(Constants.Plan.PLAN_IDENTIFIER)) ?
				caseDetailsEvent.getDetails().get(Constants.Plan.PLAN_IDENTIFIER) : UUID.randomUUID().toString();
		valuesMap.put(Constants.Plan.PLAN_IDENTIFIER, planIdentifier);
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.Plan.DATE_FORMAT);
		Date currentDate = getCurrentDate();
		Date endDate;
		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		c.add(Calendar.DATE, Constants.Plan.PLAN_DURATION_DAYS);
		endDate = c.getTime();

		valuesMap.put(Constants.Plan.CURRENT_DATE, sdf.format(currentDate));
		valuesMap.put(Constants.Plan.END_DATE, sdf.format(endDate));
		valuesMap.put(Constants.Plan.REGISTER_FAMILY_ACTION_ID, UUID.randomUUID().toString());
		valuesMap.put(Constants.Plan.BLOOD_SCREENING_ACTION_ID, UUID.randomUUID().toString());
		valuesMap.put(Constants.Plan.BCC_ACTION_ID, UUID.randomUUID().toString());
		valuesMap.put(Constants.Plan.BEDNET_DISTRIBUTION_ACTION_ID, UUID.randomUUID().toString());
		valuesMap.put(Constants.Plan.LARVAL_DIPPING_ACTION_ID, UUID.randomUUID().toString());
		valuesMap.put(Constants.Plan.MOSQUITO_COLLECTION_ID, UUID.randomUUID().toString());

		valuesMap.put(PlanConstants.FOCUS_STATUS, caseDetailsEvent.getDetails().get(PlanConstants.FOCUS_STATUS));
		valuesMap.put(Constants.Plan.OPENSRP_CASE_CLASSIFICATION_EVENT_ID, caseDetailsEvent.getId());
		valuesMap.put(PlanConstants.CASE_NUMBER, caseDetailsEvent.getDetails().get(PlanConstants.CASE_NUMBER));
		valuesMap.put(PlanConstants.FOCUS_ID, caseDetailsEvent.getDetails().get(PlanConstants.FOCUS_ID));
		valuesMap.put(PlanConstants.FOCUS_NAME, caseDetailsEvent.getDetails().get(PlanConstants.FOCUS_NAME));
		valuesMap.put(PlanConstants.PATIENT_NAME, caseDetailsEvent.getDetails().get(PlanConstants.FIRST_NAME));
		valuesMap.put(PlanConstants.PATIENT_SURNAME, caseDetailsEvent.getDetails().get(PlanConstants.SURNAME));
		valuesMap.put(PlanConstants.FLAG, caseDetailsEvent.getDetails().get(PlanConstants.FLAG));

		// Build StringSubstitutor
		StringSubstitutor sub = new StringSubstitutor(valuesMap);

		// Replace
		String resolvedString = sub.replace(templateString);

		return gson.fromJson(resolvedString, PlanDefinition.class);
	}

}
