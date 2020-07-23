/**
 * 
 */
package org.opensrp.web.rest;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.bean.ResetPasswordBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * @author Samuel Githengi created on 10/09/19
 */
@Controller
@RequestMapping(value = "/rest/user")
public class UserResource {
	
	private OpenmrsUserService userService;
	
	@Autowired
	private KeycloakDeployment keycloakDeployment;
	
	@Autowired
	private KeycloakRestTemplate restTemplate;
	
	@Value("#{opensrp['keycloak.password.reset.endpoint']}")
	private String resetPasswordURL;
	
	@Value("#{opensrp['keycloak.users.endpoint']}")
	private String usersURL;
	
	/**
	 * @param userService the userService to set
	 */
	@Autowired
	public void setUserService(OpenmrsUserService userService) {
		this.userService = userService;
	}
	
	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	private ResponseEntity<String> getAllUsers(@RequestParam("page_size") int limit,
	        @RequestParam("start_index") int offset) {
		
		JSONObject users = userService.getUsers(limit, offset);
		return new ResponseEntity<>(users == null ? "{}" : users.toString(), HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/keycloak")
	private ResponseEntity<String> getAllKeycloakUsers(@RequestParam("page_size") int limit,
	        @RequestParam("start_index") int offset) {
		
		String url = MessageFormat.format(usersURL, keycloakDeployment.getAuthServerBaseUrl(),
		    keycloakDeployment.getRealm());
		
		ResponseEntity<String> response = null;
		Map<String, Integer> uriVariables = new HashMap<>();
		uriVariables.put("first", offset);
		uriVariables.put("max", limit);
		try {
			
			response = restTemplate.getForEntity(url, String.class, uriVariables);
		}
		catch (HttpStatusCodeException e) {
			return new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
		
	}
	
	@GetMapping(value = "/keycloak/count")
	private ResponseEntity<String> getKeycloakUsersCount(@RequestParam("page_size") int limit,
	        @RequestParam("start_index") int offset) {
		
		String url = MessageFormat.format(usersURL, keycloakDeployment.getAuthServerBaseUrl(), keycloakDeployment.getRealm())
		        + "/count";
		ResponseEntity<String> response = null;
		try {	
			response = restTemplate.getForEntity(url, String.class);
		}
		catch (HttpStatusCodeException e) {
			return new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
		}
		return response;
		
	}
	
	@PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	private ResponseEntity<String> changePassword(@RequestBody ResetPasswordBean resetPasswordBean) {
		String url = MessageFormat.format(resetPasswordURL, keycloakDeployment.getAuthServerBaseUrl(),
		    keycloakDeployment.getRealm());
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.postForEntity(url, resetPasswordBean, String.class);
		}
		catch (HttpStatusCodeException e) {
			return new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
			
		}
		return response;
	}
	
}
