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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/rest/identifiersource")
public class IdentifierSourceResource {

	private static Logger logger = LoggerFactory.getLogger(IdentifierSourceResource.class.toString());

	private IdentifierSourceService identifierSourceService;

	protected ObjectMapper objectMapper;

	public static final String IDENTIFIER = "identifier";

	@Autowired
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Autowired
	public IdentifierSourceResource(IdentifierSourceService identifierSourceService) {
		this.identifierSourceService = identifierSourceService;
	}

	@RequestMapping(value = "getAll", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> get() throws JsonProcessingException {
		return new ResponseEntity<>(objectMapper.writeValueAsString(
				identifierSourceService.findAllIdentifierSources()),
				RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getIdentifierSourceByIdentifier(@PathVariable(IDENTIFIER) String identifier)
			throws JsonProcessingException {

		return new ResponseEntity<>(objectMapper.writeValueAsString(
				identifierSourceService.findByIdentifier(identifier)),
				RestUtils.getJSONUTF8Headers(),
				HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
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
