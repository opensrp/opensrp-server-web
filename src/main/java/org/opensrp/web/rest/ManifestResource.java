package org.opensrp.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensrp.domain.Manifest;
import org.opensrp.service.ClientFormService;
import org.opensrp.service.ManifestService;
import org.opensrp.web.Constants;
import org.opensrp.web.utils.FormConfigUtils;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/rest/manifest")
public class ManifestResource {

    private static Logger logger = LoggerFactory.getLogger(ManifestResource.class.toString());
    public static final String FALSE = Boolean.FALSE.toString();
    private ManifestService manifestService;
    private ClientFormService clientFormService;
    protected ObjectMapper objectMapper;
    public static final String IDENTIFIER = "identifier";
    public static final String FORM_VERSION = "forms_version";
    public static final String FORM_IDENTIFIERS = "identifiers";

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setManifestService(ManifestService manifestService) {
        this.manifestService = manifestService;
    }

    @Autowired
    public void setClientFormService(ClientFormService clientFormService) {
        this.clientFormService = clientFormService;
    }

    @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> get() throws JsonProcessingException {
        return new ResponseEntity<>(objectMapper.writeValueAsString(
                manifestService.getAllManifest()),
                RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getManifestByUniqueId(@PathVariable(IDENTIFIER) String identifier) throws JsonProcessingException {

        return new ResponseEntity<>(objectMapper.writeValueAsString(
                manifestService.getManifest(identifier)),
                RestUtils.getJSONUTF8Headers(),
                HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<HttpStatus> create(@RequestBody Manifest manifest) {
        Manifest newManifest = manifest;
        if (manifest.getIdentifier() == null && manifest.getJson() != null) {
            newManifest = generateManifest(manifest.getJson());
        }
        logger.info("Manifest version " + newManifest.getAppVersion());
        manifestService.addManifest(newManifest);
        clientFormService.updateClientFormMetadataIsDraftValueByVersion(false, FormConfigUtils.getFormsVersion(newManifest.getJson()));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<HttpStatus> update(@RequestBody Manifest manifest) {
        manifestService.updateManifest(manifest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> batchSave(@RequestBody Manifest[] manifests) {
        Set<String> tasksWithErrors = manifestService.saveManifests(Arrays.asList(manifests));
        if (tasksWithErrors.isEmpty())
            return new ResponseEntity<>("All Tasks  processed", HttpStatus.CREATED);
        else
            return new ResponseEntity<>(
                    "Tasks with identifiers not processed: " + String.join(",", tasksWithErrors),
                    HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.DELETE, consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> delete(@RequestBody Manifest manifest) {
        manifestService.deleteManifest(manifest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/appId/{appId:.+}", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getManifestByAppId(@PathVariable("appId") String appId) throws JsonProcessingException {
        return new ResponseEntity<>(objectMapper.writeValueAsString(manifestService.getManifestByAppId(appId)), RestUtils.getJSONUTF8Headers(),
                HttpStatus.OK);

    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getManifestByAppIdAndAppVersion(
            @RequestParam(value = Constants.EndpointParam.APP_VERSION) String appVersion,
            @RequestParam(value = Constants.EndpointParam.APP_ID) String appId,
            @RequestParam(value = Constants.EndpointParam.STRICT, defaultValue = Constants.DefaultEndpointParam.FALSE) String strict) throws JsonProcessingException {
        boolean strictSearch = Boolean.parseBoolean(strict.toLowerCase());
        Manifest manifest = null;

        if (strictSearch) {
            manifest = manifestService.getManifest(appId, appVersion);
        } else {
            List<Manifest> manifests = manifestService.getManifestsByAppId(appId);
            if (manifests != null) {
                DefaultArtifactVersion requestedAppVersion = new DefaultArtifactVersion(appVersion);
                DefaultArtifactVersion highestAppVersion = null;

                for (Manifest queryResultManifest: manifests) {
                    DefaultArtifactVersion manifestAppVersion = new DefaultArtifactVersion(queryResultManifest.getAppVersion());

                    int comparisonResult = manifestAppVersion.compareTo(requestedAppVersion);
                    if (comparisonResult == 0) {
                        manifest = queryResultManifest;
                        break;
                    }

                    if (comparisonResult < 0 && (highestAppVersion == null || manifestAppVersion.compareTo(highestAppVersion) > 0)) {
                        highestAppVersion = manifestAppVersion;
                        manifest = queryResultManifest;
                    }
                }
            }
        }

        if (manifest == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(objectMapper.writeValueAsString(manifest), RestUtils.getJSONUTF8Headers(),
                HttpStatus.OK);
    }

    protected Manifest generateManifest(String jsonString) {
        Manifest latestManifest = null;
        List<Manifest> manifestList = manifestService.getAllManifest();
        if (manifestList.size() > 0) {
            latestManifest = manifestService.getAllManifest(1).get(0);
        }
        Manifest generatedManifest = new Manifest();
        JSONArray mergedIdentifiersArray = null;
        if (latestManifest != null) {
            mergedIdentifiersArray = getMergedIdentifiersJSONArray(latestManifest, jsonString);
            String identifier = FormConfigUtils.getNewVersion(latestManifest.getIdentifier());
            logger.info("Generated Identifier -> " + identifier);
            generatedManifest.setIdentifier(identifier);
            generatedManifest.setAppId(latestManifest.getAppId());
            generatedManifest.setAppVersion(latestManifest.getAppVersion());
        }
        String generatedManifestJson = jsonString;
        if (mergedIdentifiersArray != null) {
            JSONObject newJson = new JSONObject(jsonString).put(FORM_IDENTIFIERS, mergedIdentifiersArray);
            generatedManifestJson = newJson.toString();
        }
        generatedManifest.setJson(generatedManifestJson);
        return generatedManifest;
    }

    protected JSONArray getMergedIdentifiersJSONArray(Manifest latestManifest, String json) {
        JSONArray identifiersJsonArray = new JSONArray();
        Set<String> identifiersSet = new HashSet<>();
        if (StringUtils.isNotBlank(latestManifest.getJson())) {
            JSONObject existingJson = new JSONObject(latestManifest.getJson());
            if (existingJson.has(FORM_IDENTIFIERS)) {
                for (Object object : existingJson.getJSONArray(FORM_IDENTIFIERS)) {
                    identifiersSet.add((String) object);
                }
            }
        }
        JSONObject jsonObject = new JSONObject(json);
        if (jsonObject.has(FORM_IDENTIFIERS)) {
            for (Object object : jsonObject.getJSONArray(FORM_IDENTIFIERS)) {
                identifiersSet.add((String) object);
            }
        }
        if (identifiersSet.size() > 0) {
            for (String item : identifiersSet) {
                identifiersJsonArray.put(item);
            }
        }
        return identifiersJsonArray;
    }
}
