/**
 * 
 */
package org.opensrp.web.rest;

import static org.opensrp.web.Constants.ORDER_BY_FIELD_NAME;
import static org.opensrp.web.Constants.ORDER_BY_TYPE;
import static org.opensrp.web.Constants.PAGE_NUMBER;
import static org.opensrp.web.Constants.PAGE_SIZE;
import static org.opensrp.web.Constants.TOTAL_RECORDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.api.domain.User;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.smartregister.domain.Practitioner;
import org.opensrp.search.OrganizationSearchBean;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.bean.OrganizationAssigmentBean;
import org.opensrp.web.bean.UserAssignmentBean;
import org.opensrp.web.controller.UserController;
import org.smartregister.domain.Jurisdiction;
import org.smartregister.domain.PhysicalLocation;
import org.smartregister.domain.PlanDefinition.PlanStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Samuel Githengi created on 09/10/19
 */
@Controller
@RequestMapping(value = "/rest/organization")
public class OrganizationResource {
	
	private static Logger logger = LogManager.getLogger(OrganizationResource.class.toString());
	
	private OrganizationService organizationService;
	
	private PractitionerService practitionerService;
	
	private PhysicalLocationService locationService;
	
	private PlanService planService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * Set the organizationService
	 * 
	 * @param organizationService the organizationService to set
	 */
	@Autowired
	public void setOrganizationService(OrganizationService organizationService) {
		this.organizationService = organizationService;
	}
	
	/**
	 * set the practitionerService
	 * 
	 * @param practitionerService the practitionerService to set
	 */
	@Autowired
	public void setPractitionerService(PractitionerService practitionerService) {
		this.practitionerService = practitionerService;
	}
	
	/**
	 * @param locationService the locationService to set
	 */
	@Autowired
	public void setLocationService(PhysicalLocationService locationService) {
		this.locationService = locationService;
	}
	
	/**
	 * @param planService the planService to set
	 */
	@Autowired
	public void setPlanService(PlanService planService) {
		this.planService = planService;
	}
	
	/**
	 * Gets all the organizations
	 * 
	 * @return all the organizations
	 */
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<Organization> getAllOrganizations(@RequestParam(value = "location_id", required = false) String locationID,
			@RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
			@RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName
			) {
		OrganizationSearchBean organizationSearchBean = createOrganizationSearchBeanForPagination(pageNumber, pageSize, orderByType, orderByFieldName);

		if (StringUtils.isNotBlank(locationID)) {
			return organizationService.selectOrganizationsEncompassLocations(locationID);
		} else {
			return organizationService.getAllOrganizations(organizationSearchBean);
		}
	}
	
	/**
	 * Gets an organization using the identifier
	 * 
	 * @param identifier the Organization Identifier
	 * @return the organization
	 */
	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Organization getOrganizationByIdentifier(@PathVariable("identifier") String identifier) {
		return organizationService.getOrganization(identifier);
	}
	
