package org.opensrp.web.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.gson.Gson;

@Controller
@RequestMapping("/location/")
public class LocationController {
	private static org.slf4j.Logger logger = LoggerFactory.getLogger(LocationController.class.toString());
	private OpenmrsLocationService openmrsLocationService;
	
	@Autowired
	public LocationController(OpenmrsLocationService openmrsLocationService) {
		this.openmrsLocationService = openmrsLocationService;
	}
	
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

	/**
	 * This method receives
	 * the uuid of a location within the location hierarchy level
	 * the tag name of top location hierarchy level to query locations from
	 * and a list of location tags to be returned
	 *  return a list of all other location within the location hierarchy level matching the requested tags
	 * @param  payload string of JsonObject containing
	 *              locationUUID ,string of any location within the hierarchy level within the a hierarchy,
	 *              locationTopLevel, string of the tag name of top location hierarchy level to query locations from,
	 *              locationTagsQueried, a jsonArray of containing tags of locations to be returned
	 * @return List of a list of all other location within the location hierarchy level matching the requested tags.
	 */

	@RequestMapping(value = "/getLocationsByLevelAndTags", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE , MediaType.TEXT_PLAIN_VALUE },produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocationsWithinALevelAndTags(@RequestBody String payload) throws JSONException {
		logger.info("LocationsByLevelAndTags received payload = "+payload);
		JSONObject jsonObject = new JSONObject(payload);
		return new ResponseEntity<>(new Gson().toJson(
				openmrsLocationService.getLocationsByLevelAndTags(
						jsonObject.getString("locationUUID"),
						jsonObject.getString("locationTopLevel"),
						jsonObject.getJSONArray("locationTagsQueried")
				)
		), HttpStatus.OK);
	}
}
