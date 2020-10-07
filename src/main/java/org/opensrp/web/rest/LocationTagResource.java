package org.opensrp.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.opensrp.domain.postgres.LocationTagExample;
import org.opensrp.service.LocationTagService;
import org.opensrp.web.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartregister.domain.LocationTag;
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

import java.util.List;

@Controller
@RequestMapping(value = "/rest/location-tag")
public class LocationTagResource {

	private static Logger logger = LoggerFactory.getLogger(LocationTagResource.class.toString());

	private LocationTagService locationTagService;

	protected ObjectMapper objectMapper;

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Autowired
	public void setLocationTagService(LocationTagService locationTagService) {
		this.locationTagService = locationTagService;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocationTagById(@PathVariable(Constants.RestPartVariables.ID) int identifier)
			throws JsonProcessingException {
		LocationTagExample locationTagExample = new LocationTagExample();
		LocationTagExample.Criteria criteria = locationTagExample.createCriteria();
		criteria.andIdEqualTo(Long.valueOf(identifier));
		List<LocationTag> locationTags = locationTagService.findByLocationTagExample(locationTagExample, 0, 1000);
		return new ResponseEntity<>(objectMapper.writeValueAsString(locationTags), RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getLocationTags() throws JsonProcessingException {
		List<LocationTag> locationTags = locationTagService.getAllLocationTags();
		return new ResponseEntity<>(objectMapper.writeValueAsString(locationTags), RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);

	}

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> create(@RequestBody String entity) {
		try {
			LocationTag locationTag;
			locationTag = objectMapper.readValue(entity, LocationTag.class);
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
			LocationTag locationTag = objectMapper.readValue(entity, LocationTag.class);
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
