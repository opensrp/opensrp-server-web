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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
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
import org.opensrp.common.util.OpenMRSCrossVariables;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.LocationProperty.PropertyStatus;
import org.opensrp.domain.Organization;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.domain.Practitioner;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.rest.RestUtils;
import org.opensrp.web.utils.LocationUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class UserController {
	
	private static Logger logger = LoggerFactory.getLogger(UserController.class.toString());
	
	@Value("#{opensrp['opensrp.cors.allowed.source']}")
	private String opensrpAllowedSources;
	
	private OpenmrsLocationService openmrsLocationService;
	
	private OpenmrsUserService openmrsUserService;
	
	private OrganizationService organizationService;
	
	private PractitionerService practitionerService;
	
	private PhysicalLocationService locationService;
	
	@Value("#{opensrp['openmrs.version']}")
	protected String OPENMRS_VERSION;
	
	@Value("#{opensrp['use.opensrp.team.module']}")
	protected boolean useOpenSRPTeamModule = false;
	
	@Autowired
	public UserController(OpenmrsLocationService openmrsLocationService, OpenmrsUserService openmrsUserService) {
		this.openmrsLocationService = openmrsLocationService;
		this.openmrsUserService = openmrsUserService;
	}
	
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
			KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>) authentication
			        .getPrincipal();
			AccessToken token = kp.getKeycloakSecurityContext().getToken();
			UserDetail userDetail = new UserDetail();
			userDetail.setIdentifier(authentication.getName());
			userDetail.setUserName(token.getPreferredUsername());
			userDetail.setPreferredName(token.getName());
			userDetail.setFamilyName(token.getFamilyName());
			userDetail.setGivenName(token.getGivenName());
			userDetail.setEmail(token.getEmail());
			userDetail.setEmailVerified(token.getEmailVerified());
			userDetail.setRoles(
			    authentication.getAuthorities().stream().map(e -> e.getAuthority()).collect(Collectors.toList()));
			return new ResponseEntity<>(userDetail, RestUtils.getJSONUTF8Headers(), OK);
			
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
	}
	
	@RequestMapping("/security/authenticate")
	@ResponseBody
	public ResponseEntity<String> authenticate(HttpServletRequest request, Authentication authentication)
	        throws JSONException {
		if (useOpenSRPTeamModule) {
			return authenticateUsingOrganization(authentication);
		}
		User u = currentUser(authentication);
		System.out.println(u);
		String lid = "";
		JSONObject tm = null;
		try {
			tm = openmrsUserService.getTeamMember(u.getAttribute("_PERSON_UUID").toString());
			JSONArray locs = tm.getJSONArray(OpenMRSCrossVariables.LOCATIONS_JSON_KEY.makeVariable(OPENMRS_VERSION));
			
			for (int i = 0; i < locs.length(); i++) {
				lid += locs.getJSONObject(i).getString("uuid") + ";;";
			}
		}
		catch (Exception e) {
			logger.error("USER Location info not mapped in team management module. Now trying Person Attribute", e);
		}
		if (StringUtils.isBlank(lid)) {
			lid = (String) u.getAttribute("Location");
			if (StringUtils.isBlank(lid)) {
				lid = (String) u.getAttribute("Locations");
				if (lid == null) {
					throw new IllegalStateException(
					        "User not mapped on any location. Make sure that user have a person attribute Location or Locations with uuid(s) of valid OpenMRS Location(s) separated by ;;");
				}
				
			}
		}
		LocationTree l = openmrsLocationService.getLocationTreeOf(lid.split(";;"));
		Map<String, Object> map = new HashMap<>();
		map.put("user", u);
		try {
			Map<String, Object> tmap = new Gson().fromJson(tm.toString(), new TypeToken<HashMap<String, Object>>() {
				
			}.getType());
			map.put("team", tmap);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		map.put("locations", l);
		Time t = getServerTime();
		map.put("time", t);
		return new ResponseEntity<>(new Gson().toJson(map), RestUtils.getJSONUTF8Headers(), OK);
	}
	
	private ResponseEntity<String> authenticateUsingOrganization(Authentication authentication) throws JSONException {
		User u = currentUser(authentication);
		logger.debug("logged in user {}", u.toString());
		
		Map<String, String> openMRSIdsMap = new HashMap<>();
		Set<String> openMRSIds = new HashSet<>();
		ImmutablePair<Practitioner, List<Long>> practionerOrganizationIds = null;
		List<PhysicalLocation> jurisdictions = null;
		Set<String> locationIds = new HashSet<>();
		Set<String> jurisdictionNames = new HashSet<>();
		Map<String, String> locationAndParent = new HashMap<>();
		try {
			String userId = u.getBaseEntityId();
			practionerOrganizationIds = practitionerService.getOrganizationsByUserId(userId);
			
			for (AssignedLocations assignedLocation : organizationService
			        .findAssignedLocationsAndPlans(practionerOrganizationIds.right)) {
				locationIds.add(assignedLocation.getJurisdictionId());
			}
			
			jurisdictions = locationService.findLocationsByIdsOrParentIds(false, new ArrayList<>(locationIds));
			
			for (PhysicalLocation jurisdiction : jurisdictions) {
				if (PropertyStatus.INACTIVE.equals(jurisdiction.getProperties().getStatus()))
					continue;
				String openMRSId = jurisdiction.getProperties().getCustomProperties().get("OpenMRS_Id");
				if (StringUtils.isNotBlank(openMRSId)) {
					String parentId = jurisdiction.getProperties().getParentId();
					openMRSIdsMap.put(jurisdiction.getId(), openMRSId);
					locationAndParent.put(jurisdiction.getId(), parentId);
				}
				jurisdictionNames.add(jurisdiction.getProperties().getName());
			}
			
			Set<String> parentLocations = LocationUtils.getRootLocation(locationAndParent);
			if (parentLocations.size() == 1) {
				openMRSIds.addAll(openMRSIdsMap.values());
			} else {
				for (String locationId : parentLocations) {
					if (openMRSIdsMap.containsKey(locationId)) {
						openMRSIds.add(openMRSIdsMap.get(locationId));
					} else {
						openMRSIds.add(locationService.getLocation(locationId, false).getProperties().getCustomProperties()
						        .get("OpenMRS_Id"));
					}
				}
			}
			
		}
		catch (Exception e) {
			logger.error("USER Location info not mapped to an organization", e);
		}
		if (openMRSIds.isEmpty()) {
			throw new IllegalStateException(
			        "User not mapped on any location. Make sure that user is assigned to an organization with valid Location(s) including OpenMRS ");
		}
		
		LocationTree l = new LocationTree();
		try {
			l = openmrsLocationService.getLocationTreeOf(openMRSIds.toArray(new String[] {}));
		}
		catch (Exception e) {
			logger.error("Error getting location tree from OpenMRS", e);
		}
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
		
		JSONObject teamLocation = new JSONObject();
		// TODO populate jurisdictions if user has many jurisdictions
		PhysicalLocation jurisdiction = jurisdictions.get(0);
		teamLocation.put("uuid", openMRSIds.iterator().next());
		teamLocation.put("name", jurisdiction.getProperties().getName());
		teamLocation.put("display", jurisdiction.getProperties().getName());
		teamJson.put("location", teamLocation);
		
		JSONArray locations = new JSONArray();
		locations.put(teamLocation);
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
		map.put("jurisdictions", jurisdictionNames);
		return new ResponseEntity<>(new Gson().toJson(map), RestUtils.getJSONUTF8Headers(), OK);
	}
	
	@RequestMapping("/security/configuration")
	@ResponseBody
	public ResponseEntity<String> configuration() throws JSONException {
		Map<String, Object> map = new HashMap<>();
		map.put("serverDatetime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
		return new ResponseEntity<>(new Gson().toJson(map), RestUtils.getJSONUTF8Headers(), OK);
	}
	
	public void setOpenmrsUserService(OpenmrsUserService openmrsUserService) {
		this.openmrsUserService = openmrsUserService;
	}
	
}
