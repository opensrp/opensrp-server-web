package org.opensrp.web.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.YamlRuleDefinitionReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.IdVersionTuple;
import org.opensrp.domain.Manifest;
import org.opensrp.domain.postgres.ClientForm;
import org.opensrp.domain.postgres.ClientFormMetadata;
import org.opensrp.service.ClientFormService;
import org.opensrp.service.ManifestService;
import org.opensrp.web.Constants;
import org.opensrp.web.utils.ClientFormValidator;
import org.opensrp.web.utils.FormConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping (value = "/rest/clientForm")
public class ClientFormResource {

    public static final String FORM_IDENTIFIERS = "identifiers";

    public static final String FORMS_VERSION = "forms_version";

    private static Logger logger = LogManager.getLogger(ClientFormResource.class.toString());
    protected ObjectMapper objectMapper;
    private ClientFormService clientFormService;
    private ManifestService manifestService;
    private ClientFormValidator clientFormValidator;

    @Autowired
    public void setClientFormService(ClientFormService clientFormService, ManifestService manifestService) {
        this.clientFormService = clientFormService;
        this.manifestService = manifestService;
        this.clientFormValidator = new ClientFormValidator(clientFormService);
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/metadata")
    private ResponseEntity<String> getClientFormMetadataList(
            @RequestParam(value = Constants.EndpointParam.IS_DRAFT, required = false) String isDraftParam,
            @RequestParam(value = Constants.EndpointParam.IS_JSON_VALIDATOR, required = false) String isJsonValidatorParam) throws JsonProcessingException {
        List<ClientFormMetadata> clientFormMetadataList = new ArrayList<>();
        if (StringUtils.isBlank(isDraftParam) && StringUtils.isBlank(isJsonValidatorParam)) {
            clientFormMetadataList = clientFormService.getAllClientFormMetadata();
        } else if (StringUtils.isNotBlank(isDraftParam)) {
            boolean isDraft = Boolean.parseBoolean(isDraftParam.toLowerCase());
            clientFormMetadataList = clientFormService.getDraftsClientFormMetadata(isDraft);
        } else if (StringUtils.isNotBlank(isJsonValidatorParam)) {
            boolean isJsonValidator = Boolean.parseBoolean(isJsonValidatorParam.toLowerCase());
            clientFormMetadataList = clientFormService.getJsonWidgetValidatorClientFormMetadata(isJsonValidator);
        }
        return new ResponseEntity<>(objectMapper.writeValueAsString(clientFormMetadataList.toArray(new ClientFormMetadata[0])),RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    private ResponseEntity<String> searchForFormByFormVersionAndIdentifier(@RequestParam(value = "form_identifier") String formIdentifier
            , @RequestParam(value = "form_version") String formVersion
            , @RequestParam(value = "strict", defaultValue = "false") String strict
            , @RequestParam(value = "current_form_version", required = false) String currentFormVersion
            , @RequestParam(value = "is_json_validator", defaultValue = "false") String isJsonValidatorStringRep) throws JsonProcessingException {
        boolean strictSearch = Boolean.parseBoolean(strict.toLowerCase());
        boolean isJsonValidator = Boolean.parseBoolean(isJsonValidatorStringRep.toLowerCase());
        DefaultArtifactVersion formVersionRequired = new DefaultArtifactVersion(formVersion);
        DefaultArtifactVersion currentFormVersionV = null;
        if (!StringUtils.isEmpty(currentFormVersion)) {
            currentFormVersionV = new DefaultArtifactVersion(currentFormVersion);
        }

        if (currentFormVersionV != null && currentFormVersionV.compareTo(formVersionRequired) > 0) {
            return new ResponseEntity<>((String) null, HttpStatus.BAD_REQUEST);
        }

        if (!clientFormService.isClientFormExists(formIdentifier, isJsonValidator)) {
            return new ResponseEntity<>((String) null, HttpStatus.NOT_FOUND);
        }

        // Check if the form identifier with that version exists
        ClientFormMetadata clientFormMetadata = clientFormService.getClientFormMetadataByIdentifierAndVersion(formIdentifier, formVersion, isJsonValidator);
        ClientFormService.CompleteClientForm completeClientForm = null;

        long formId;

        if (clientFormMetadata == null) {
            // Get an older form version
            clientFormMetadata = getMostRecentVersion(formIdentifier, isJsonValidator);
            if (clientFormMetadata == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                formId = clientFormMetadata.getId();

                if (strictSearch) {
                    if (clientFormMetadata.getVersion().equals(currentFormVersion)) {
                        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                    } else {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                }
            }
        } else {
            formId = clientFormMetadata.getId();
        }

        ClientForm clientForm = clientFormService.getClientFormById(formId);
        if (clientForm == null) {
            return new ResponseEntity<>((String) null, HttpStatus.NOT_FOUND);
        }

        completeClientForm = new ClientFormService.CompleteClientForm(clientForm, clientFormMetadata);

        return new ResponseEntity<>(objectMapper.writeValueAsString(completeClientForm),RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    private final Comparator<IdVersionTuple> idVersionTupleByVersionComparator = (o1, o2) -> {
        final DefaultArtifactVersion artifactVersion = new DefaultArtifactVersion(o1.getVersion());
        final DefaultArtifactVersion otherArtifactVersion = new DefaultArtifactVersion(o2.getVersion());
        return artifactVersion.compareTo(otherArtifactVersion);
    };

    private ClientFormMetadata getMostRecentWithVersion(@NonNull final String formIdentifier, final boolean isJsonValidator, @Nullable final String formVersionCap){
        final List<IdVersionTuple> availableFormIdVersions = clientFormService.getAvailableClientFormMetadataVersionByIdentifier(formIdentifier, isJsonValidator);
        final Optional<IdVersionTuple> maxInCapLimitIdVersionTuple;
        if (formVersionCap != null) {
            final DefaultArtifactVersion artifactVersionCap = new DefaultArtifactVersion(formVersionCap);
            maxInCapLimitIdVersionTuple = availableFormIdVersions.stream()
                    .filter(idVersionTuple -> {
                        final DefaultArtifactVersion artifactVersion = new DefaultArtifactVersion(idVersionTuple.getVersion());
                        return artifactVersion.compareTo(artifactVersionCap) <= 0;
                    })
                    .max(idVersionTupleByVersionComparator);
        } else {
            maxInCapLimitIdVersionTuple = Optional.empty();
        }

        final Optional<IdVersionTuple> maxNoCap;
        if (maxInCapLimitIdVersionTuple.isEmpty()) {
            maxNoCap = availableFormIdVersions.stream()
                    .max(idVersionTupleByVersionComparator);
        } else {
            maxNoCap = Optional.empty();
        }

        if (maxInCapLimitIdVersionTuple.isEmpty() && maxNoCap.isEmpty()) return null;
        final long formMetadataId = maxInCapLimitIdVersionTuple
                .map(IdVersionTuple::getId)
                .orElseGet(() -> maxNoCap.get().getId());

        return clientFormService.getClientFormMetadataById(formMetadataId);
    }

    private ClientFormMetadata getMostRecentVersion(@NonNull String formIdentifier, boolean isJsonValidator) {
        return getMostRecentWithVersion(formIdentifier, isJsonValidator, null);
    }

    @RequestMapping(method = RequestMethod.GET, path = "release-related-files")
    private ResponseEntity<String> getAllFilesRelatedToRelease(@RequestParam(value = "identifier") String releaseIdentifier)
            throws JsonProcessingException {
        if (StringUtils.isBlank(releaseIdentifier)) {
            return new ResponseEntity<>("Request parameter cannot be empty", HttpStatus.BAD_REQUEST);
        }

        Manifest manifest = manifestService.getManifest(releaseIdentifier);
        if (manifest == null || StringUtils.isBlank(manifest.getJson())){
            return new ResponseEntity<>("This manifest does not have any files related to it",
                    HttpStatus.NOT_FOUND);
        }

        JSONObject json = new JSONObject(manifest.getJson());
        if (!json.has(FORM_IDENTIFIERS)){
            return new ResponseEntity<>("This manifest does not have any files related to it",
                    HttpStatus.NO_CONTENT);
        }

        JSONArray fileIdentifiers = json.getJSONArray(FORM_IDENTIFIERS);
        if (fileIdentifiers == null || fileIdentifiers.length() <= 0){
            return new ResponseEntity<>("This manifest does not have any files related to it",
                    HttpStatus.NO_CONTENT);
        }

        List<ClientFormMetadata> clientFormMetadataList = new ArrayList<>();
        final String formVersionFromManifest = deriveNewFormVersionFromManifest(manifest);
        for (int i = 0; i < fileIdentifiers.length(); i++) {
            String fileIdentifier = fileIdentifiers.getString(i);
            ClientFormMetadata clientFormMetadata = getMostRecentWithVersion(fileIdentifier, false, formVersionFromManifest);
            if (clientFormMetadata != null) {
                clientFormMetadataList.add(clientFormMetadata);
            }
        }

        return new ResponseEntity<>(objectMapper.writeValueAsString(clientFormMetadataList.toArray(new ClientFormMetadata[0])),RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(headers = {"Accept=multipart/form-data"}, method = RequestMethod.POST)
    private ResponseEntity<String> addClientForm(@RequestParam(value = "form_version", required = false) String formVersion,
                                                 @RequestParam(value = "form_identifier", required = false) String formIdentifier,
                                                 @RequestParam(value = "form_relation", required = false) String relation,
                                                 @RequestParam("form_name") String formName,
                                                 @RequestParam("form") MultipartFile jsonFile,
                                                 @RequestParam(required = false) String module,
                                                 @RequestParam(value = "is_json_validator", defaultValue = "false") String isJsonValidatorStringRep)
            throws JsonProcessingException {
        boolean isJsonValidator = Boolean.parseBoolean(isJsonValidatorStringRep.toLowerCase());
        if (StringUtils.isBlank(formName) || jsonFile.isEmpty()) {
            return new ResponseEntity<>("Required params is empty", HttpStatus.BAD_REQUEST);
        }

        String fileContentType = jsonFile.getContentType();
        if (!(isClientFormContentTypeValid(fileContentType, jsonFile.getOriginalFilename()) || isPropertiesFile(fileContentType, jsonFile.getOriginalFilename()))) {
            return new ResponseEntity<>("The form is not a JSON/Properties/Yaml file.", HttpStatus.BAD_REQUEST);
        }

        if (jsonFile.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        byte[] bytes;
        try {
            bytes = jsonFile.getBytes();
        } catch (IOException e) {
            logger.error("Error occurred trying to read uploaded json file", e);
            return new ResponseEntity<>("Invalid file", HttpStatus.BAD_REQUEST);
        }

        String identifier = getIdentifier(formIdentifier, jsonFile);
        String version = getFormVersion(formVersion);
        String fileContentString = new String(bytes);

        ResponseEntity<String> errorMessageForInvalidContent1 = checkYamlPropertiesValidity(fileContentType,
                fileContentString,  jsonFile.getOriginalFilename());
        if (errorMessageForInvalidContent1 != null)
            return errorMessageForInvalidContent1;

        ResponseEntity<String> errorMessage = checkJsonFormsReferenceValidity(identifier, isJsonValidator,
                fileContentType, fileContentString, jsonFile.getOriginalFilename());
        if (errorMessage != null)
            return errorMessage;


        logger.debug(fileContentString);
        ClientFormService.CompleteClientForm completeClientForm =
                clientFormService.addClientForm(getClientForm(fileContentString), getClientFormMetadata(version,
                formName, module, isJsonValidator, identifier, relation));

        if (completeClientForm == null) {
            return new ResponseEntity<>("Unknown error. Kindly confirm that the form does not already exist on the server", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private String getFormVersion(String formVersion){
        String version = formVersion;
        if (StringUtils.isBlank(formVersion)) {
            version = generateFormVersion(2);
        }
        return  version;
    }

    private String getIdentifier(String formIdentifier, MultipartFile jsonFile) {
        String identifier = formIdentifier;
        if (StringUtils.isBlank(formIdentifier)) {
            identifier = Paths.get(jsonFile.getOriginalFilename()).getFileName().toString();
        }
        return identifier;
    }


    private ClientForm getClientForm(String fileContentString) {
        ClientForm clientForm = new ClientForm();
        clientForm.setJson(fileContentString);
        clientForm.setCreatedAt(new Date());
        return clientForm;
    }

    private String generateFormVersion(int limit) {
        String formVersion = "0.0.1";
        if (limit < 6) {//I don't think we would have more than six manifests without form versions.
            List<Manifest> manifestList = manifestService.getAllManifest(limit);
            if (manifestList != null && manifestList.size() > 0) {
                for (int i = 0; i < manifestList.size(); i++) {
                    Manifest manifest = manifestList.get(i);
                    final String newVersion = deriveNewFormVersionFromManifest(manifest);
                    if (StringUtils.isNotBlank(newVersion)){
                        formVersion = newVersion;
                        break;
                    }

                    if (i + 1 == manifestList.size()) {
                        generateFormVersion(limit + 1);
                    }
                }
            }
        }

        return formVersion;
    }

    private String deriveNewFormVersionFromManifest(final Manifest manifest){
        final String manifestJson = manifest.getJson();
        if (StringUtils.isBlank(manifestJson)) {
            return null;
        }
        JSONObject manifestJSONObject = new JSONObject(manifestJson);
        if (!manifestJSONObject.has(FORMS_VERSION)) {
            return null;
        }
        final String version = manifestJSONObject.getString(FORMS_VERSION);
        if (StringUtils.isBlank(version)) {
            return null;
        }
        return FormConfigUtils.getNewVersion(version);
    }

    private ClientFormMetadata getClientFormMetadata(String formVersion, String formName,String module,
            boolean isJsonValidator, String identifier, String relation) {
        ClientFormMetadata clientFormMetadata = new ClientFormMetadata();
        clientFormMetadata.setVersion(formVersion);
        clientFormMetadata.setIdentifier(identifier);
        clientFormMetadata.setLabel(formName);
        clientFormMetadata.setIsJsonValidator(isJsonValidator);
        clientFormMetadata.setCreatedAt(new Date());
        clientFormMetadata.setModule(module);
        if(!isJsonValidator) {
            clientFormMetadata.setIsDraft(true); //After any upload all the files will need to be a draft except for the json
            // widget validators.
        }
        if (!StringUtils.isBlank(relation)){
            clientFormMetadata.setRelation(relation);
        }
        return clientFormMetadata;
    }

    private ResponseEntity<String> checkJsonFormsReferenceValidity(
            @RequestParam(value = "form_identifier", required = false) String formIdentifier,
            boolean isJsonValidator, String fileContentType, String fileContentString, String fileName) throws JsonProcessingException {
        if (isJsonFile(fileContentType) && !isJsonValidator) {
            HashSet<String> missingSubFormReferences = clientFormValidator.checkForMissingFormReferences(fileContentString);
            HashSet<String> missingRuleFileReferences = clientFormValidator.checkForMissingRuleReferences(fileContentString);
            HashSet<String> missingPropertyFileReferences = clientFormValidator.checkForMissingPropertyFileReferences(fileContentString);
            String errorMessage;
            if (!missingRuleFileReferences.isEmpty() || !missingSubFormReferences.isEmpty() || !missingPropertyFileReferences.isEmpty()) {
                errorMessage = "Form upload failed.";

                if (!missingSubFormReferences.isEmpty()) {
                    errorMessage += "Kindly make sure that the following sub-form(s) are uploaded before: " + String.join(", ", missingSubFormReferences);
                }

                if (!missingRuleFileReferences.isEmpty()) {
                    errorMessage += "Kindly make sure that the following rules file(s) are uploaded before: " + String.join(", ", missingRuleFileReferences);
                }

                if (!missingPropertyFileReferences.isEmpty()) {
                    errorMessage += "Kindly make sure that the following property file(s) are uploaded before: " + String.join(", ", missingPropertyFileReferences);
                }

                return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
            }

            // Perform check to make sure that fields are not removed
            HashSet<String> missingFields = clientFormValidator.performWidgetValidation(objectMapper, formIdentifier, fileContentString);
            if (missingFields.size() > 0) {
                return new ResponseEntity<>("Kindly make sure that the following fields are still in the form : " + String.join(", ", missingFields)
                        + ". The fields cannot be removed as per the Administrator policy", HttpStatus.BAD_REQUEST);
            }
        }
        else if (isYamlContentType(fileContentType,  fileName)) {
	        HashSet<String> missingPropertyFileReferences = clientFormValidator.checkForMissingYamlPropertyFileReferences(fileContentString);
	        if (!missingPropertyFileReferences.isEmpty()) {
		        String errorMessage = "Form upload failed. Kindly make sure that the following property file(s) are uploaded before: " + String.join(", ", missingPropertyFileReferences);
		        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
	        }
        }
        return null;
    }

	private ResponseEntity<String> checkYamlPropertiesValidity(String fileContentType, String fileContentString, @NonNull String fileName) {
		String errorMessageForInvalidContent = checkValidJsonYamlPropertiesStructure(fileContentString, fileContentType, fileName);
		if (errorMessageForInvalidContent != null) {
			return new ResponseEntity<>("File content error:\n" + errorMessageForInvalidContent, HttpStatus.BAD_REQUEST);
		}
		return null;
	}

    @VisibleForTesting
    @Nullable
    protected String checkValidJsonYamlPropertiesStructure(@NonNull String fileContentString, @NonNull String contentType, @NonNull String fileName) {
        if (isJsonFile(contentType)) {
            try {
                new JSONObject(fileContentString);
                return null;
            } catch (JSONException ex) {
                logger.error("JSON File upload is invalid JSON", ex);
                return ex.getMessage();
            }
        } else if (isYamlContentType(contentType,fileName)) {
            String errorMessage;
            try {
                (new MVELRuleFactory(new YamlRuleDefinitionReader())).createRule(new BufferedReader(new StringReader(fileContentString)));
                return null;
            } catch (Exception ex) {
                logger.error("Rules file upload is invalid YAML rules file", ex);
                errorMessage = ex.getMessage();
            }
            try {
                new Yaml().load(fileContentString);
                return null;
            }catch (Exception ex) {
                logger.error("YAML file upload is invalid", ex);
                errorMessage += "\n\n" + ex.getMessage();
            }
            if (StringUtils.isNotBlank(errorMessage)) {
                return errorMessage;
            }
        } else {
            // This is a properties file
            try {
                Properties properties = new Properties();
                properties.load(new StringReader(fileContentString));
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        return null;
    }

    @VisibleForTesting
    protected boolean isClientFormContentTypeValid(@Nullable String fileContentType, @NonNull String fileName) {
        return fileContentType != null && (isJsonFile(fileContentType) ||
                isYamlContentType(fileContentType, fileName));
    }

    private boolean isJsonFile(@Nullable String fileContentType) {
        return fileContentType.equals(ContentType.APPLICATION_JSON.getMimeType());
    }

    private boolean isYamlContentType(@NonNull String fileContentType, @NonNull String fileName) {
        return fileContentType.equals(Constants.ContentType.APPLICATION_YAML) || fileContentType.equals(Constants.ContentType.TEXT_YAML)
                || (fileContentType.equals(ContentType.APPLICATION_OCTET_STREAM.getMimeType()) && (fileName.endsWith(".yml") || fileName.endsWith(".yaml")));
    }

    @VisibleForTesting
    protected boolean isPropertiesFile(@NonNull String fileContentType, @NonNull String fileName) {
        return fileContentType.equals(ContentType.APPLICATION_OCTET_STREAM.getMimeType()) && fileName.endsWith(".properties");
    }

}
