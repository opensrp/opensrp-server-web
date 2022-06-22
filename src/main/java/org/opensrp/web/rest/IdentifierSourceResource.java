package org.opensrp.web.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.domain.IdentifierSource;
import org.opensrp.service.IdentifierSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/identifier-source")
public class IdentifierSourceResource {

    private static Logger logger = LogManager.getLogger(IdentifierSourceResource.class.toString());

    @Autowired
    private IdentifierSourceService identifierSourceService;


    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<IdentifierSource>> getAll() {
        return new ResponseEntity<>(identifierSourceService.findAllIdentifierSources(),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @GetMapping(value = "/{identifier}", produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<IdentifierSource> getByIdentifier(@PathVariable String identifier) {
        return new ResponseEntity<>(identifierSourceService.findByIdentifier(identifier),
                RestUtils.getJSONUTF8Headers(),
                HttpStatus.OK);
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> create(@RequestBody IdentifierSource identifierSource) {
        try {
            identifierSourceService.add(identifierSource);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error(String.format("Exception occurred while adding identifier source: %s", e.getMessage()));
            return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> update(@RequestBody IdentifierSource identifierSource) {
        try {
            identifierSourceService.update(identifierSource);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error(String.format("Exception occurred while updating identifier source: %s", e.getMessage()));
            return new ResponseEntity<String>("The request contain illegal argument ", HttpStatus.BAD_REQUEST);
        }
    }

}
