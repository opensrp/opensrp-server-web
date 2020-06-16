package org.opensrp.web.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.OK;

@Controller
@RequestMapping("/location/")
public class LocationController {
	
	private OpenmrsLocationService openmrsLocationService;
	
	@Autowired
	public LocationController(OpenmrsLocationService openmrsLocationService) {
		this.openmrsLocationService = openmrsLocationService;
	}

	@Autowired
	private ClientService clientService;
	
	@RequestMapping("location-tree")
	@ResponseBody
	public ResponseEntity<String> getLocationTree() throws JSONException {
		return new ResponseEntity<>(new Gson().toJson(openmrsLocationService.getLocationTree()),HttpStatus.OK);
	}
	
	@RequestMapping("location-tree/{uuid}")
	@ResponseBody
	public ResponseEntity<String> getLocationTree(@PathVariable("uuid") String uuid) throws JSONException {
		return new ResponseEntity<>(new Gson().toJson(openmrsLocationService.getLocationTreeOf(uuid)),HttpStatus.OK);
	}

	@RequestMapping(value = "/district-upazila", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> getDistrictAndUpazila(HttpServletRequest request) throws JSONException {
		JSONArray districts = clientService.getDistrictAndUpazila(29);
		return new ResponseEntity<>(districts.toString(), OK);
	}
}
