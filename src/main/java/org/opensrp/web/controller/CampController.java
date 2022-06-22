package org.opensrp.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.opensrp.web.listener.RapidproMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;

@Profile("rapidpro")
@Controller
public class CampController {

    @Autowired
    private RapidproMessageListener rapidproMessageListener;

    @RequestMapping(method = GET, value = "/message-announcement", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ArrayList<String> campAnnouncement(@RequestParam String provider) {
        rapidproMessageListener.campAnnouncementListener(provider);
        ArrayList<String> response = new ArrayList<String>();
        return response;
    }
}