	/**
	 * Saves a new Organization
	 * 
	 * @param organization to add
	 * @return the http status code
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> createOrganization(@RequestBody Organization organization) {
		try {
			organizationService.addOrganization(organization);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
	}
	
	/**
	 * update an Organization
	 * 
	 * @param organization to add
	 * @return the http status code
	 */
	@RequestMapping(value = "/{identifier}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> updateOrganization(@PathVariable("identifier") String identifier,
	        @RequestBody Organization organization) {
		try {
			organizationService.updateOrganization(organization);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@RequestMapping(value = "/assignLocationsAndPlans", method = RequestMethod.POST, produces = {
	        MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> assignLocationAndPlan(
	        @RequestBody OrganizationAssigmentBean[] organizationAssigmentBeans) {
		try {
			for (OrganizationAssigmentBean organizationAssigmentBean : organizationAssigmentBeans) {
				organizationService.assignLocationAndPlan(organizationAssigmentBean.getOrganization(),
				    organizationAssigmentBean.getJurisdiction(), organizationAssigmentBean.getPlan(),
				    organizationAssigmentBean.getFromDate(), organizationAssigmentBean.getToDate());
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/assignedLocationsAndPlans/{identifier}", method = RequestMethod.GET, produces = {
	        MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<AssignedLocations>> getAssignedLocationsAndPlans(
	        @PathVariable("identifier") String identifier,
			@RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
			@RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName) {
		try {
			return new ResponseEntity<>(organizationService
					.findAssignedLocationsAndPlans(identifier, true, pageNumber, pageSize, orderByType, orderByFieldName),
					RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/user-assignment", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public UserAssignmentBean getUserAssignedLocationsAndPlans(Authentication authentication) {
		User user = RestUtils.currentUser(authentication);
		String userId = user.getBaseEntityId();
		ImmutablePair<Practitioner, List<Long>> practionerOrganizationIds = practitionerService
		        .getOrganizationsByUserId(userId);
		Set<String> jurisdictionIdentifiers = new HashSet<>();
		Set<String> planIdentifiers = new HashSet<>();
		/**@formatter:off*/
		organizationService
		        .findAssignedLocationsAndPlans(practionerOrganizationIds.right)
		        .stream()
		        .filter(a->StringUtils.isNotBlank(a.getJurisdictionId()))
		        .forEach(a ->{
		        	if(StringUtils.isNotBlank(a.getJurisdictionId())) {
		        		jurisdictionIdentifiers.add(a.getJurisdictionId());
		        	}
		        	if(StringUtils.isNotBlank(a.getPlanId())) {
		        		planIdentifiers.add(a.getPlanId());
		        	}
		        });
		Set<PhysicalLocation> jurisdictions = new HashSet<>(locationService.findLocationByIdsWithChildren(false,
		    jurisdictionIdentifiers, Integer.MAX_VALUE));
		
		if (!planIdentifiers.isEmpty()) {
			Set<String> planLocationIds = planService
			        .getPlansByIdsReturnOptionalFields(new ArrayList<>(planIdentifiers),
			            Arrays.asList(UserController.JURISDICTION,UserController.STATUS), false)
			        .stream()
			        .filter(plan -> PlanStatus.ACTIVE.equals(plan.getStatus()))
			        .flatMap(plan -> plan.getJurisdiction().stream())
			        .map(Jurisdiction::getCode)
			        .collect(Collectors.toSet());
		
			Set<PhysicalLocation> planLocations = new HashSet<>(planLocationIds.isEmpty()? Collections.emptySet():
			        locationService.findLocationByIdsWithChildren(false, planLocationIds, Integer.MAX_VALUE));
			jurisdictions.retainAll(planLocations);
		}
		
		Set<String> locationParents = jurisdictions.stream()
				.map(l->l.getProperties().getParentId())
				.collect(Collectors.toSet());
				
		return UserAssignmentBean.builder()
				.organizationIds(new HashSet<>(practionerOrganizationIds.right))
				.jurisdictions(jurisdictions
					.stream()
					.filter(l->!locationParents.contains(l.getId()))
		            .map(PhysicalLocation::getId)
					.collect(Collectors.toSet()))
				.plans(planIdentifiers)
				.build();
		/**@formatter:on*/
		
	}
	
	@GetMapping(value = "/practitioner/{identifier}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<Practitioner>> getOrgPractitioners(@PathVariable("identifier") String identifier) {
		try {
			return new ResponseEntity<>(practitionerService.getPractitionersByOrgIdentifier(identifier),
			        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/assignedLocationsAndPlans", method = RequestMethod.GET, produces = {
	        MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<AssignedLocations>> getAssignedLocationsAndPlansByPlanId(
	        @RequestParam(value = "plan") String planIdentifier, @RequestParam(value = PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(value = PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(value = ORDER_BY_TYPE, required = false) String orderByType,
			@RequestParam(value = ORDER_BY_FIELD_NAME, required = false) String orderByFieldName) {
		try {
			return new ResponseEntity<>(organizationService.findAssignedLocationsAndPlansByPlanIdentifier(planIdentifier,
					pageNumber, pageSize, orderByType, orderByFieldName),
			        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> searchOrganization(OrganizationSearchBean organizationSearchBean)
	        throws JsonProcessingException {
		
		Integer pageNumber = organizationSearchBean.getPageNumber();
		HttpHeaders headers = RestUtils.getJSONUTF8Headers();
		int total = 0;
		if (pageNumber != null && pageNumber == 1) {
			total = organizationService.findOrganizationCount(organizationSearchBean);
		}
		List<Organization> organizations = organizationService.getSearchOrganizations(organizationSearchBean);
		
		headers.add(TOTAL_RECORDS, String.valueOf(total));
		return new ResponseEntity<>(objectMapper.writeValueAsString(organizations), headers, HttpStatus.OK);
		
	}

	private OrganizationSearchBean createOrganizationSearchBeanForPagination(Integer pageNumber, Integer pageSize,
			String orderByType, String orderByFieldName) {
		OrganizationSearchBean organizationSearchBean = new OrganizationSearchBean();
		OrganizationSearchBean.OrderByType orderByTypeEnum = orderByType != null ? OrganizationSearchBean.OrderByType.valueOf(orderByType) : OrganizationSearchBean.OrderByType.DESC;
		OrganizationSearchBean.FieldName fieldName = orderByFieldName != null ? OrganizationSearchBean.FieldName.valueOf(orderByFieldName) : OrganizationSearchBean.FieldName.id;
		organizationSearchBean.setPageNumber(pageNumber);
		organizationSearchBean.setPageSize(pageSize);
		organizationSearchBean.setOrderByFieldName(fieldName);
        organizationSearchBean.setOrderByType(orderByTypeEnum);

		return organizationSearchBean;
	}
	
}
