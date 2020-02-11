package org.opensrp.connector.dhis2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.Client;

public interface DHIS2Tracker {
	
	public JSONArray getTrackCaptureData(Client client) throws JSONException;
	
	public JSONObject sendTrackCaptureData(JSONArray attributes) throws JSONException;
	
}
