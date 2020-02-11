/**
 * 
 */
package org.opensrp.connector.dhis2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.opensrp.service.EventService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author proshanto
 */
@Service
public class MotherTracker extends DHIS2Service implements DHIS2Tracker {
	
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(MotherTracker.class.toString());
	
	@Autowired
	private DHIS2TrackerService dhis2TrackerService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private DHIS2Connector dhis2Connector;
	
	public MotherTracker() {
		
	}
	
	public MotherTracker(String dhis2Url, String user, String password) {
		super(dhis2Url, user, password);
	}
	
	@Override
	public JSONArray getTrackCaptureData(Client client) throws JSONException {
		JSONObject clientData = new JSONObject();
		String firstName = "firstName";
		
		JSONArray generateTrackCaptureData = new JSONArray();
		Map<String, Object> attributes = new HashMap<>();
		attributes = client.getAttributes();
		JSONObject attributesAsJson = new JSONObject(attributes);
		JSONObject clientAsJson = new JSONObject(client);
		
		Map<String, String> identifiers = new HashMap<>();
		identifiers = client.getIdentifiers();
		JSONObject identifiersAsJson = new JSONObject(identifiers);
		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.COMMONMAPPING.get("client_type")
		        .toString(), "Mother"));
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(identifiersAsJson,
		    DHIS2Settings.MOTHERIDMAPPING.get("Member_Registration_No").toString(), "OpenMRS_ID"));
		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.MOTHERIDMAPPING.get(firstName)
		        .toString(), client.fullName()));
		
		//birthdate	
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.MOTHERIDMAPPING.get("birthdate")
		        .toString(), client.getBirthdate().toString()));
		/*JSONObject birthDate = new JSONObject();
		birthDate.put(attributeKey, DHIS2Settings.MOTHERIDMAPPING.get("birthdate").toString());
		birthDate.put(valueKey, client.getBirthdate());
		generateTrackCaptureData.put(birthDate);*/
		// registration date
		/*JSONObject registrationDate = new JSONObject();
		registrationDate.put(attributeKey, DHIS2Settings.MOTHERIDMAPPING.get("registration_Date").toString());
		registrationDate.put(valueKey, client.getDateCreated());
		generateTrackCaptureData.put(registrationDate);*/
		
		// id type
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson, DHIS2Settings.MOTHERIDMAPPING
		        .get("id_type").toString(), "idtype"));
		
		// ID(NID,BRID)
		
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson, DHIS2Settings.MOTHERIDMAPPING
		        .get("NID").toString(), "nationalId"));
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson, DHIS2Settings.MOTHERIDMAPPING
		        .get("BRID").toString(), "birthRegistrationId"));
		
		// phone number
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson, DHIS2Settings.MOTHERIDMAPPING
		        .get("phone_Number").toString(), "phoneNumber"));
		
		// spouseName
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson, DHIS2Settings.MOTHERIDMAPPING
		        .get("houband_Name").toString(), "spouseName"));
		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.MOTHERIDMAPPING.get("base_entity_id")
		        .toString(), client.getBaseEntityId()));
		/***** get information form Event ******/
		List<Event> event = eventService.findByBaseEntityAndType(client.getBaseEntityId(), "New Woman Member Registration");
		if (event.size() != 0) {
			List<Obs> observations = event.get(0).getObs();
			
			/**** epi_card_number ***/
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("EPI_Card_Number").toString(), "epi_card_number"));
			/**** Maritial_Status ***/
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("Maritial_Status").toString(), "maritial_status"));
			/** Couple_No */
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("Couple_No").toString(), "couple_no"));
			
			/** pregnant */
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("pregnant").toString(), "pregnant"));
			
			/** FP_User */
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("FP_User").toString(), "fp_user"));
			
			/** FP_Methods */
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByHumanReadableValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("FP_Methods").toString(), "fp_methods"));
			
			/** know_lmp */
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("know_lmp").toString(), "know_lmp"));
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("LMP").toString(), "latest_lmp"));
			/** EDD */
			
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByHumanReadableValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("know_edd_ultra").toString(), "edd_lmp"));
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("EDD").toString(), "edd"));
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("ultrasound").toString(), "ultrasound_date"));
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("ultrasound_week").toString(), "ultrasound_weeks"));
			
			/****************/
			//member reg date
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("registration_Date").toString(), "member_Reg_Date"));
		} else {
			logger.info("No event found:" + client.getBaseEntityId());
		}
		clientData.put("attributes", generateTrackCaptureData);
		System.err.println("MOther Data:" + clientData.toString());
		return generateTrackCaptureData;
	}
	
	@Override
	public JSONObject sendTrackCaptureData(JSONArray attributes) throws JSONException {
		String orgUnit = "IDc0HEyjhvL";
		String program = "OprRhyWVIM6";
		String trackedEntity = "MCPQUTHX1Ze";
		dhis2Connector.setAttributes(attributes);
		dhis2Connector.setOrgUnit(orgUnit);
		dhis2Connector.setProgram(program);
		dhis2Connector.setTrackedEntity(trackedEntity);
		return dhis2Connector.send();
	}
}
