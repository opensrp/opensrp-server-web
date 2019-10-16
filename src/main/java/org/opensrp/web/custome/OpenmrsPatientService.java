package org.opensrp.web.custome;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.util.LocationTree;
import org.opensrp.connector.openmrs.schedule.OpenmrsSyncerListener;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.connector.openmrs.service.PatientService;
import org.opensrp.domain.Address;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OpenmrsPatientService extends PatientService {
    private ClientService clientService;
    private EventService eventService;

    @Autowired
    public OpenmrsPatientService(ClientService clientService, EventService eventService){
        this.clientService = clientService;
        this.eventService = eventService;
    }

    @Override
    public JSONArray convertAddressesToOpenmrsJson(Client client, String clientAddress4UUID) throws JSONException{
        List<Address> adl = client.getAddresses();
        if (adl.isEmpty() && !client.getRelationships().isEmpty()){
            adl = clientService.getByBaseEntityId(client.getRelationships().get("mother").get(0)).getAddresses();
        }

        JSONArray jaar = new JSONArray();
        for (Address ad : adl) {
            JSONObject jao = new JSONObject();
            if (ad.getAddressFields() != null) {
                jao.put("address1", ad.getAddressFieldMatchingRegex("(?i)(ADDRESS1|HOUSE_NUMBER|HOUSE|HOUSE_NO|UNIT|UNIT_NUMBER|UNIT_NO)"));
                jao.put("address2", ad.getAddressFieldMatchingRegex("(?i)(ADDRESS2|STREET|STREET_NUMBER|STREET_NO|LANE)"));
                jao.put("address3", ad.getAddressFieldMatchingRegex("(?i)(ADDRESS3|SECTOR|AREA)"));
                jao.put("address4", ad.getAddressFieldMatchingRegex("(?i)(ADDRESS4|SUB_DISTRICT|MUNICIPALITY|TOWN|LOCALITY|REGION)"));

                List<Event> registrationEvents = eventService.findByBaseEntityId(client.getBaseEntityId());
                for (Event event : registrationEvents) {
                    if (event.getEventType().equals("Birth Registration")
                            || event.getEventType().equals("Child Enrollment")) {

                        for (Obs obs2 : event.getObs()) {
                            if (obs2 != null && obs2.getFieldType().equals("formsubmissionField") && obs2
                                    .getFormSubmissionField().equals("Home_Facility") && obs2.getValue() != null) {
                                jao.put("address5", fetchLocationByUUID(obs2.getValue().toString()));
                                break;
                            }
                        }
                    }
                }
            }
            jao.put("address6", ad.getAddressType());
            jao.put("cityVillage", ad.getCityVillage());
            jao.put("countyDistrict", ad.getCountyDistrict());
            jao.put("stateProvince", ad.getStateProvince());
            jao.put("country", ad.getCountry());
            jao.put("postalCode", ad.getPostalCode());
            jao.put("latitude", ad.getLatitude());
            jao.put("longitude", ad.getLongitude());
            if (ad.getStartDate() != null) {
                jao.put("startDate", OPENMRS_DATE.format(ad.getStartDate().toDate()));
            }
            if (ad.getEndDate() != null) {
                jao.put("endDate", OPENMRS_DATE.format(ad.getEndDate().toDate()));
            }

            jaar.put(jao);
        }

        return jaar;
}

}
