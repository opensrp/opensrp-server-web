package org.opensrp.web.rest;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.opensrp.domain.LocationTag;
import org.opensrp.service.LocationTagService;
import org.opensrp.util.DateTypeConverter;
import org.opensrp.util.TaskDateTimeTypeConverter;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

@Controller
@RequestMapping(value = "/rest/locationTag")
public class LocationTagResource {
	
	private static Logger logger = LoggerFactory.getLogger(LocationTagResource.class.toString());
	
	public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
	        .registerTypeAdapter(LocalDate.class, new DateTypeConverter()).create();
	
	private LocationTagService locationTagService;
	
	public static final String IDENTIFIER = "identifier";
	
	@Autowired
	public void setLocationTagService(LocationTagService locationTagService) {
		this.locationTagService = locationTagService;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocationTags() {
		try {
			return new ResponseEntity<>(gson.toJson(locationTagService.getAllLocationTags()),
			        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> create(@RequestBody String entity) {
		try {
			LocationTag locationTag = gson.fromJson(entity, LocationTag.class);
			
			locationTagService.addOrUpdateLocationTag(locationTag);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesn't contain a valid location tag representation" + entity);
			return new ResponseEntity<String>("The request doesn't contain a valid location tag representation",
			        HttpStatus.BAD_REQUEST);
		}
		catch (IllegalArgumentException e) {
			
			return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
		}
		catch (DuplicateKeyException e) {
			
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> update(@RequestBody String entity) {
		try {
			LocationTag locationTag = gson.fromJson(entity, LocationTag.class);
			locationTagService.addOrUpdateLocationTag(locationTag);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesn't contain a valid location tag representation" + entity);
			return new ResponseEntity<String>("The request doesn't contain a valid location tag representation",
			        HttpStatus.BAD_REQUEST);
		}
		catch (IllegalArgumentException e) {
			return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
		}
		catch (DuplicateKeyException e) {
			
			return new ResponseEntity<String>("Location tag name already exists", HttpStatus.OK);
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
