/**
 * 
 */
package org.opensrp.web.rest;

import static org.opensrp.web.Constants.TOTAL_RECORDS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.opensrp.api.domain.User;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.domain.Practitioner;
import org.opensrp.search.OrganizationSearchBean;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.bean.OrganizationAssigmentBean;
import org.opensrp.web.bean.UserAssignmentBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static Logger logger = LoggerFactory.getLogger(OrganizationResource.class.toString());
	
	private OrganizationService organizationService;
	
	private PractitionerService practitionerService;
	
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
	 * Gets all the organizations
	 * 
	 * @return all the organizations
	 */
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<Organization> getAllOrganizations(@RequestParam(value = "location_id", required = false) String locationID) {
		if (StringUtils.isNotBlank(locationID)) {
			return organizationService.selectOrganizationsEncompassLocations(locationID);
		} else {
			return organizationService.getAllOrganizations();
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
		return  organizationService.getOrganization(identifier);
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
	
	@RequestMapping(value = "/assignLocationsAndPlans", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE }, consumes = {
	        MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> assignLocationAndPlan(@RequestBody OrganizationAssigmentBean[] organizationAssigmentBeans) {
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
	
	@RequestMapping(value = "/assignedLocationsAndPlans/{identifier}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<AssignedLocations>> getAssignedLocationsAndPlans(@PathVariable("identifier") String identifier) {
		try {
			return new ResponseEntity<>(organizationService.findAssignedLocationsAndPlans(identifier,true),
			        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	

	@RequestMapping(value = "/user-assignment", method = RequestMethod.GET, produces = {
	        MediaType.APPLICATION_JSON_VALUE })
	public UserAssignmentBean getUserAssignedLocationsAndPlans(Authentication authentication) {
		User user = RestUtils.currentUser(authentication);
		String userId = user.getBaseEntityId();
		ImmutablePair<Practitioner, List<Long>> practionerOrganizationIds = practitionerService
		        .getOrganizationsByUserId(userId);
		Set<String> jurisdictions= new HashSet<>();
		Set<String> plans= new HashSet<>();
		/**@formatter:off*/
		organizationService
		        .findAssignedLocationsAndPlans(practionerOrganizationIds.right)
		        .stream()
		        .filter(a->StringUtils.isNotBlank(a.getJurisdictionId()))
		        .forEach(a ->{
		        	jurisdictions.add(a.getJurisdictionId());
		        	plans.add(a.getPlanId());
		        });
		return UserAssignmentBean.builder()
				.organizationIds(new HashSet<>(practionerOrganizationIds.right))
				.jurisdictions(jurisdictions)
				.plans(plans)
				.build();
		/**@formatter:on*/
	}
	
	@GetMapping(value = "/practitioner/{identifier}",produces = { MediaType.APPLICATION_JSON_VALUE })
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
	
	@RequestMapping(value = "/assignedLocationsAndPlans", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<AssignedLocations>> getAssignedLocationsAndPlansByPlanId(@RequestParam(value = "plan") String planIdentifier) {
		try {
			return new ResponseEntity<>(organizationService.findAssignedLocationsAndPlansByPlanIdentifier(planIdentifier),
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
	
}
