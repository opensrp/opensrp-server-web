package org.opensrp.connector.dhis2;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.Client;
import org.opensrp.domain.Obs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DHIS2TrackerService {
	
	private DHIS2Tracker dhis2Tracker;
	
	@Autowired
	private DHIS2TrackerFactory dhis2TrackerFactory;
	
	public DHIS2Tracker getTrackerType(Client client) {
		TrackerType clientType = getClientType(client);
		if (clientType == TrackerType.HOUSEHOLD) {
			
			dhis2Tracker = dhis2TrackerFactory.getTracker(TrackerType.HOUSEHOLD);
		} else if (clientType == TrackerType.MOTHER) {
			dhis2Tracker = dhis2TrackerFactory.getTracker(TrackerType.MOTHER);
		} else if (clientType == TrackerType.CHILD) {
			dhis2Tracker = dhis2TrackerFactory.getTracker(TrackerType.CHILD);
		} else {
			
		}
		
		return dhis2Tracker;
		
	}
	
	public static TrackerType getClientType(Client client) {
		
		Map<String, List<String>> relationships = client.getRelationships();
		TrackerType type = null;
		if (relationships == null) {
			type = TrackerType.HOUSEHOLD;
		} else if (relationships.containsKey("household")) {
			type = TrackerType.MOTHER;
			
		} else if (relationships.containsKey("mother")) {
			type = TrackerType.CHILD;
			
		} else {
			
		}
		
		return type;
		
	}
	
	public JSONObject getTrackCaptureData(JSONObject client, String attributeId, String key) throws JSONException {
		
		JSONObject data = new JSONObject();
		if (client.has(key)) {
			data.put("attribute", attributeId);
			data.put("value", client.getString(key));
			
		} else {
			data.put("attribute", attributeId);
			data.put("value", "");
		}
		return data;
		
	}
	
	public JSONObject withKnownValue(String attributeId, String value) throws JSONException {
		JSONObject data = new JSONObject();
		try {
			data.put("attribute", attributeId);
			data.put("value", value);
		}
		catch (JSONException e) {
			data.put("attribute", attributeId);
			data.put("value", "");
		}
		return data;
		
	}
	
	public JSONObject getTrackCaptureDataFromEventByValues(List<Obs> obsservations, String attributeId,
	                                                       String formSubmissionField) throws JSONException {
		JSONObject data = new JSONObject();
		for (Obs obs : obsservations) {
			
			if (obs.getFormSubmissionField().equalsIgnoreCase(formSubmissionField)) {
				data.put("attribute", attributeId);
				data.put("value", obs.getValues().get(0));
				return data;
			}
		}
		data.put("attribute", attributeId);
		data.put("value", "");
		return data;
		
	}
	
	public JSONObject getVaccinationDataFromObservation(Obs obs, String attributeId) throws JSONException {
		JSONObject data = new JSONObject();
		try {
			
			data.put("attribute", attributeId);
			data.put("value", obs.getValues().get(0));
		}
		catch (Exception e) {
			data.put("attribute", attributeId);
			data.put("value", "");
		}
		return data;
	}
	
	public JSONObject getTrackCaptureDataFromEventByHumanReadableValues(List<Obs> obsservations, String attributeId,
	                                                                    String formSubmissionField) throws JSONException {
		JSONObject data = new JSONObject();
		for (Obs obs : obsservations) {
			
			if (obs.getFormSubmissionField().equalsIgnoreCase(formSubmissionField)) {
				data.put("attribute", attributeId);
				data.put("value", obs.getHumanReadableValues().get(0));
				return data;
			}
		}
		data.put("attribute", attributeId);
		data.put("value", "");
		return data;
		
	}
}
