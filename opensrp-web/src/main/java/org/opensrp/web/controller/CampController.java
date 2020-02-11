package org.opensrp.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;

import org.opensrp.web.listener.RapidproMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CampController {
	
	@Autowired
	private RapidproMessageListener rapidproMessageListener;
	
	@RequestMapping(method = GET, value = "/message-announcement")
	@ResponseBody
	public ArrayList<String> campAnnouncement(@RequestParam String provider) {
		rapidproMessageListener.campAnnouncementListener(provider);
		ArrayList<String> response = new ArrayList<String>();
		return response;
	}
}
