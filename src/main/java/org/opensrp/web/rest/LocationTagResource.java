package org.opensrp.web.rest;

import java.util.List;

import org.opensrp.domain.LocationTag;
import org.opensrp.service.LocationTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping(value = "/rest/location-tag")
public class LocationTagResource {
	
	private static Logger logger = LoggerFactory.getLogger(LocationTagResource.class.toString());
	
	private LocationTagService locationTagService;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	public void setLocationTagService(LocationTagService locationTagService) {
		this.locationTagService = locationTagService;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocationTags() {
		String response = "";
		List<LocationTag> locationTags = locationTagService.getAllLocationTags();
		try {
			response = mapper.writeValueAsString(locationTags);
		}
		catch (JsonProcessingException e) {
			return new ResponseEntity<String>("Json Processing Exception ", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> create(@RequestBody String entity) {
		try {
			LocationTag locationTag;
			locationTag = mapper.readValue(entity, LocationTag.class);
			locationTag.setId(0l);
			locationTagService.addOrUpdateLocationTag(locationTag);
			return new ResponseEntity<>(HttpStatus.CREATED);
			
		}
		catch (JsonProcessingException e) {
			return new ResponseEntity<String>("The request doesn't contain a valid location tag representation ",
			        HttpStatus.BAD_REQUEST);
		}
		catch (IllegalArgumentException e) {
			
			return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
		}
		catch (DuplicateKeyException e) {
			
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.CONFLICT);
		}
	}
	
	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> update(@RequestBody String entity) {
		try {
			LocationTag locationTag = mapper.readValue(entity, LocationTag.class);
			locationTagService.addOrUpdateLocationTag(locationTag);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonProcessingException e) {
			return new ResponseEntity<String>("The request doesn't contain a valid location tag representation ",
			        HttpStatus.BAD_REQUEST);
		}
		catch (IllegalArgumentException e) {
			return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
		}
		catch (DuplicateKeyException e) {
			
			return new ResponseEntity<String>("Location tag name already exists", HttpStatus.CONFLICT);
		}
	}
	
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> delete(@PathVariable("id") Long id) {
		try {
			locationTagService.deleteLocationTag(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
	}
	
}
