package org.opensrp.web.controller.fake;

import org.opensrp.dto.Action;
import org.opensrp.scheduler.repository.ActionsRepository;
import org.opensrp.web.controller.ActionConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;

@Controller
public class FakeActionController {
	
	private ActionsRepository allActions;
	
	@Autowired
	public FakeActionController(ActionsRepository allActions) {
		this.allActions = allActions;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/action/submit", produces = { MediaType.APPLICATION_JSON_VALUE })
	public String submitFakeAction(@RequestParam("formData") String formData,
	        @RequestParam("anmIdentifier") String anmIdentifier) throws Exception {
		allActions.add(ActionConvertor.toAction(new Gson().fromJson(formData, Action.class), anmIdentifier));
		return "Success!";
	}
	
}
