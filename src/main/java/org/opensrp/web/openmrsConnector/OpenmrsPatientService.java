package org.opensrp.web.openmrsConnector;

import com.mysql.jdbc.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.connector.openmrs.constants.OpenmrsConstants;
import org.opensrp.connector.openmrs.schedule.OpenmrsSyncerListener;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.connector.openmrs.service.PatientService;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.domain.Obs;
import org.opensrp.service.ClientService;
import org.opensrp.service.ConfigService;
import org.opensrp.service.ErrorTraceService;
import org.opensrp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenmrsPatientService extends PatientService {

    private ClientService clientService;
    private OpenmrsLocationService openmrsLocationService;
    private EventService eventService;
    private ConfigService config;
    private ErrorTraceService errorTraceService;
    private static Logger logger = LoggerFactory.getLogger(OpenmrsSyncerListener.class.toString());


    @Autowired
    public OpenmrsPatientService(ClientService clientService, OpenmrsLocationService openmrsLocationService, EventService eventService, ConfigService config, ErrorTraceService errorTraceService) {
        this.clientService = clientService;
        this.openmrsLocationService = openmrsLocationService;
        this.eventService = eventService;
        this.config = config;
        this.errorTraceService = errorTraceService;
    }

    @Override
    public JSONObject convertBaseEntityToOpenmrsJson(Client be, boolean update) throws JSONException {
        JSONObject per = new JSONObject();
        per.put("gender", be.getGender());
        per.put("birthdate", OPENMRS_DATE.format(be.getBirthdate().toDate()));
        per.put("birthdateEstimated", be.getBirthdateApprox());
        if (be.getDeathdate() != null) {
            per.put("deathDate", OPENMRS_DATE.format(be.getDeathdate().toDate()));
        }

        String fn = be.getFirstName() != null && !StringUtils.isEmptyOrWhitespaceOnly(be.getFirstName()) ? be.getFirstName() : "-";
        if (!fn.equals("-")) {
            fn = fn.replaceAll("[^A-Za-z0-9\\s]+", "");
        }

        fn = this.convertToOpenmrsString(fn);
        String mn = be.getMiddleName() == null ? "" : be.getMiddleName();
        if (!mn.equals("-")) {
            mn = mn.replaceAll("[^A-Za-z0-9\\s]+", "");
        }

        mn = this.convertToOpenmrsString(mn);
        String ln = be.getLastName() != null && !be.getLastName().equals(".") ? be.getLastName() : "-";
        if (!ln.equals("-")) {
            ln = ln.replaceAll("[^A-Za-z0-9\\s]+", "");
        }

        ln = this.convertToOpenmrsString(ln);
        String address4UUID = null;
        List<Event> registrationEvents = this.eventService.findByBaseEntityId(be.getBaseEntityId());
        Iterator var9 = registrationEvents.iterator();

        label67:
        while(var9.hasNext()) {
            Event event = (Event)var9.next();
            if (event.getEventType().equals("Birth Registration") || event.getEventType().equals("New Woman Registration")) {
                List<Obs> obs = event.getObs();
                Iterator var12 = obs.iterator();

                while(true) {
                    if (!var12.hasNext()) {
                        break label67;
                    }

                    Obs obs2 = (Obs)var12.next();
                    if (obs2 != null && obs2.getFieldType().equals("formsubmissionField") && obs2.getFormSubmissionField().equals("Home_Facility") && obs2.getValue() != null) {
                        address4UUID = obs2.getValue().toString();
                        String clientAddress4 = this.openmrsLocationService.getLocation(address4UUID).getName();
                        if (be.getAttribute("Home_Facility") != null) {
                            be.removeAttribute("Home_Facility");
                        }

                        be.addAttribute("Home_Facility", clientAddress4);
                    }
                }
            }
        }

        if (!update) {
            per.put("names", new JSONArray("[{\"givenName\":\"" + fn + "\",\"middleName\":\"" + mn + "\", \"familyName\":\"" + ln + "\"}]"));
            per.put("addresses", this.convertAddressesToOpenmrsJson(be, address4UUID));
        }

        return per;
    }

    private String convertToOpenmrsString(String sParam) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(sParam)) {
            return sParam;
        } else {
            String s = sParam.replaceAll("\t", "");
            s = org.apache.commons.lang3.StringUtils.stripAccents(s);
            return s;
        }
    }

    @Override
    public void processClients(List<Client> cl, JSONArray patientsJsonArray, OpenmrsConstants.SchedulerConfig schedulerConfig, String errorType) {
        JSONObject patient = new JSONObject();
        logger.info("Reprocessing_clients " + cl.size());

        for(Iterator var6 = cl.iterator(); var6.hasNext(); patientsJsonArray.put(patient)) {
            Client c = (Client)var6.next();

            try {
                if (c.getIdentifiers().containsKey("M_KIP_ID")) {
                    if (c.getBirthdate() == null) {
                        c.setBirthdate(new DateTime("1970-01-01"));
                    }

                    c.setGender("Female");
                }

                String uuid = c.getIdentifier("OPENMRS_UUID");
                if (uuid == null) {
                    uuid = this.getPatientByIdentifierUUID(c.getBaseEntityId());
                    if (uuid == null) {
                        Iterator var9 = c.getIdentifiers().entrySet().iterator();

                        while(var9.hasNext()) {
                            Map.Entry<String, String> id = (Map.Entry)var9.next();
                            uuid = this.getPatientByIdentifierUUID((String)id.getValue());
                            if (uuid != null) {
                                break;
                            }
                        }
                    }
                }

                if (uuid != null) {
                    logger.info("Updating patient " + uuid);
                    patient = this.updatePatient(c, uuid);
                    if (c.getIdentifier("OPENMRS_UUID") != null) {
                        c.removeIdentifier("OPENMRS_UUID");
                    }

                    c.addIdentifier("OPENMRS_UUID", uuid);
                    this.clientService.addorUpdate(c, false);
                    this.config.updateAppStateToken(schedulerConfig, c.getServerVersion());
                } else {
                    JSONObject patientJson = this.createPatient(c);
                    patient = patientJson;
                    if (patientJson != null && !StringUtils.isNullOrEmpty(patientJson.optString("uuid"))) {
                        c.addIdentifier("OPENMRS_UUID", patientJson.getString("uuid"));
                        this.clientService.addorUpdate(c, false);
                    }

                    this.config.updateAppStateToken(schedulerConfig, c.getServerVersion());
                }
            } catch (Exception var11) {
                logger.error("", var11);
                this.errorTraceService.log(errorType, Client.class.getName(), c.getBaseEntityId(), ExceptionUtils.getStackTrace(var11), "");
            }
        }

    }
}
