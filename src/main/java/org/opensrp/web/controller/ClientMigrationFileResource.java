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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Controller
@RequestMapping(value = "/rest/client-migration-file")
public class ClientMigrationFileResource {

    public static final String MIGRATION_FILENAME_PATTERN = "(\\d).(up|down).sql";
    private static final Logger logger = LogManager.getLogger(ClientMigrationFileResource.class.toString());
    protected ObjectMapper objectMapper;
    private ClientMigrationFileService clientMigrationFileService;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setClientMigrationFileService(ClientMigrationFileService clientMigrationFileService) {
        this.clientMigrationFileService = clientMigrationFileService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> create(@RequestParam(value = "identifier", required = false) String finalIdentifier,
                                         @RequestParam(value = "filename", required = false) String finalFilename,
                                         @RequestParam(value = "jurisdiction", required = false) String jurisdiction,
                                         @RequestParam(value = "version") int version,
                                         @RequestParam("migration_file") MultipartFile migrationFile) {
        ResponseEntity<String> errorResponse = addOrUpdateClientMigrationFile(true, finalIdentifier, finalFilename,
                jurisdiction, version, migrationFile);
        if (errorResponse != null) {
            return errorResponse;
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private ResponseEntity<String> addOrUpdateClientMigrationFile(boolean newRecord, String finalIdentifier, String finalFilename, String jurisdiction,
                                                                  int version, MultipartFile migrationFile) {
        if (migrationFile.isEmpty()) {
            return new ResponseEntity<>("Migration file is empty/missing", HttpStatus.BAD_REQUEST);
        }

        if (!migrationFile.getOriginalFilename().matches(MIGRATION_FILENAME_PATTERN)) {
            return new ResponseEntity<>("The migration filename does not obey the pattern " + MIGRATION_FILENAME_PATTERN, HttpStatus.BAD_REQUEST);
        }

        byte[] bytes;
        try {
            bytes = migrationFile.getBytes();
        } catch (IOException e) {
            logger.error("Error occurred trying to read uploaded file", e);
            return new ResponseEntity<>("Invalid file", HttpStatus.BAD_REQUEST);
        }

        // The file storage can be easily switched here -> Kindly implement using a storage contracts if you do so
        String filename = finalFilename != null ? finalFilename : migrationFile.getOriginalFilename();
        String identifier = finalIdentifier != null ? finalIdentifier : filename;

        String fileContentString = new String(bytes);

        ClientMigrationFile clientMigrationFile = null;
        if (newRecord) {
            clientMigrationFile = new ClientMigrationFile();
        } else {
            clientMigrationFile = clientMigrationFileService.getClientMigrationFile(identifier);

            if (clientMigrationFile == null) {
                return new ResponseEntity<>("Migration file with the identifier does not exist", HttpStatus.BAD_REQUEST);
            }
        }

        updateClientMigrationFileProperties(jurisdiction, version, filename, identifier, fileContentString, clientMigrationFile);

        // TODO: This should be handled on the manifest upload
        //clientMigrationFile.setManifestId(4);

        if (newRecord) {
            clientMigrationFileService.addClientMigrationFile(clientMigrationFile);
        } else {
            clientMigrationFileService.updateClientMigrationFile(clientMigrationFile);
        }

        return null;
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> update(@RequestParam(value = "identifier", required = false) String finalIdentifier,
                                         @RequestParam(value = "filename", required = false) String finalFilename,
                                         @RequestParam(value = "jurisdiction", required = false) String jurisdiction,
                                         @RequestParam(value = "version") int version,
                                         @RequestParam("migration_file") MultipartFile migrationFile) {
        ResponseEntity<String> errorResponse = addOrUpdateClientMigrationFile(false, finalIdentifier, finalFilename,
                jurisdiction, version, migrationFile);
        if (errorResponse != null) {
            return errorResponse;
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private void updateClientMigrationFileProperties(
            @RequestParam(value = "jurisdiction", required = false) String jurisdiction,
            @RequestParam("version") int version, String filename, String identifier,
            String fileContentString, ClientMigrationFile clientMigrationFile) {
        clientMigrationFile.setIdentifier(identifier);
        clientMigrationFile.setFilename(filename);
        clientMigrationFile.setVersion(version);
        clientMigrationFile.setCreatedAt(new Date());
        clientMigrationFile.setFileContents(fileContentString);
        clientMigrationFile.setJurisdiction(jurisdiction);
        clientMigrationFile.setOnObjectStorage(false);
    }

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> get(
            @RequestParam(value = Constants.EndpointParam.LIMIT, required = false) Integer limit,
            @RequestParam(value = Constants.EndpointParam.PAGE) int page) throws JsonProcessingException {
        return new ResponseEntity<>(objectMapper.writeValueAsString(
                clientMigrationFileService.getAllClientMigrationFiles(limit)),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/identifier", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getClientMigrationFileByIdentifier(@RequestParam(value = Constants.EndpointParam.IDENTIFIER) String identifier) throws JsonProcessingException {
        return new ResponseEntity<>(objectMapper.writeValueAsString(
                clientMigrationFileService.getClientMigrationFile(identifier)),
                RestUtils.getJSONUTF8Headers(),
                HttpStatus.OK);
    }

    @RequestMapping(value = "/{identifier:.+}", method = RequestMethod.DELETE)
    public ResponseEntity<String> delete(@PathVariable(Constants.EndpointParam.IDENTIFIER) String identifier) {
        ClientMigrationFile clientMigrationFile = new ClientMigrationFile();
        clientMigrationFile.setIdentifier(identifier);

        clientMigrationFileService.deleteClientMigrationFile(clientMigrationFile);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

}
