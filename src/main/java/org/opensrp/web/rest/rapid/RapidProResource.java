package org.opensrp.web.rest.rapid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.connector.openmrs.service.PatientService;
import org.opensrp.domain.RapidProContact;
import org.opensrp.search.EventSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.smartregister.domain.Address;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.text.MessageFormat.format;
import static org.opensrp.common.AllConstants.Event.*;
import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(value = "/rest/repidpro")
public class RapidProResource {

    private static final Logger logger = LogManager.getLogger(RapidProResource.class.toString());
    private static final Comparator<Client> COMPARATOR = new Comparator<Client>() {

        public int compare(Client c1, Client c2) {
            return c1.getDateCreated().compareTo(c2.getDateCreated());
        }
    };
    private final EventService eventService;
    private final ClientService clientService;
    private final PatientService patientService;
    private final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    @Autowired
    public RapidProResource(ClientService clientService, EventService eventService, PatientService patientService) {
        this.clientService = clientService;
        this.eventService = eventService;
        this.patientService = patientService;
    }

    @RequestMapping(value = "/sync", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    protected ResponseEntity<String> getNewContacts(HttpServletRequest request) {
        List<Event> events;
        List<RapidProContact> rapidProContacts = new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        try {
            String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
            String eventType = getStringFilter(EVENT_TYPE, request);

            Long lastSyncedServerVersion = null;
            if (serverVersion != null) {
                lastSyncedServerVersion = Long.valueOf(serverVersion) + 1;
            }
            Integer limit = getIntegerFilter("limit", request);
            if (limit == null || limit.intValue() == 0) {
                limit = 25;
            }

            if (eventType != null && serverVersion != null && eventType.equals(BIRTH_REGISTRATION)) {
                EventSearchBean eventSearchBean = new EventSearchBean();
                eventSearchBean.setServerVersion(lastSyncedServerVersion);
                eventSearchBean.setEventType(eventType);
                events = eventService.findEvents(eventSearchBean, BaseEntity.SERVER_VERSIOIN, "asc", limit);

                if (events != null && !events.isEmpty()) {
                    for (Event event : events) {
                        try {
                            rapidProContacts = addNewContacts(event);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            JsonArray mVaccArray = (JsonArray) gson.toJsonTree(rapidProContacts, new TypeToken<List<RapidProContact>>() {

            }.getType());

            response.put("contacts", mVaccArray);

            return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
        } catch (Exception e) {
            response.put("msg", "Error occurred");
            e.printStackTrace();
            return new ResponseEntity<>(new Gson().toJson(response), INTERNAL_SERVER_ERROR);
        }
    }

    private String getLocationNameIfId(String locationID) {
        if (locationID != null) {
            String locationName = patientService.fetchLocationByUUID(locationID);
            return locationName.contains("Unknown Location Id") ? locationID : locationName;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(headers = {"Accept=application/json"}, method = POST, value = "/update")
    public ResponseEntity<String> updateMvaccUuid(@RequestBody String data) {
        Map<String, Object> response = new HashMap<>();
        try {
            JSONObject syncData = new JSONObject(data);
            if (!syncData.has("mvacc_uuid") || !syncData.has("baseEntityId")) {
                return new ResponseEntity<>(BAD_REQUEST);
            }

            String mvacc_uuid = gson.fromJson(syncData.getString("mvacc_uuid"), new TypeToken<String>() {

            }.getType());
            String baseEntityId = gson.fromJson(syncData.getString("baseEntityId"), new TypeToken<String>() {

            }.getType());
            if (!StringUtils.isBlank(mvacc_uuid) && mvacc_uuid != null && !StringUtils.isBlank(baseEntityId) && baseEntityId != null) {

                Client client = clientService.getByBaseEntityId(baseEntityId);
                if (client != null) {
                    if (client.getIdentifier(MVACC_UUID_IDENTIFIER_TYPE) != null) {
                        client.removeIdentifier(MVACC_UUID_IDENTIFIER_TYPE);
                    }
                    client.addIdentifier(MVACC_UUID_IDENTIFIER_TYPE, mvacc_uuid);
                    clientService.addorUpdate(client, false);
                    response.put("mvacc_uuid", mvacc_uuid);
                }
            }
        } catch (Exception e) {
            logger.error(format("Updating mvacc_uuid failed", e));
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
    }

    private List<RapidProContact> addNewContacts(Event event) {
        List<RapidProContact> rapidProContacts = new ArrayList<>();
        RapidProContact rapidProContact = new RapidProContact();
        Client contactMother;
        Client contactChild;
        List<Obs> obs;
        if (event.getBaseEntityId() != null && !event.getBaseEntityId().isEmpty() && event.getEventType() != null && event.getEventType().equals(BIRTH_REGISTRATION)) {
            rapidProContact = new RapidProContact();
            contactChild = clientService.getByBaseEntityId(event.getBaseEntityId());
            contactMother = clientService.getByBaseEntityId(contactChild.getRelationships().get("mother").get(0));
            rapidProContact.setServerVersion(event.getServerVersion());
            if (contactMother.getIdentifier(MVACC_UUID_IDENTIFIER_TYPE) != null && !contactMother.getIdentifier(MVACC_UUID_IDENTIFIER_TYPE).isEmpty()) {
                rapidProContact.setMvaccUuid(contactMother.getIdentifier(MVACC_UUID_IDENTIFIER_TYPE));
            }

            obs = event.getObs();
            for (Obs obs2 : obs) {
                if (obs2 != null && "concept".equals(obs2.getFieldType()) && "Mother_Guardian_Number".equals(obs2.getFormSubmissionField()) && obs2.getValue() != null) {
                    rapidProContact.setMotherTel(obs2.getValue().toString());
                }
                if (obs2 != null && "formsubmissionField".equals(obs2.getFieldType()) && "Home_Facility".equals(obs2.getFormSubmissionField()) && obs2.getValue() != null) {
                    rapidProContact.setHomeFacility(getLocationNameIfId(obs2.getValue().toString()));
                }
            }
            if ((rapidProContact.getMotherTel() == null || StringUtils.isBlank(rapidProContact.getMotherTel())) && rapidProContact.getMvaccUuid() == null || !StringUtils.isBlank(rapidProContact.getMvaccUuid())) {
                rapidProContacts.add(rapidProContact);
                return rapidProContacts;
            }

            List<Client> clients = clientService.findByRelationship(contactMother.getBaseEntityId());

            Collections.sort(clients, COMPARATOR);
            Client client;
            for (int count = 0; count < clients.size(); count++) {
                if (count == 0) {
                    client = clients.get(count);
                    rapidProContact.setBirthDate(new SimpleDateFormat(MVACC_DATE_FORMAT).format(client.getBirthdate().toDate()));
                    rapidProContact.setChildName(client.fullName());
                    rapidProContact.setZeirID(client.getIdentifier("ZEIR_ID"));
                } else if (count == 1) {
                    client = clients.get(count);
                    rapidProContact.setC2dob(new SimpleDateFormat(MVACC_DATE_FORMAT).format(client.getBirthdate().toDate()));
                    rapidProContact.setC2name(client.fullName());
                    rapidProContact.setC2zeir(client.getIdentifier("ZEIR_ID"));
                } else if (count == 2) {
                    client = clients.get(count);
                    rapidProContact.setC3dob(new SimpleDateFormat(MVACC_DATE_FORMAT).format(client.getBirthdate().toDate()));
                    rapidProContact.setC3name(client.fullName());
                    rapidProContact.setC3zeir(client.getIdentifier("ZEIR_ID"));
                }
            }
            rapidProContact.setDateJoined(new SimpleDateFormat(MVACC_DATE_FORMAT).format(new Date()));
            rapidProContact.setBaseEntityId(contactMother.getBaseEntityId());
            rapidProContact.setMotherFirstName(contactMother.getFirstName());
            rapidProContact.setMotherSecondName(contactMother.getLastName());
            rapidProContact.setMotherZeirID(contactMother.getIdentifier("M_ZEIR_ID"));
            rapidProContact.setServerVersion(event.getServerVersion());
            List<Address> adl = contactMother.getAddresses();
            if (adl != null && !adl.isEmpty()) {
                for (Address ad : adl) {
                    rapidProContact.setResidentialArea(getLocationNameIfId(ad.getAddressFieldMatchingRegex("(?i)(ADDRESS3|SECTOR|AREA)")));
                    rapidProContact.setHomeAddress(getLocationNameIfId(ad.getAddressFieldMatchingRegex("(?i)(ADDRESS2|STREET|STREET_NUMBER|STREET_NO|LANE)")));
                    rapidProContact.setLandMark(getLocationNameIfId(ad.getAddressFieldMatchingRegex("(?i)(ADDRESS1|HOUSE_NUMBER|HOUSE|HOUSE_NO|UNIT|UNIT_NUMBER|UNIT_NO)")));
                }
            }

        }
        rapidProContacts.add(rapidProContact);
        return rapidProContacts;
    }
}
