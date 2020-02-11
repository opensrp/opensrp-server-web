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
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author proshanto
 */
@Service
public class ChildTracker extends DHIS2Service implements DHIS2Tracker {
	
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ChildTracker.class.toString());
	
	@Autowired
	private DHIS2TrackerService dhis2TrackerService;
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private DHIS2Connector dhis2Connector;
	
	public ChildTracker() {
		
	}
	
	public ChildTracker(String dhis2Url, String user, String password) {
		super(dhis2Url, user, password);
	}
	
	@Override
	public JSONArray getTrackCaptureData(Client client) throws JSONException {
		JSONObject clientData = new JSONObject();
		String firstName = "firstName";
		String lastName = "lastName";
		String attributeKey = "attribute";
		String valueKey = "value";
		String gender = "gender";
		String Child_Birth_Certificate = "Child_Birth_Certificate";
		JSONArray generateTrackCaptureData = new JSONArray();
		Map<String, Object> attributes = new HashMap<>();
		attributes = client.getAttributes();
		JSONObject attributesAsJson = new JSONObject(attributes);
		JSONObject clientAsJson = new JSONObject(client);
		
		// firstName
		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(
		    DHIS2Settings.CHILDMAPPING.get(firstName).toString(), client.fullName()));
		
		//Gender
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.CHILDMAPPING.get(gender).toString(),
		    client.getGender()));
		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.CHILDMAPPING.get("base_entity_id")
		        .toString(), client.getBaseEntityId()));
		
		//Child_Birth_Certificate
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson, DHIS2Settings.CHILDMAPPING
		        .get(Child_Birth_Certificate).toString(), Child_Birth_Certificate));
		
		//birthdate	
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.CHILDMAPPING.get("birthdate")
		        .toString(), client.getBirthdate().toString()));
		
		/*JSONObject data = new JSONObject();
		data.put(attributeKey, DHIS2Settings.CHILDMAPPING.get("birthdate").toString());
		data.put(valueKey, client.getBirthdate());
		generateTrackCaptureData.put(data);*/
		
		/**** getting mother info from Client of Mother *******/
		
		List<Event> events = eventService.findByBaseEntityAndType(client.getBaseEntityId(), "Birth Registration");
		if (events.size() != 0) {
			List<Obs> observations = events.get(0).getObs();
			// birth weight
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.CHILDMAPPING.get("birth_weight").toString(), "Birth_Weight"));
			//Mother/Guardian First Name
			
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.CHILDMAPPING.get("mother_guardian_name").toString(), "Mother_Guardian_First_Name"));
			
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.CHILDMAPPING.get("father_guardian_full_name").toString(), "Father_Guardian_Name"));
			
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.CHILDMAPPING.get("mother_guardian_phone_number").toString(), "Mother_Guardian_Number"));
			
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.CHILDMAPPING.get("child_health_facility_name").toString(), "Birth_Facility_Name"));
			
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByHumanReadableValues(observations,
			    DHIS2Settings.CHILDMAPPING.get("which_health_facility_was_the_child_born_in").toString(), "Place_Birth"));
		} else {
			logger.info("No event found:" + client.getBaseEntityId());
		}
		/****** getting information from Event *****/
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.COMMONMAPPING.get("client_type")
		        .toString(), "Child"));
		clientData.put("attributes", generateTrackCaptureData);
		System.err.println("Child Data:" + clientData.toString());
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
