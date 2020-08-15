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
import org.opensrp.repository.AllDHIS2Marker;
import org.opensrp.service.ClientService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VaccinationTracker extends DHIS2Service {
	
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(VaccinationTracker.class.toString());
	
	@Autowired
	private DHIS2TrackerService dhis2TrackerService;
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private DHIS2Connector dhis2Connector;
	
	@Autowired
	private AllDHIS2Marker allDHIS2Marker;
	
	public VaccinationTracker() {
		
	}
	
	public VaccinationTracker(String dhis2Url, String user, String password) {
		super(dhis2Url, user, password);
	}
	
	public JSONArray getTrackCaptureDataAndSend(List<Event> events) throws JSONException {
		for (Event event : events) {
			List<Obs> observations = event.getObs();
			allDHIS2Marker.updateEventMarker(event.getServerVersion());
			for (Obs obs : observations) {
				
				if (DHIS2Settings.VACCINATION.containsKey(obs.getFormSubmissionField())) {
					Client client = clientService.find(event.getBaseEntityId(), "");
					
					if (client != null)
						sendTrackCaptureData(prepareData(obs, client));
				}
			}
			
		}
		return null;
	}
	
	private JSONArray prepareData(Obs obs, Client client) {
		String attributeKey = "attribute";
		String valueKey = "value";
		JSONArray generateTrackCaptureData = new JSONArray();
		String firstName = "firstName";
		
		try {
			//Name
			generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.VACCINATIONMAPPING.get(firstName)
			        .toString(), client.fullName()));
			generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(
			    DHIS2Settings.VACCINATIONMAPPING.get("base_entity_id").toString(), client.getBaseEntityId()));
			
			Map<String, String> identifiers = new HashMap<>();
			identifiers = client.getIdentifiers();
			JSONObject identifiersAsJson = new JSONObject(identifiers);
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(identifiersAsJson,
			    DHIS2Settings.VACCINATIONMAPPING.get("child_id").toString(), "OpenMRS_ID"));
			
			//vaccination name
			JSONObject vaccieName = new JSONObject();
			vaccieName.put(attributeKey, DHIS2Settings.VACCINATIONMAPPING.get("Vaccina_name"));
			vaccieName.put(valueKey, obs.getFormSubmissionField());
			generateTrackCaptureData.put(vaccieName);
			
			//vaccination dose
			JSONObject vaccieDose = new JSONObject();
			vaccieDose.put(attributeKey, DHIS2Settings.VACCINATIONMAPPING.get("Vaccina_dose"));
			vaccieDose.put(valueKey, DHIS2Settings.VACCINATION.get(obs.getFormSubmissionField()));
			generateTrackCaptureData.put(vaccieDose);
			
			generateTrackCaptureData.put(dhis2TrackerService.getVaccinationDataFromObservation(obs,
			    DHIS2Settings.VACCINATIONMAPPING.get("Vaccina_date").toString()));
			logger.info("Vaccination Data:" + generateTrackCaptureData.toString());
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return generateTrackCaptureData;
	}
	
	public JSONObject sendTrackCaptureData(JSONArray attributes) throws JSONException {
		String orgUnit = "IDc0HEyjhvL";
		String program = "Bxy7WXRMscX";
		String trackedEntity = "MCPQUTHX1Ze";
		dhis2Connector.setAttributes(attributes);
		dhis2Connector.setOrgUnit(orgUnit);
		dhis2Connector.setProgram(program);
		dhis2Connector.setTrackedEntity(trackedEntity);
		return dhis2Connector.send();
	}
}
