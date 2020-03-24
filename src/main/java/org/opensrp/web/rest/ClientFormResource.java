package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.util.TextUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.joda.time.DateTime;
import org.opensrp.domain.IdVersionTuple;
import org.opensrp.domain.postgres.ClientForm;
import org.opensrp.domain.postgres.ClientFormMetadata;
import org.opensrp.service.ClientFormService;
import org.opensrp.util.DateTimeTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/clientForm")
public class ClientFormResource {

    private static Logger logger = LoggerFactory.getLogger(EventResource.class.toString());

    private ClientFormService clientFormService;
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    @Autowired
    public void setClientFormService(ClientFormService clientFormService) {
        this.clientFormService = clientFormService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    private ResponseEntity<String> searchForFormByFormVersion(@RequestParam(value = "form_identifier") String formIdentifier
            , @RequestParam(value = "form_version") String formVersion
            , @RequestParam(value = "current_form_version", required = false) String currentFormVersion) {
        DefaultArtifactVersion formVersionRequired = new DefaultArtifactVersion(formVersion);
        DefaultArtifactVersion currentFormVersionV = null;
        if (!TextUtils.isEmpty(currentFormVersion)) {
            currentFormVersionV = new DefaultArtifactVersion(currentFormVersion);
        }

        if (currentFormVersionV != null && currentFormVersionV.compareTo(formVersionRequired) > 0) {
            return new ResponseEntity<>((String) null, HttpStatus.BAD_REQUEST);
        }

        if (!clientFormService.isClientFormExists(formIdentifier)) {
            return new ResponseEntity<>((String) null, HttpStatus.BAD_REQUEST);
        }

        // Check if the form identifier with that version exists
        ClientFormMetadata clientFormMetadata = clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion);
        ClientFormService.CompleteClientForm completeClientForm = null;

        long formId;

        if (clientFormMetadata == null) {
            // Get an older form version
            List<IdVersionTuple> availableFormVersions = clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier);
            DefaultArtifactVersion highestVersion = null;
            IdVersionTuple highestIdVersionTuple = null;

            for (IdVersionTuple availableFormVersion: availableFormVersions) {
                DefaultArtifactVersion semanticFormVersion = new DefaultArtifactVersion(availableFormVersion.getVersion());

                if (highestVersion == null || semanticFormVersion.compareTo(highestVersion) > 0) {
                    highestVersion = semanticFormVersion;
                    highestIdVersionTuple = availableFormVersion;
                }
            }

            if (highestVersion == null) {
                return new ResponseEntity<>((String) null, HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                formId = highestIdVersionTuple.getId();
                clientFormMetadata = clientFormService.getClientFormMetadataById(formId);
            }
        } else {
            formId = clientFormMetadata.getId();
        }

        ClientForm clientForm = clientFormService.getClientFormById(formId);
        if (clientForm == null) {
            return new ResponseEntity<>((String) null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        completeClientForm = new ClientFormService.CompleteClientForm(clientForm, clientFormMetadata);

        return new ResponseEntity<>(gson.toJson(completeClientForm), HttpStatus.OK);
    }
    
    //@RequestMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @RequestMapping(headers = { "Accept=multipart/form-data" }, method = RequestMethod.POST)
    @ResponseBody
    private ResponseEntity<String> addClientForm(@RequestParam("form_version") String formVersion,
                                                 @RequestParam("form_identifier") String formIdentifier,
                                                 @RequestParam("form_name") String formName,
                                                 @RequestParam("form") MultipartFile jsonFile,
                                                 @RequestParam(required = false) String module) {
        if (TextUtils.isEmpty(formVersion) || TextUtils.isEmpty(formIdentifier) || TextUtils.isEmpty(formName) || jsonFile.isEmpty()) {
            return new ResponseEntity<>("Required params is empty", HttpStatus.BAD_REQUEST);
        }

        // TOOD: Check the string is a valid version
        DefaultArtifactVersion formVersionV = new DefaultArtifactVersion(formVersion);
        if (!jsonFile.getOriginalFilename().contains(".json")) {
            return new ResponseEntity<>("The form is not a JSON file", HttpStatus.BAD_REQUEST);
        }

        if (jsonFile.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        ClientForm clientForm = new ClientForm();

        byte[] bytes;
        try {
            bytes = jsonFile.getBytes();
        } catch (IOException e) {
            logger.error("Error occurred trying to read uploaded json file", e);
            return new ResponseEntity<>("Invalid file", HttpStatus.BAD_REQUEST);
        }

        String jsonString = new String(bytes);
        logger.info(jsonString);
        clientForm.setJson(jsonString);
        clientForm.setCreatedAt(new Date());

        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setVersion(formVersion);
        clientFormMetadata.setIdentifier(formIdentifier);
        clientFormMetadata.setLabel(formName);
        clientFormMetadata.setCreatedAt(new Date());
        clientFormMetadata.setModule(module);

        ClientFormService.CompleteClientForm completeClientForm = clientFormService.addClientForm(clientForm, clientFormMetadata);

        if (completeClientForm == null) {
            return new ResponseEntity<>("Unknown error. Kindly confirm that the form does not already exist on the server", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
