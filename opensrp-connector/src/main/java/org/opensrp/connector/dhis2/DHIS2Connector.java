/**
 * 
 */
package org.opensrp.connector.dhis2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author proshanto
 */
@Service
public class DHIS2Connector extends DHIS2Service {
	
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(DHIS2Connector.class.toString());
	
	private String orgUnit;
	
	private String trackedEntity;
	
	private String program;
	
	private JSONArray attributes = new JSONArray();
	
	private JSONObject clientData = new JSONObject();
	
	private String programKey = "program";
	
	private String orgUnitKey = "orgUnit";
	
	public DHIS2Connector() {
		
	}
	
	public String getTrackedEntity() {
		return trackedEntity;
	}
	
	public void setTrackedEntity(String trackedEntity) {
		this.trackedEntity = trackedEntity;
	}
	
	public JSONArray getAttributes() {
		return attributes;
	}
	
	public void setAttributes(JSONArray attributes) {
		this.attributes = attributes;
	}
	
	public String getOrgUnit() {
		return orgUnit;
	}
	
	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}
	
	public String getProgram() {
		return program;
	}
	
	public void setProgram(String program) {
		this.program = program;
	}
	
	public JSONObject prepareClientData() throws JSONException {
		clientData.put("attributes", attributes);
		clientData.put("trackedEntity", trackedEntity);
		clientData.put(orgUnitKey, orgUnit);
		return clientData;
	}
	
	public JSONObject send() throws JSONException {
		
		String reference = "reference";
		try {
			JSONObject responseTrackEntityInstance = new JSONObject(Dhis2HttpUtils.post(
			    DHIS2_BASE_URL.replaceAll("\\s+", "") + "trackedEntityInstances", "", prepareClientData().toString(),
			    DHIS2_USER.replaceAll("\\s+", ""), DHIS2_PWD.replaceAll("\\s+", "")).body());
			JSONObject trackEntityReference = (JSONObject) responseTrackEntityInstance.get("response");
			
			JSONObject enroll = new JSONObject();
			enroll.put("trackedEntityInstance", trackEntityReference.get(reference));
			enroll.put(programKey, program);
			enroll.put(orgUnitKey, orgUnit);
			
			JSONObject response = new JSONObject(Dhis2HttpUtils.post(DHIS2_BASE_URL.replaceAll("\\s+", "") + "enrollments",
			    "", enroll.toString(), DHIS2_USER.replaceAll("\\s+", ""), DHIS2_PWD.replaceAll("\\s+", "")).body());
			
			response.put("track", trackEntityReference.get(reference));
			return response;
		}
		catch (Exception e) {
			logger.info("send Data: " + e.getMessage());
		}
		
		return null;
		
	}
	
}
