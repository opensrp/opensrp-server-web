package org.opensrp.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.domain.ClientMigrationFile;
import org.opensrp.service.ClientMigrationFileService;
import org.opensrp.web.Constants;
import org.opensrp.web.rest.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/rest/client-migration-file")
public class ClientMigrationFileResource {

    private static Logger logger = LogManager.getLogger(ClientMigrationFileResource.class.toString());
    private ClientMigrationFileService clientMigrationFileService;
    protected ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setClientMigrationFileService(ClientMigrationFileService clientMigrationFileService) {
        this.clientMigrationFileService = clientMigrationFileService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<HttpStatus> create(@RequestBody ClientMigrationFile clientMigrationFile) {
        // TODO: Perform any checks
        clientMigrationFileService.addClientMigrationFile(clientMigrationFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<HttpStatus> update(@RequestBody ClientMigrationFile clientMigrationFile) {
        // TODO: Perform any checks
        clientMigrationFileService.updateClientMigrationFile(clientMigrationFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> get(
            @RequestParam(value = Constants.EndpointParam.LIMIT, required = false) int limit,
            @RequestParam(value = Constants.EndpointParam.PAGE) int page) throws JsonProcessingException {
        return new ResponseEntity<>(objectMapper.writeValueAsString(
                clientMigrationFileService.getAllClientMigrationFiles()),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getClientMigrationFileByIdentifier(@PathVariable(Constants.EndpointParam.IDENTIFIER) String identifier) throws JsonProcessingException {
        return new ResponseEntity<>(objectMapper.writeValueAsString(
                clientMigrationFileService.getClientMigrationFile(identifier)),
                RestUtils.getJSONUTF8Headers(),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> delete(@RequestBody ClientMigrationFile clientMigrationFile) {
        clientMigrationFileService.deleteClientMigrationFile(clientMigrationFile);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
