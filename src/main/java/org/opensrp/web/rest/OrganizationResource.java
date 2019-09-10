/**
 * 
 */
package org.opensrp.web.rest;

import java.util.List;

import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.service.OrganizationService;
import org.opensrp.web.bean.OrganizationAssigmentBean;
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

	public static Gson gson = new GsonBuilder().create();

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
	 * Gets all the organizations
	 * 
	 * @return all the organizations
	 */
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getAllOrganizations() {
		return new ResponseEntity<>(gson.toJson(organizationService.getAllOrganizations()), HttpStatus.OK);
	}

	/**
	 * Gets an organization using the identifier
	 * 
	 * @param identifier the Organization Identifier
	 * 
	 * @return the organization
	 */
	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getOrganizationByIdentifier(@PathVariable("identifier") String identifier) {
		return new ResponseEntity<>(gson.toJson(organizationService.getOrganization(identifier)), HttpStatus.OK);
	}

	/**
	 * Saves a new Organization
	 * 
	 * @param organization to add
	 * @return the http status code
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> createOrganization(@RequestBody Organization organization) {
		try {
			organizationService.addOrganization(organization);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
	public ResponseEntity<HttpStatus> updateOrganization(@PathVariable("identifier") String identifier,
			@RequestBody Organization organization) {
		try {
			organizationService.updateOrganization(organization);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping(value = "/assignLocationAndPlan", method = RequestMethod.POST, produces = {
			MediaType.APPLICATION_JSON_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> assignLocationAndPlan(
			@RequestBody OrganizationAssigmentBean organizationAssigmentBean) {
		try {
			organizationService.assignLocationAndPlan(organizationAssigmentBean.getOrganizationId(),
					organizationAssigmentBean.getJusrisdictionIdentifier(),
					organizationAssigmentBean.getPlanIdentifier(), organizationAssigmentBean.getDateFrom(),
					organizationAssigmentBean.getDateTo());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getAssignedLocationsAndPlans", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<List<AssignedLocations>> getAssignedLocationsAndPlans(
			@RequestParam("organizationId") Long organizationId) {
		try {
			;
			return new ResponseEntity<>(organizationService.findAssignedLocationsAndPlans(organizationId),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
