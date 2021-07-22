package org.opensrp.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.domain.postgres.RapidproState;
import org.opensrp.domain.rapidpro.ZeirRapidProEntity;
import org.opensrp.domain.rapidpro.ZeirRapidProEntityProperty;
import org.opensrp.service.rapidpro.RapidProService;
import org.opensrp.service.rapidpro.ZeirRapidProStateService;
import org.opensrp.util.DateParserUtils;
import org.opensrp.util.constants.RapidProConstants;
import org.opensrp.web.Constants;
import org.opensrp.web.Constants.RapidPro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/rapidpro")
public class RapidProResource {

	private final Logger logger = LogManager.getLogger(this.getClass());

	@Value("#{opensrp['rapidpro.token']}")
	private String rapidProToken;

	@Value("#{opensrp['rapidpro.project']}")
	private String rapidProProject;

	private RapidProService rapidProService;

	private ZeirRapidProStateService rapidProStateService;

	@Autowired
	public void setRapidProService(RapidProService rapidProService) {
		this.rapidProService = rapidProService;
	}

	@Autowired
	public void setRapidProStateService(ZeirRapidProStateService rapidProStateService) {
		this.rapidProStateService = rapidProStateService;
	}

	@RequestMapping(value = "/callback", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE }, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> callback(HttpServletRequest request, @RequestBody final String payload) {
		String apiToken = request.getHeader(Constants.AUTHORIZATION);
		String[] tokenValues = apiToken.split(" ");

		final JSONObject responseJson = new JSONObject();

		if (tokenValues.length == 2 && StringUtils.isNotBlank(rapidProToken) && StringUtils.isNotBlank(rapidProProject)
				&& rapidProToken.equalsIgnoreCase(tokenValues[1].trim())) {
			if (RapidProConstants.RapidProProjects.ZEIR_RAPIDPRO.equalsIgnoreCase(rapidProProject)) {
				rapidProService.queryContacts(() -> {
					JSONObject payloadJson = new JSONObject(payload);
					if (payloadJson.has(RapidPro.CONTACT) && payloadJson.has(RapidProConstants.RESULTS)) {

						JSONObject idJson = payloadJson.getJSONObject(RapidPro.ID);
						JSONObject dobJson = payloadJson.getJSONObject(RapidPro.DATE_OF_BIRTH);
						if (dobJson != null && idJson != null) {
							JSONObject supervisorJson = payloadJson.getJSONObject(RapidPro.CONTACT);
							String supervisorUuid = supervisorJson.getString(RapidPro.UUID);

							RapidproState supervisorState = rapidProStateService.getRapidProStateByUuid(supervisorUuid,
									ZeirRapidProEntity.SUPERVISOR.name(), ZeirRapidProEntityProperty.LOCATION_ID.name());

							if (supervisorState != null) {
								String locationId = supervisorState.getPropertyValue();
								String id = idJson.getString(RapidPro.VALUE);
								DateTime dob = DateParserUtils.parseZoneDateTime(dobJson.getString(RapidPro.VALUE));

								String uniqueMvaccId = locationId + "|" + id + "/" + dob.getYear();
								List<RapidproState> childStates =
										rapidProStateService.getStatesByPropertyKey(ZeirRapidProEntity.CHILD.name(),
												ZeirRapidProEntityProperty.IDENTIFIER.name(), uniqueMvaccId);

								if (childStates != null && !childStates.isEmpty()) {
									RapidproState childState = childStates.get(childStates.size() - 1);
									String opensrpId = childState.getPropertyValue();
									responseJson.put(RapidProConstants.OPENSRP_ID, opensrpId);
									logger.info("ZEIR ID (" + opensrpId + ") found for MVACC ID " + uniqueMvaccId);
								}
							}
						}
					}

				});
			}
		}

		if (responseJson.has(RapidProConstants.OPENSRP_ID) && StringUtils.isNotBlank(
				responseJson.getString(RapidProConstants.OPENSRP_ID))) {
			return new ResponseEntity<>(responseJson.toString(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
