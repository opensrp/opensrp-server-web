package org.opensrp.web.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.opensrp.service.ClientFormService;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ClientFormValidator {

    private ArrayList<String> jsonPathForSubFormReferences = new ArrayList<>();
    private ArrayList<String> jsonPathForRuleReferences = new ArrayList<>();
    private ArrayList<String> jsonPathForPropertyFileReferences = new ArrayList<>();
    private ClientFormService clientFormService;

    public ClientFormValidator(@NonNull ClientFormService clientFormService) {
        this.clientFormService = clientFormService;

        initialiseSubFormJsonPathReferences();
        initialiseRuleJsonPathReferences();
        initialisePropertiesFileJsonPathReferences();
    }

    private void initialiseSubFormJsonPathReferences() {
        jsonPathForSubFormReferences.add("$.*.fields[*][?(@.type == 'native_radio')].options[*].content_form");
        jsonPathForSubFormReferences.add("$.*.fields[*][?(@.type == 'expansion_panel')].content_form");
    }

    private void initialiseRuleJsonPathReferences() {
        jsonPathForRuleReferences.add("$.*.fields[*].calculation.rules-engine.ex-rules.rules-file");
        jsonPathForRuleReferences.add("$.*.fields[*].relevance.rules-engine.ex-rules.rules-file");
        jsonPathForRuleReferences.add("$.*.fields[*].constraints.rules-engine.ex-rules.rules-file");
    }

    private void initialisePropertiesFileJsonPathReferences() {
        jsonPathForPropertyFileReferences.add("$.properties_file_name");
    }

    @NonNull
    public HashSet<String> checkForMissingFormReferences(@NonNull String jsonForm) {
        HashSet<String> subFormReferences = new HashSet<>();
        HashSet<String> missingSubFormReferences = new HashSet<>();

        for (String jsonPath: jsonPathForSubFormReferences) {
            List<String> references = JsonPath.read(jsonForm, jsonPath);
            subFormReferences.addAll(references);
        }

        // Check if the references exist in the DB
        for (String subFormReference: subFormReferences) {
            if (!clientFormService.isClientFormExists(subFormReference)) {
                // Add a .json extension & check again
                if (!clientFormService.isClientFormExists(subFormReference + ".json")) {
                    missingSubFormReferences.add(subFormReference);
                }
            }
        }

        return missingSubFormReferences;
    }

    @NonNull
    public HashSet<String> checkForMissingRuleReferences(@NonNull String jsonForm) {
        HashSet<String> ruleFileReferences = new HashSet<>();
        HashSet<String> missingRuleFileReferences = new HashSet<>();

        for (String jsonPath: jsonPathForRuleReferences) {
            List<String> references = JsonPath.read(jsonForm, jsonPath);
            ruleFileReferences.addAll(references);
        }

        // Check if the references exist in the DB
        for (String ruleFileReference: ruleFileReferences) {
            if (!clientFormService.isClientFormExists(ruleFileReference)) {
                missingRuleFileReferences.add(ruleFileReference);
            }
        }

        return missingRuleFileReferences;
    }

    @NonNull
    public HashSet<String> checkForMissingPropertyFileReferences(@NonNull String jsonForm) {
        Configuration conf = Configuration.defaultConfiguration()
                .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        HashSet<String> propertyFileReferences = new HashSet<>();
        HashSet<String> missingPropertyFileReferences = new HashSet<>();

        for (String jsonPath: jsonPathForPropertyFileReferences) {
            String reference = (JsonPath.using(conf)).parse(jsonForm).read(jsonPath);

            if (reference != null) {
                propertyFileReferences.add(reference);
            }
        }

        // Check if the references exist in the DB
        for (String propertyFileReference: propertyFileReferences) {
            if (!clientFormService.isClientFormExists(propertyFileReference) && !clientFormService.isClientFormExists(propertyFileReference + ".properties")) {
                missingPropertyFileReferences.add(propertyFileReference);
            }
        }

        return missingPropertyFileReferences;
    }
}
