package org.opensrp.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensrp.domain.IdentifierSource;
import org.opensrp.service.IdentifierSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/rest/identifiersource")
public class IdentifierSourceResource {

	private static Logger logger = LoggerFactory.getLogger(IdentifierSourceResource.class.toString());

	@Autowired
	private IdentifierSourceService identifierSourceService;

	@Autowired
	protected ObjectMapper objectMapper;

	@GetMapping(value = "/getAll", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getAll() throws JsonProcessingException {
		return new ResponseEntity<>(objectMapper.writeValueAsString(
				identifierSourceService.findAllIdentifierSources()),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@GetMapping(value = "/{identifier}", produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<IdentifierSource> getByIdentifier(@PathVariable String identifier) {
		return new ResponseEntity<>(identifierSourceService.findByIdentifier(identifier),
				RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);
	}

	@PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> createOrUpdate(@RequestBody IdentifierSource identifierSource) {
		try {
			identifierSourceService.addOrUpdate(identifierSource);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (IllegalArgumentException e) {
			return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
		}
	}

}
