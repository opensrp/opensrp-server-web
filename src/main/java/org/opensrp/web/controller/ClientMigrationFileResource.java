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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Controller
@RequestMapping(value = "/rest/client-migration-file")
public class ClientMigrationFileResource {

    private static Logger logger = LogManager.getLogger(ClientMigrationFileResource.class.toString());
    private ClientMigrationFileService clientMigrationFileService;
    protected ObjectMapper objectMapper;

    public static final String MIGRATION_FILENAME_PATTERN = "(\\d).(up|down).sql";

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setClientMigrationFileService(ClientMigrationFileService clientMigrationFileService) {
        this.clientMigrationFileService = clientMigrationFileService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> create(@RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "jurisdiction", required = false) String jurisdiction,
            @RequestParam(value = "version") int version,
            @RequestParam("migration_file") MultipartFile migrationFile) {
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
        filename = migrationFile.getOriginalFilename();
        identifier = filename;

        String fileContentString = new String(bytes);

        ClientMigrationFile clientMigrationFile = new ClientMigrationFile();
        clientMigrationFile.setIdentifier(identifier);
        clientMigrationFile.setFilename(filename);
        clientMigrationFile.setVersion(version);
        clientMigrationFile.setCreatedAt(new Date());
        clientMigrationFile.setFileContents(fileContentString);
        clientMigrationFile.setJurisdiction(jurisdiction);
        clientMigrationFile.setOnObjectStorage(false);

        // TODO: This should be handled on the manifest upload
        //clientMigrationFile.setManifestId(4);

        clientMigrationFileService.addClientMigrationFile(clientMigrationFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<String> update(@RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "filename", required = false) String filename,
            @RequestParam(value = "jurisdiction", required = false) String jurisdiction,
            @RequestParam(value = "version") int version,
            @RequestParam("migration_file") MultipartFile migrationFile) {
        if (migrationFile.isEmpty()) {
            return new ResponseEntity<>("Migration file is empty/missing", HttpStatus.BAD_REQUEST);
        }

        if (!migrationFile.getOriginalFilename().matches(MIGRATION_FILENAME_PATTERN)) {
            return new ResponseEntity<>("The migration filename does not obey the pattern " + MIGRATION_FILENAME_PATTERN,
                    HttpStatus.BAD_REQUEST);
        }

        byte[] bytes;
        try {
            bytes = migrationFile.getBytes();
        }
        catch (IOException e) {
            logger.error("Error occurred trying to read uploaded file", e);
            return new ResponseEntity<>("Invalid file", HttpStatus.BAD_REQUEST);
        }

        // The file storage can be easily switched here -> Kindly implement using a storage contracts if you do so
        filename = migrationFile.getOriginalFilename();
        identifier = filename;

        String fileContentString = new String(bytes);

        ClientMigrationFile clientMigrationFile = clientMigrationFileService.getClientMigrationFile(identifier);

        if (clientMigrationFile == null) {
            return new ResponseEntity<>("Migration file with the identifier does not exist", HttpStatus.BAD_REQUEST);
        }

        clientMigrationFile.setIdentifier(identifier);
        clientMigrationFile.setFilename(filename);
        clientMigrationFile.setVersion(version);
        clientMigrationFile.setCreatedAt(new Date());
        clientMigrationFile.setFileContents(fileContentString);
        clientMigrationFile.setJurisdiction(jurisdiction);
        clientMigrationFile.setOnObjectStorage(false);

        clientMigrationFileService.updateClientMigrationFile(clientMigrationFile);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
