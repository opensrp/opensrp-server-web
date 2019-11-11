package org.opensrp.web.openmrsConnector;

import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.common.util.HttpUtil;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;

public class OpenmrsUser extends OpenmrsUserService {
    private static final String TEAM_MEMBER_URL = "ws/rest/v1/teammodule/member";

    @Override
    public JSONObject getTeamMember(String uuid) throws JSONException{
        HttpResponse op = HttpUtil.get(
                HttpUtil.removeEndingSlash(OPENMRS_BASE_URL) + "/"
                        + TEAM_MEMBER_URL + "/" + uuid,
                "v=full", OPENMRS_USER, OPENMRS_PWD);
        return new JSONObject(op.body());
    }
}
