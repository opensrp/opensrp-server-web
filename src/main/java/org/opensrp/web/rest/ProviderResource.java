package org.opensrp.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.api.domain.User;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.domain.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/rest/provider")
public class ProviderResource extends RestResource<Provider> {

    @Autowired
    private OpenmrsUserService userService;

    @RequestMapping(value = "authenticate", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, Object> authenticate(HttpServletRequest request) throws JSONException {
        Map<String, Object> resp = new HashMap<String, Object>();
        String u = request.getParameter("u");
        String p = request.getParameter("p");
        String id = request.getParameter("id");

        try {
            if (StringUtils.isBlank(id)) {
                if (StringUtils.isBlank(u) || StringUtils.isBlank(p)) {
                    resp.put("ERROR", "Username and Password not provided.");
                } else {
                    if (userService.authenticate(u, p)) {
                        User usr = userService.getUser(u);
                        JSONObject tm = userService.getTeamMember(usr.getAttribute("_PERSON_UUID").toString());
                        if (tm == null) {
                            resp.put("ERROR", "Given credentails donot belong to a team member.");
                        } else {
                            resp.put("SUCCESS", Boolean.TRUE.toString());
                            resp.put("providerToken", tm.getString("uuid"));
                            resp.put("identifier", tm.getString("identifier"));
                            try {
                                resp.put("name", tm.getJSONObject("person").getString("display"));
                                ArrayList<String> ll = new ArrayList<>();
                                for (int i = 0; i < tm.getJSONArray("location").length(); i++) {
                                    ll.add(tm.getJSONArray("location").getJSONObject(i).getString("name"));
                                }
                                resp.put("locations", ll);
                                resp.put("team", tm.getJSONObject("team").getString("teamName"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        resp.put("ERROR", "Authentication failed with given credentials");
                    }
                }
            } else {
                JSONObject tm = userService.getTeamMember(id);
                if (tm != null) {
                    resp.put("SUCCESS", Boolean.TRUE.toString());
                    resp.put("providerToken", tm.getString("uuid"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }

    @Override
    public List<Provider> search(HttpServletRequest request) throws ParseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Provider getByUniqueId(String uniqueId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> requiredProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Provider create(Provider entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Provider update(Provider entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Provider> filter(String query) {
        // TODO Auto-generated method stub
        return null;
    }

}
