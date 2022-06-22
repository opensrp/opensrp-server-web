package org.opensrp.web.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.common.util.HttpAgent;
import org.opensrp.common.util.HttpResponse;
import org.opensrp.dto.VillagesDTO;
import org.opensrp.web.rest.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.MessageFormat;

@Controller
public class ANMLocationController {
    private static Logger logger = LogManager.getLogger(ANMLocationController.class.toString());
    private final String opensrpANMVillagesURL;
    private HttpAgent httpAgent;

    @Autowired
    public ANMLocationController(@Value("#{opensrp['opensrp.anm.villages.url']}") String opensrpANMVillagesURL,
                                 UserController userController,
                                 HttpAgent httpAgent) {
        this.opensrpANMVillagesURL = opensrpANMVillagesURL;
        this.httpAgent = httpAgent;
    }

    @RequestMapping(method = GET, value = "/anm-villages", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<VillagesDTO> villagesForANM(Authentication authentication) {
        HttpResponse response = new HttpResponse(false, null);
        try {
            String anmIdentifier = RestUtils.currentUser(authentication).getUsername();
            response = httpAgent.get(opensrpANMVillagesURL + "?anm-id=" + anmIdentifier);
            VillagesDTO villagesDTOs = new Gson().fromJson(response.body(),
                    new TypeToken<VillagesDTO>() {
                    }.getType());
            logger.info("Fetched Villages for the ANM");
            return new ResponseEntity<>(villagesDTOs, HttpStatus.OK);
        } catch (Exception exception) {
            logger.error(MessageFormat.format("{0} occurred while fetching Village Details for anm. StackTrace: \n {1}", exception.getMessage(), ExceptionUtils.getFullStackTrace(exception)));
            logger.error(MessageFormat.format("Response with status {0} and body: {1} was obtained from {2}", response.isSuccess(), response.body(), opensrpANMVillagesURL));
        }
        return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
    }
}
