package org.opensrp.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.opensrp.service.rapidpro.ZeirRapidProService;
import org.opensrp.util.constants.RapidProConstants;
import org.opensrp.web.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/rest/rapidpro")
public class RapidProResource {

	@Value("#{opensrp['rapidpro.token']}")
	private String rapidProToken;

	@Value("#{opensrp['rapidpro.project']}")
	private String rapidProProject;

	private ZeirRapidProService zeirRapidProService;

	@RequestMapping(value = "/callback", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> callback(HttpServletRequest request, @RequestBody String payload) {

		String apiToken = request.getHeader(Constants.AUTHORIZATION);
		String[] tokenValues = apiToken.split(" ");

		if (tokenValues.length == 2 && StringUtils.isNotBlank(rapidProToken) && StringUtils.isNotBlank(rapidProProject)
				&& rapidProToken.equalsIgnoreCase(tokenValues[1].trim())) {
			if (RapidProConstants.RapidProProjects.ZEIR_RAPIDPRO.equalsIgnoreCase(rapidProProject)) {
				JSONObject responseJson = new JSONObject();
				responseJson.put(RapidProConstants.OPENSRP_ID, "1000122");
				return new ResponseEntity<>(responseJson.toString(), HttpStatus.OK);
			}
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
