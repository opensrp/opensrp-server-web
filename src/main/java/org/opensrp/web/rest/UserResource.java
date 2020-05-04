/**
 * 
 */
package org.opensrp.web.rest;

import java.text.MessageFormat;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
	
	@Value("#{keycloak.password.reset.endpoint']}")
	private String resetPasswordURL;
	
	/**
	 * @param userService the userService to set
	 */
	@Autowired
	public void setUserService(OpenmrsUserService userService) {
		this.userService = userService;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	private ResponseEntity<String> getAllUsers(@RequestParam("page_size") int limit,
	        @RequestParam("start_index") int offset) {
		
		JSONObject users = userService.getUsers(limit, offset);
		return new ResponseEntity<>(users == null ? "{}" : users.toString(), HttpStatus.OK);
		
	}
	
	@RequestMapping(value = "/reset-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	private ResponseEntity<String> changePassword(@RequestBody ResetPasswordBean resetPasswordBean) {
		String url = MessageFormat.format(resetPasswordURL, keycloakDeployment.getAuthServerBaseUrl(),
		    keycloakDeployment.getRealm());
		return restTemplate.postForEntity(url, resetPasswordBean, String.class);
	}
	
}
