package org.opensrp.web.controller;

import static org.opensrp.web.HttpHeaderFactory.allowOrigin;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.representations.AccessToken;
import org.opensrp.api.domain.Time;
import org.opensrp.api.domain.User;
import org.opensrp.api.util.LocationTree;
import org.opensrp.common.domain.UserDetail;
import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.Organization;
import org.opensrp.domain.Practitioner;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.exceptions.MissingTeamAssignmentException;
import org.opensrp.web.rest.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartregister.domain.Jurisdiction;
import org.smartregister.domain.PhysicalLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class UserController {
	
	private static Logger logger = LoggerFactory.getLogger(UserController.class.toString());
	
	public static final int LOCATION_LIMIT=5000;
	
	public static final String JURISDICTION="jurisdiction";
	
	@Value("#{opensrp['opensrp.cors.allowed.source']}")
	private String opensrpAllowedSources;
	
	private OrganizationService organizationService;
	
	private PractitionerService practitionerService;
	
	private PhysicalLocationService locationService;
	
	private PlanService planService;
	
	@Value("#{opensrp['openmrs.version']}")
	protected String OPENMRS_VERSION;
	
	@Value("#{opensrp['use.opensrp.team.module']}")
	protected boolean useOpenSRPTeamModule = false;
	
	@Value("#{opensrp['keycloak.configuration.endpoint']}")
	protected String keycloakConfigurationURL;
	
	@Autowired
	protected KeycloakRestTemplate restTemplate;
	
	@Autowired
	protected KeycloakDeployment keycloakDeployment;
	
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
	
	
	/**
	 * @param planService the planService to set
	 */
	@Autowired
	public void setPlanService(PlanService planService) {
		this.planService = planService;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/authenticate-user")
	public ResponseEntity<HttpStatus> authenticateUser() {
		return new ResponseEntity<>(null, allowOrigin(opensrpAllowedSources), OK);
	}
	
	@GetMapping(value = "/logout.do")
	public ResponseEntity<String> logoff(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
	        Authentication authentication) throws ServletException {
		if (authentication != null) {
			servletRequest.logout();
			HttpSession session = servletRequest.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			Cookie[] cookies = servletRequest.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					cookie.setValue("");
					cookie.setMaxAge(0);
					servletResponse.addCookie(cookie);
				}
			}
			return new ResponseEntity<>("User Logged out", OK);
		} else {
			return new ResponseEntity<>("User not logged in", UNAUTHORIZED);
		}
		
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
		User u = RestUtils.currentUser(authentication);
		logger.debug("logged in user {}", u.toString());
		ImmutablePair<Practitioner, List<Long>> practionerOrganizationIds = null;
		Set<PhysicalLocation> jurisdictions = null;
		Set<String> locationIds = new HashSet<>();
		Set<String> planIdentifiers = new HashSet<>();
		try {
			String userId = u.getBaseEntityId();
			practionerOrganizationIds = practitionerService.getOrganizationsByUserId(userId);
			
			for (AssignedLocations assignedLocation : organizationService
			        .findAssignedLocationsAndPlans(practionerOrganizationIds.right)) {
				if (StringUtils.isNotBlank(assignedLocation.getJurisdictionId()))
					locationIds.add(assignedLocation.getJurisdictionId());
				if (StringUtils.isNotBlank(assignedLocation.getPlanId()))
					planIdentifiers.add(assignedLocation.getPlanId());
			}
			
			jurisdictions = new HashSet<>(locationService.findLocationByIdsWithChildren(false, locationIds, LOCATION_LIMIT));
			
			if (!planIdentifiers.isEmpty()) {
				/** @formatter:off*/
				Set<String> planLocationIds = planService
				        .getPlansByIdsReturnOptionalFields(new ArrayList<>(planIdentifiers),
				            Collections.singletonList(JURISDICTION), false)
				        .stream()
				        .flatMap(plan -> plan.getJurisdiction().stream())
				        .map(Jurisdiction::getCode)
				        .collect(Collectors.toSet());
				/** @formatter:on*/	
				Set<PhysicalLocation> planLocations =new HashSet<>(locationService.findLocationByIdsWithChildren(false, planLocationIds, LOCATION_LIMIT));
				jurisdictions.retainAll(planLocations);		
			}
			
		}
		catch (Exception e) {
			logger.error("USER Location info not mapped to an organization", e);
		}
		if (jurisdictions.isEmpty()) {
			throw new MissingTeamAssignmentException(
			        "User not mapped on any location. Make sure that user is assigned to an organization with valid Location(s) ");
		}
		
		LocationTree l = locationService
		        .buildLocationHierachy(jurisdictions.stream().map(j -> j.getId()).collect(Collectors.toSet()), false, true);
		
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
		
		Set<String> locationParents = new HashSet<>();
		for (PhysicalLocation jurisdiction : jurisdictions) {
			JSONObject teamLocation = new JSONObject();
			teamLocation.put("uuid", locationIds.iterator().next());
			teamLocation.put("name", jurisdiction.getProperties().getName());
			teamLocation.put("display", jurisdiction.getProperties().getName());
			locations.put(teamLocation);
			locationParents.add(jurisdiction.getProperties().getParentId());
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
			
		/** @formatter:off*/
		Map<String,String> leafJurisdictions=jurisdictions.stream()
			.filter(location -> !locationParents.contains(location.getId()) && location.getProperties().getName()!=null)
			.collect(Collectors.toMap(PhysicalLocation::getId, (location)->location.getProperties().getName()));
		/**@formatter:on*/
			
		map.put("jurisdictionIds", leafJurisdictions.keySet());
		
		map.put("jurisdictions", leafJurisdictions.values());
	
		return new ResponseEntity<>(new Gson().toJson(map), RestUtils.getJSONUTF8Headers(), OK);
	}
	
	@RequestMapping("/security/configuration")
	public ResponseEntity<String> configuration() throws JSONException {
		Map<String, Object> map = new HashMap<>();
		map.put("serverDatetime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
		return new ResponseEntity<>(new Gson().toJson(map), RestUtils.getJSONUTF8Headers(), OK);
	}
	
}
