package org.opensrp.web.rest;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
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

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/clientForm")
public class ClientFormResource extends RestResource<ClientForm> {

    private static Logger logger = LoggerFactory.getLogger(EventResource.class.toString());

    private ClientFormService clientFormService;

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();

    @Autowired
    public ClientFormResource(ClientFormService clientFormService) {
        this.clientFormService = clientFormService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/search")
    @ResponseBody
    private ResponseEntity<String> searchForFormByFormVersion(@RequestParam(value = "form_identifier", required = true) String formIdentifier
            , @RequestParam(value = "form_version", required = true) String formVersion
            , @RequestParam(value = "current_form_version", required = false) String currentFormVersion) {
        Version formVersionRequired = Version.valueOf(formVersion);
        Version currentFormVersionV = Version.valueOf(currentFormVersion);

        if (currentFormVersionV.greaterThan(formVersionRequired)) {
            return new ResponseEntity<>((String) null, HttpStatus.BAD_REQUEST);
        }

        if (!clientFormService.isClientFormExists(formIdentifier)) {
            return new ResponseEntity<>((String) null, HttpStatus.BAD_REQUEST);
        }

        // Check if the form identifier with that version exists
        ClientFormMetadata clientFormMetadata = clientFormService.getClientFormMetatdataByIdentifierAndVersion(formIdentifier, formVersion);
        CompleteClientForm completeClientForm = null;

        int formId;

        if (clientFormMetadata == null) {
            // Get an older form version
            List<IdVersionTuple> availableFormVersions = clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier);
            Version highestVersion = null;
            IdVersionTuple highestIdVersionTuple = null;

            for (IdVersionTuple availableFormVersion: availableFormVersions) {
                Version semanticFormVersion = Version.valueOf(availableFormVersion.getVersion());

                if (highestVersion == null || semanticFormVersion.greaterThan(highestVersion)) {
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

        completeClientForm = new CompleteClientForm(clientForm, clientFormMetadata);

        return new ResponseEntity<>(gson.toJson(completeClientForm), HttpStatus.OK);
    }

    @Override
    public List<ClientForm> filter(String query) {
        return null;
    }


    @Override
    public List<ClientForm> search(HttpServletRequest request) throws ParseException {
        return null;
    }

    @Override
    public ClientForm getByUniqueId(String uniqueId) {
        return null;
    }

    @Override
    public List<String> requiredProperties() {
        return null;
    }

    @Override
    public ClientForm create(ClientForm entity) {
        return null;
    }

    @Override
    public ClientForm update(ClientForm entity) {
        return null;
    }

    public static class CompleteClientForm {

        public ClientForm clientForm;
        public ClientFormMetadata clientFormMetadata;

        public CompleteClientForm(@NonNull ClientForm clientForm, @NonNull ClientFormMetadata clientFormMetadata) {
            this.clientForm = clientForm;
            this.clientFormMetadata = clientFormMetadata;
        }
    }

}
