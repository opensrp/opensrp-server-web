package org.opensrp.connector.dhis2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.Address;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.opensrp.service.EventService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HouseholdTracker extends DHIS2Service implements DHIS2Tracker {
	
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(HouseholdTracker.class.toString());
	
	@Autowired
	private DHIS2TrackerService dhis2TrackerService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private DHIS2Connector dhis2Connector;
	
	public HouseholdTracker() {
		
	}
	
	public HouseholdTracker(String dhis2Url, String user, String password) {
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
		
		List<Address> addresses = client.getAddresses();
		String division = "";
		String district = "";
		String upazilla = "";
		String union = "";
		String ward = "";
		String subUnit = "";
		String vaccinationCenter = "";
		try {
			Address address = addresses.get(0);
			division = address.getAddressField("stateProvince");
			district = address.getAddressField("countyDistrict");
			upazilla = address.getAddressField("cityVillage");
			union = address.getAddressField("address1");
			ward = address.getAddressField("address2");
			subUnit = address.getAddressField("address3");
			vaccinationCenter = address.getAddressField("address4");
		}
		catch (Exception e) {
			
			logger.info("No Address found for base entity id:" + client.getBaseEntityId());
		}
		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("division")
		        .toString(), division));
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("district")
		        .toString(), district));
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("upazilla")
		        .toString(), upazilla));
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("union")
		        .toString(), union));
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("ward")
		        .toString(), ward));
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("sub_unit")
		        .toString(), subUnit));
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(
		    DHIS2Settings.HOUSEHOLDIDMAPPING.get("vaccination_center").toString(), vaccinationCenter));
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.COMMONMAPPING.get("client_type")
		        .toString(), "Household"));
		
		// name
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get(firstName)
		        .toString(), client.fullName()));
		
		//Gender
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("gender")
		        .toString(), client.getGender()));
		//"householdCode/Household ID
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson,
		    DHIS2Settings.HOUSEHOLDIDMAPPING.get("Household_ID").toString(), "householdCode"));
		generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureData(attributesAsJson,
		    DHIS2Settings.HOUSEHOLDIDMAPPING.get("phone_number").toString(), "phoneNumber"));
		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(
		    DHIS2Settings.HOUSEHOLDIDMAPPING.get("base_entity_id").toString(), client.getBaseEntityId()));
		//birthdate		
		generateTrackCaptureData.put(dhis2TrackerService.withKnownValue(DHIS2Settings.HOUSEHOLDIDMAPPING.get("birthdate")
		        .toString(), client.getBirthdate().toString()));
		/*JSONObject data = new JSONObject();
		data.put(attributeKey, DHIS2Settings.HOUSEHOLDIDMAPPING.get("birthdate").toString());
		data.put(valueKey, client.getBirthdate());
		generateTrackCaptureData.put(data);*/
		
		/***** get information form Event ******/
		List<Event> event = eventService.findByBaseEntityAndType(client.getBaseEntityId(), "Household Registration", "");
		if (event.size() != 0) {
			List<Obs> observations = event.get(0).getObs();
			/**** Member_Registration_No /Date_Of_Reg ***/
			generateTrackCaptureData.put(dhis2TrackerService.getTrackCaptureDataFromEventByValues(observations,
			    DHIS2Settings.MOTHERIDMAPPING.get("registration_Date").toString(), "Date_Of_Reg"));
		} else {
			logger.info("No event Found of baseEntityid: " + client.getBaseEntityId());
		}
		clientData.put("attributes", generateTrackCaptureData);
		System.err.println("HHData:" + clientData.toString());
		return generateTrackCaptureData;
	}
	
	@Override
	public JSONObject sendTrackCaptureData(JSONArray attributes) throws JSONException {
		String orgUnit = "IDc0HEyjhvL";
		String program = "OprRhyWVIM6";
		String trackedEntity = "MCPQUTHX1Ze";
		JSONObject clientData = new JSONObject();
		
		//JSONArray enrollments = new JSONArray();
		//JSONObject enrollmentsObj = new JSONObject();
		//enrollmentsObj.put("orgUnit", orgUnit);
		//enrollmentsObj.put("program", program);
		/*enrollmentsObj.put("enrollmentDate", DateUtil.getTodayAsString());
		enrollmentsObj.put("incidentDate", DateUtil.getTodayAsString());*/
		//enrollments.put(enrollmentsObj);
		//clientData.put("enrollments", enrollments);		
		dhis2Connector.setAttributes(attributes);
		dhis2Connector.setOrgUnit(orgUnit);
		dhis2Connector.setProgram(program);
		dhis2Connector.setTrackedEntity(trackedEntity);
		return dhis2Connector.send();
		
	}
	
}
