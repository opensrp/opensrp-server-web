package org.opensrp.web.rest.rapid;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.json.JSONException;
import org.opensrp.connector.openmrs.service.EncounterService;
import org.opensrp.connector.openmrs.service.PatientService;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.web.rest.RestUtils;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.domain.Obs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.nio.charset.StandardCharsets;


@Controller
@RequestMapping("/rest/rapid/client")
public class RapidClientResource {

    static Map<String, String[]> vs = new HashMap<String, String[]>() {{
        put("0", new String[]{"bcg"});
        put("6", new String[]{"penta1", "pcv1", "opv1"});
        put("10", new String[]{"penta2", "pcv2", "opv2"});
        put("14", new String[]{"penta3", "pcv3", "opv3"});
        put("36", new String[]{"measles1"});
        put("60", new String[]{"measles2"});

    }};
    private final ClientService clientService;
    private final EventService eventService;
    private final PatientService ps;
    private final EncounterService es;

    @Autowired
    public RapidClientResource(ClientService clientService, EventService eventService,
                               PatientService ps, EncounterService es) {
        this.clientService = clientService;
        this.eventService = eventService;
        this.ps = ps;
        this.es = es;
    }

    private static ArrayList<String> eligibleVaccines(int age) {
        ArrayList<String> a = new ArrayList<>();
        for (String ag : vs.keySet()) {
            if (Integer.parseInt(ag) <= age) {
                for (String v : vs.get(ag)) {
                    a.add(v);
                }
            }
        }
        return a;
    }

    private static Map<String, Object> makeVaccineCard(List<Event> el, List<String> eligibleVaccines) {
        Map<String, Object> receivedVacines = new HashMap<>();

        System.out.println(el.size() + " FOUND EVENT");
        for (Event e : el) {
            System.out.println();
            for (Obs o : e.getObs()) {
                System.out.println(o + " FOUND OBS");

                if (o.getFormSubmissionField().toLowerCase().matches("bcg|penta1|penta2|penta3|measles1|measles2")) {
                    receivedVacines.put(o.getFormSubmissionField(), o.getValue().toString());
                }
            }
        }

        Map<String, Object> vaccineCard = new HashMap<>();
        for (String vl : vs.keySet()) {
            for (String v : vs.get(vl)) {
                if (receivedVacines.containsKey(v)) {
                    vaccineCard.put(v, receivedVacines.get(v));
                } else if (eligibleVaccines.contains(v)) {
                    vaccineCard.put(v, "due");
                } else {
                    vaccineCard.put(v, "n/a");
                }
            }
        }
        return vaccineCard;
    }

    public static void main(String[] args) {
        System.out.println(new DateTime(new Date()));
        System.out.println("vp".substring("vp".length() - 1).matches("[0-9]"));
        System.out.println(makeVaccineCard(new ArrayList<Event>(), eligibleVaccines(8)));
    }

    @RequestMapping(value = "/cv", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Object> createChild(HttpServletRequest req) {
        Map<String, Object> res = new HashMap<>();
        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String gender = req.getParameter("gender");
        String birthdate = req.getParameter("birthdate");
        String identifier = req.getParameter("identifier");

        if (StringUtils.isBlank(identifier)
                || StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)
                || StringUtils.isBlank(gender) || StringUtils.isBlank(birthdate)) {
            res.put("ERROR", "Request MUST have parameters identifier, firstName, lastName, gender, birthdate with valid values");
            return res;
        }

        Client c = new Client(UUID.randomUUID().toString());
        c.addIdentifier("Program Client ID", identifier);
        c.setFirstName(firstName);
        c.setLastName(lastName);
        c.setGender(gender);
        c.setBirthdate(new DateTime(birthdate));

        clientService.addClient(c);

        res.put("SUCESS", true);
        res.put("SUCCESS", true);
        res.put("data", c);

        try {
            ps.createPatient(c);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return res;
    }

    @RequestMapping(value = "/uvo", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, String> updateVaccineO(HttpServletRequest req, Authentication authentication) {
        Map<String, String> resp = new HashMap<>();
        String id = req.getParameter("clientId");
        String location = req.getParameter("location");
        String date = req.getParameter("date");
        String vaccine = req.getParameter("vaccine");

        try {
            if (StringUtils.isBlank(vaccine)) {
                resp.put("ERROR", "vaccine MUST be specified.");
                return resp;
            }
            if (StringUtils.isBlank(date)) {
                resp.put("ERROR", "date MUST be specified.");
                return resp;
            }
            if (StringUtils.isBlank(id)) {
                resp.put("ERROR", "clientId MUST be specified.");
                return resp;
            }
            Client c = clientService.find(id);
            if (c == null) {
                resp.put("ERROR", "ID Not found");
                return resp;
            }

            Event e = new Event(c.getBaseEntityId(), "Immunization", new DateTime(),
                    "testentity", "demotest", location, System.currentTimeMillis() + "");
            List<Object> values = new ArrayList<>();
            values.add(date);
            e.addObs(new Obs("concept", "txt", "1025AAAAAAAAAAAAAAAA", null, values, "", vaccine));

            eventService.addEvent(e, RestUtils.currentUser(authentication).getUsername());
            resp.put("SUCCESS", Boolean.toString(true));
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("ERROR", e.getMessage());
            return resp;
        }
    }

    @RequestMapping(value = "/c", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Object> findInfo(HttpServletRequest req) {
        String id = req.getParameter("id");
        Map<String, Object> m = new HashMap<String, Object>();
        Client c = clientService.find(id);
        if (c == null) {
            m.put("found", false);
            return m;
        }
        m.put("found", true);
        String eventType = req.getParameter("eventType");
        m.put("client", c);

        int age = Weeks.weeksBetween(c.getBirthdate(), new DateTime().now()).getWeeks();
        m.put("age", age);

        ArrayList<String> ev = eligibleVaccines(age);
        m.put("eligibleVaccines", ev);
        Map<String, Object> receivedVacines = new HashMap<>();
        try {
            List<Event> el = eventService.findByBaseEntityId(c.getBaseEntityId()); //eventService.findEventsBy(c.getBaseEntityId(), null, null, eventType, null, null, null, null, null);
            m.put("vaccineCard", makeVaccineCard(el, ev));
        } catch (Exception e) {
            e.printStackTrace();
        }
        m.put("receivedVaccines", receivedVacines);
        return m;
    }

    public String usernameWithBasicAuthentication(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null) {
            StringTokenizer st = new StringTokenizer(authHeader);
            if (st.hasMoreTokens()) {
                String basic = st.nextToken();

                if (basic.equalsIgnoreCase("Basic")) {
                    String credentials = new String(Base64.decodeBase64(st.nextToken()), StandardCharsets.UTF_8);
                    System.out.println("Credentials: " + credentials);
                    int p = credentials.indexOf(":");
                    if (p != -1) {
                        String login = credentials.substring(0, p).trim();
                        String password = credentials.substring(p + 1).trim();

                        return login;
                    } else {
                        System.out.println("Invalid authentication token");
                    }
                }
            }
        }

        return null;
    }
}

