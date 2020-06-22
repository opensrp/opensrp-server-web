package org.opensrp.web.controller;

import static org.opensrp.web.HttpHeaderFactory.allowOrigin;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.opensrp.api.domain.Time;
import org.opensrp.api.domain.User;
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.domain.UserDetail;
import org.opensrp.domain.AssignedLocations;
<<<<<<< HEAD
=======
import org.smartregister.domain.LocationProperty.PropertyStatus;
>>>>>>> 3af8343... Change domain package for entities
import org.opensrp.domain.Organization;
import org.smartregister.domain.PhysicalLocation;
import org.opensrp.domain.Practitioner;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.rest.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class UserController {
	
	private static Logger logger = LoggerFactory.getLogger(UserController.class.toString());
	
	@Value("#{opensrp['opensrp.cors.allowed.source']}")
	private String opensrpAllowedSources;
	
	private OrganizationService organizationService;
	
	private PractitionerService practitionerService;
	
	private PhysicalLocationService locationService;
	
	@Value("#{opensrp['openmrs.version']}")
	protected String OPENMRS_VERSION;
	
	@Value("#{opensrp['use.opensrp.team.module']}")
	protected boolean useOpenSRPTeamModule = false;
	
	/**
	 * @param organizationService the organizationService to set
	 */
	@Autowired
	public void setOrganizationService(OrganizationService organizationService) {
		this.organizationService = organizationService;
	}
	
	/**
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/authenticate-user")
	public ResponseEntity<HttpStatus> authenticateUser() {
		return new ResponseEntity<>(null, allowOrigin(opensrpAllowedSources), OK);
	}
	
	public User currentUser(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof KeycloakPrincipal) {
			@SuppressWarnings("unchecked")
			KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) authentication
			        .getPrincipal();
			AccessToken token = kp.getKeycloakSecurityContext().getToken();
			User user = new User(authentication.getName());
			user.setPreferredName(token.getName());
			user.setUsername(token.getPreferredUsername());
			List<String> authorities = authentication.getAuthorities().stream().map(e -> e.getAuthority())
			        .collect(Collectors.toList());
			user.setAttributes(token.getOtherClaims());
			user.setRoles(authorities);
			user.setPermissions(authorities);
			return user;
		}
		return null;
	}
	
	public Time getServerTime() {
		return new Time(Calendar.getInstance().getTime(), TimeZone.getDefault());
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/user-details")
	public ResponseEntity<UserDetail> getUserDetails(Authentication authentication)
	        throws MalformedURLException, IOException {
		if (authentication != null && authentication.getPrincipal() instanceof KeycloakPrincipal) {
			@SuppressWarnings("unchecked")
			KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) authentication
			        .getPrincipal();
			AccessToken token = kp.getKeycloakSecurityContext().getToken();
			UserDetail userDetail = UserDetail.builder().identifier(authentication.getName())
			        .userName(token.getPreferredUsername()).preferredName(token.getName()).familyName(token.getFamilyName())
			        .givenName(token.getGivenName()).email(token.getEmail()).emailVerified(token.getEmailVerified())
			        .roles(authentication.getAuthorities().stream().map(e -> e.getAuthority()).collect(Collectors.toList()))
			        .build();
			return new ResponseEntity<>(userDetail, RestUtils.getJSONUTF8Headers(), OK);
			
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
	}
	
	@RequestMapping("/security/authenticate")
	public ResponseEntity<String> authenticate(Authentication authentication) throws JSONException {
		User u = currentUser(authentication);
		logger.debug("logged in user {}", u.toString());
		ImmutablePair<Practitioner, List<Long>> practionerOrganizationIds = null;
		List<PhysicalLocation> jurisdictions = null;
		Set<String> locationIds = new HashSet<>();
		try {
			String userId = u.getBaseEntityId();
			practionerOrganizationIds = practitionerService.getOrganizationsByUserId(userId);
			
			for (AssignedLocations assignedLocation : organizationService
			        .findAssignedLocationsAndPlans(practionerOrganizationIds.right)) {
				locationIds.add(assignedLocation.getJurisdictionId());
			}
			
			jurisdictions = locationService.findLocationsByIds(false, new ArrayList<>(locationIds));
			
		}
		catch (Exception e) {
			logger.error("USER Location info not mapped to an organization", e);
		}
		if (locationIds.isEmpty()) {
			throw new IllegalStateException(
			        "User not mapped on any location. Make sure that user is assigned to an organization with valid Location(s) ");
		}
		
		LocationTree l = locationService.buildLocationHierachy(locationIds);
		Map<String, Object> map = new HashMap<>();
		map.put("user", u);
		
		JSONObject teamMemberJson = new JSONObject();
		teamMemberJson.put("identifier", practionerOrganizationIds.left.getIdentifier());
		teamMemberJson.put("uuid", practionerOrganizationIds.left.getUserId());
		
		JSONObject teamJson = new JSONObject();
		// TODO populate organizations if user has many organizations
		Organization organization = organizationService.getOrganization(practionerOrganizationIds.right.get(0));
		teamJson.put("teamName", organization.getName());
		teamJson.put("display", organization.getName());
		teamJson.put("uuid", organization.getIdentifier());
		teamJson.put("organizationIds", practionerOrganizationIds.right);
		
		JSONArray locations = new JSONArray();
		
		for (PhysicalLocation jurisdiction : jurisdictions) {
			JSONObject teamLocation = new JSONObject();
			teamLocation.put("uuid", locationIds.iterator().next());
			teamLocation.put("name", jurisdiction.getProperties().getName());
			teamLocation.put("display", jurisdiction.getProperties().getName());
			locations.put(teamLocation);
		}
		
		//team location is still returned as 1 object
		teamJson.put("location", locations.getJSONObject(0));
		teamMemberJson.put("locations", locations);
		teamMemberJson.put("team", teamJson);
		
		try {
			Map<String, Object> tmap = new Gson().fromJson(teamMemberJson.toString(),
			    new TypeToken<HashMap<String, Object>>() {
			    
			    }.getType());
			map.put("team", tmap);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		map.put("locations", l);
		Time t = getServerTime();
		map.put("time", t);
		map.put("jurisdictions",
		    jurisdictions.stream().map(location -> location.getProperties().getName()).collect(Collectors.toSet()));
		return new ResponseEntity<>(new Gson().toJson(map), RestUtils.getJSONUTF8Headers(), OK);
	}
	
	@RequestMapping("/security/configuration")
	public ResponseEntity<String> configuration() throws JSONException {
		Map<String, Object> map = new HashMap<>();
		map.put("serverDatetime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
		return new ResponseEntity<>(new Gson().toJson(map), RestUtils.getJSONUTF8Headers(), OK);
	}
	
}
