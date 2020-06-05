package org.opensrp.web.utils;

import com.jayway.jsonpath.JsonPath;
import org.opensrp.repository.ClientFormRepository;
import org.opensrp.service.ClientFormService;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashSet;

public class ClientFormValidator {

    private ArrayList<String> jsonPathForSubFormReferences = new ArrayList<>();
    private ArrayList<String> jsonPathForRuleReferences = new ArrayList<>();
    private ClientFormService clientFormService;

    public ClientFormValidator(@NonNull ClientFormService clientFormService) {
        this.clientFormService = clientFormService;
        initialiseSubFormJsonPathReferences();
        initialiseRuleJsonPathReferences();
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

    @NonNull
    public HashSet<String> checkForMissingFormReferences(@NonNull String jsonForm) {
        HashSet<String> subFormReferences = new HashSet<>();
        HashSet<String> missingSubFormReferences = new HashSet<>();

        for (String jsonPath: jsonPathForSubFormReferences) {
            List<String> references = JsonPath.read(jsonForm, jsonPath);
            subFormReferences.addAll(references);
        }

        // Check if the refernces exist in the DB
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

        for (String jsonPath: jsonPathForSubFormReferences) {
            List<String> references = JsonPath.read(jsonForm, jsonPath);
            ruleFileReferences.addAll(references);
        }

        // Check if the refernces exist in the DB
        for (String ruleFileReference: ruleFileReferences) {
            if (!clientFormService.isClientFormExists(ruleFileReference)) {
                // Add a .json extension & check again
                if (!clientFormService.isClientFormExists(ruleFileReference + ".json")) {
                    missingRuleFileReferences.add(ruleFileReference);
                }
            }
        }

        return missingRuleFileReferences;
    }

    class MissingFileReferences {

    }
}
