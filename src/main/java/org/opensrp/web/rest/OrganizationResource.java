/**
 * 
 */
package org.opensrp.web.rest;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.domain.Practitioner;
import org.opensrp.search.AssignedLocationAndPlanSearchBean;
import org.opensrp.search.OrganizationSearchBean;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.bean.OrganizationAssigmentBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Samuel Githengi created on 09/10/19
 */
@Controller
@RequestMapping(value = "/rest/organization")
public class OrganizationResource {

	private static Logger logger = LoggerFactory.getLogger(OrganizationResource.class.toString());

	private OrganizationService organizationService;

	private PractitionerService practitionerService;
	public static Gson gson = new GsonBuilder().create();

	
	private PhysicalLocationService locationService;
	
	private PlanService planService;
	
	@Autowired
	private ObjectMapper objectMapper;

	public static final String PAGE_NUMBER = "pageNumber";

	public static final String PAGE_SIZE = "pageSize";

	public static final String ORDER_BY_TYPE = "orderByType";

	public static final String ORDER_BY_FIELD_NAME = "orderByFieldName";
	
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
	 *  set the practitionerService
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
	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getOrganizationByIdentifier(@PathVariable("identifier") String identifier) {
		return new ResponseEntity<>(gson.toJson(organizationService.getOrganization(identifier)),
		        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	/**
	 * Saves a new Organization
	 * 
	 * @param organization to add
	 * @return the http status code
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> createOrganization(@RequestBody Organization organization) {
		try {
			organizationService.addOrganization(organization);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
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
	@RequestMapping(value = "/{identifier}", method = RequestMethod.PUT, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> updateOrganization(@PathVariable("identifier") String identifier,
			@RequestBody Organization organization) {
		try {
			organizationService.updateOrganization(organization);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

	}

	@RequestMapping(value = "/assignLocationsAndPlans", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> assignLocationAndPlan(
			@RequestBody OrganizationAssigmentBean[] organizationAssigmentBeans) {
		try {
			for (OrganizationAssigmentBean organizationAssigmentBean : organizationAssigmentBeans) {
				organizationService.assignLocationAndPlan(organizationAssigmentBean.getOrganization(),
						organizationAssigmentBean.getJurisdiction(),
						organizationAssigmentBean.getPlan(), organizationAssigmentBean.getFromDate(),
						organizationAssigmentBean.getToDate());
			}
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (IllegalArgumentException e) {
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
			AssignedLocationAndPlanSearchBean assignedLocationAndPlanSearchBean = createAssignedLocationAndPlanSearchBean(pageNumber,pageSize,orderByType,orderByFieldName,null,identifier,true);
			return new ResponseEntity<>(organizationService.findAssignedLocationsAndPlans(assignedLocationAndPlanSearchBean),
			        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/practitioner/{identifier}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<Practitioner>> getOrgPractitioners(
			@PathVariable("identifier") String identifier) {
		try {
			return new ResponseEntity<>(practitionerService.getPractitionersByOrgIdentifier(identifier), RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		} catch (IllegalArgumentException e) {
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
			AssignedLocationAndPlanSearchBean assignedLocationAndPlanSearchBean = createAssignedLocationAndPlanSearchBean(pageNumber,pageSize,orderByType,orderByFieldName,planIdentifier,null,false);
			return new ResponseEntity<>(organizationService.findAssignedLocationsAndPlansByPlanIdentifier(assignedLocationAndPlanSearchBean),
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
		organizationSearchBean.setPageNumber(pageNumber);
		organizationSearchBean.setPageSize(pageSize);
		if (orderByType != null) {
			organizationSearchBean.setOrderByType(OrganizationSearchBean.OrderByType.valueOf(orderByType));
		}
		if (orderByFieldName != null) {
			organizationSearchBean.setOrderByFieldName(OrganizationSearchBean.FieldName.valueOf(orderByFieldName));
		}

		return organizationSearchBean;
	}


	private AssignedLocationAndPlanSearchBean createAssignedLocationAndPlanSearchBean(Integer pageNumber, Integer pageSize,
			String orderByType, String orderByFieldName, String planIdentifier, String organizationIdentifier, boolean returnFutureAssignments) {
		AssignedLocationAndPlanSearchBean assignedLocationAndPlanSearchBean = new AssignedLocationAndPlanSearchBean();
		assignedLocationAndPlanSearchBean.setPageNumber(pageNumber);
		assignedLocationAndPlanSearchBean.setPageSize(pageSize);
		if (orderByType != null) {
			assignedLocationAndPlanSearchBean.setOrderByType(AssignedLocationAndPlanSearchBean.OrderByType.valueOf(orderByType));
		}
		if (orderByFieldName != null) {
			assignedLocationAndPlanSearchBean.setOrderByFieldName(AssignedLocationAndPlanSearchBean.FieldName.valueOf(orderByFieldName));
		}
		assignedLocationAndPlanSearchBean.setOrganizationIdentifier(organizationIdentifier);
		assignedLocationAndPlanSearchBean.setPlanIdentifier(planIdentifier);
		assignedLocationAndPlanSearchBean.setReturnFutureAssignments(returnFutureAssignments);

		return assignedLocationAndPlanSearchBean;
	}
	
}
