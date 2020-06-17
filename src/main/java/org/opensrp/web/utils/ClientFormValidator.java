package org.opensrp.web.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import org.apache.commons.lang.StringEscapeUtils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import org.json.JSONObject;
import org.opensrp.domain.postgres.ClientForm;
import org.opensrp.service.ClientFormService;
import org.opensrp.web.Constants;
import org.opensrp.web.bean.JsonWidgetValidatorDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.ComposerException;
import springfox.documentation.service.BasicAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ClientFormValidator {

	private static final Logger logger = LoggerFactory.getLogger(ClientFormValidator.class.toString());

	private final ArrayList<String> jsonPathForSubFormReferences = new ArrayList<>();

	private final ArrayList<String> jsonPathForRuleReferences = new ArrayList<>();

	private final ArrayList<String> jsonPathForPropertyFileReferences = new ArrayList<>();

	private final ClientFormService clientFormService;

	private static final String PROPERTIES_FILE_NAME = "properties_file_name";

    public static void main(String[] mainArgs) throws IOException {
        ClientFormValidator clientFormValidator = new ClientFormValidator();
        clientFormValidator.performUpload();
    }

    private void performUpload() throws IOException {
        String assetsFilePath = "/home/ona-kigamba/Documents/Projects/OpenSRP/opensrp-client-reveal/opensrp-reveal/src/main/assets/";
        String formVersion = "0.0.1000";
        String clientFormUrl = "http://192.168.56.103:8082/opensrp/rest/clientForm";
        String basicAuthValue = "Basic ZXBocmFpbTpBbWFuaTEyMw==";

        File file = new File(assetsFilePath);
        HashMap<String, String> ruleFileRelations = new HashMap<>();
        HashMap<String, String> subformFileRelations = new HashMap<>();
        HashSet<String> jsonFormFiles = new HashSet<>();
        HashMap<String, String> formLabels = new HashMap<>();

        // JSONPath Configuration
        Configuration conf = Configuration.defaultConfiguration()
                .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);

        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();

            for (File fileD: files) {
                String fileName = fileD.getName();
                if (fileD.isDirectory()) {
                    System.out.println(fileD.getName() + " @ " + fileD.getAbsolutePath());
                    if (fileName.equals("rule")) {
                        File[] ruleFiles = fileD.listFiles();
                        if (ruleFiles != null) {
                            for (File ruleFile : ruleFiles) {
                                String ruleFileName = ruleFile.getName();
                                if (ruleFileName.endsWith(".yml")) {
                                    System.out.println(
                                            String.format("RULE FILE %s @ %s", ruleFileName, ruleFile.getAbsolutePath()));
                                    if (!ruleFileRelations.containsKey(ruleFileName)) {
                                        ruleFileRelations.put(fileName, null);
                                    }
                                }
                            }
                        }
                    } else if (fileName.equals("json.form")) {
                        File[] formFiles = fileD.listFiles();
                        if (formFiles != null) {
                            for (File formFile : formFiles) {
                                String formFileName = formFile.getName();
                                if (formFileName.endsWith(".json")) {
                                    System.out.println(
                                            String.format("JSON FORM FILE %s @ %s", formFileName, formFile.getAbsolutePath()));
                                    jsonFormFiles.add(formFileName);

                                    String fileStringContent = Files.readString(formFile.toPath());

                                    String title = (JsonPath.using(conf)).parse(fileStringContent).read("$.step1.title");;

                                    title = title == null ? "Sample Form" : title;
                                    formLabels.put(formFileName, title);


                                    // TODO: FETCH DEPENDENT RULE & FORM FILES
                                    for (String jsonPath: jsonPathForSubFormReferences) {
                                        List<String> references = (JsonPath.using(conf)).parse(
                                                fileStringContent).read(jsonPath);

                                        if (references != null && references.size() > 0) {
                                            for (String reference : references) {
                                                subformFileRelations.put(reference + ".json", formFileName);
                                            }
                                        }
                                    }


                                    for (String jsonPath: jsonPathForRuleReferences) {
                                        List<String> references = (JsonPath.using(conf)).parse(
                                                fileStringContent).read(jsonPath);

                                        if (references != null) {
                                            for (String reference : references) {
                                                ruleFileRelations.put(reference, formFileName);
                                            }
                                        }
                                    }
/*

                                    for (String jsonPath: jsonPathForPropertyFileReferences) {
                                        List<String> references = (JsonPath.using(conf)).parse(Files.readString(formFile.toPath())).read(jsonPath);

                                        for(String reference: references) {
                                            ruleFileRelations.put(reference + ".yml", formFileName);
                                        }
                                    }*/


                                } else if (formFile.isDirectory() && formFileName.equals("sub_form")) {
                                    File[] subFormFiles = formFile.listFiles();
                                    if (subFormFiles != null) {
                                        for (File subFormFile : subFormFiles) {
                                            String subFormFileName = subFormFile.getName();
                                            if (subFormFileName.endsWith(".json")) {
                                                System.out.println(
                                                        String.format("JSON SUB-FORM FILE %s @ %s", subFormFileName, subFormFile.getAbsolutePath()));
                                                if (!subformFileRelations.containsKey(subFormFileName)) {
                                                    subformFileRelations.put(subFormFileName, null);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TODO: Fetch each YML file & upload
            for (String ymlFileName: ruleFileRelations.keySet()) {
                File ymlFile = new File(assetsFilePath + "/rule/" + ymlFileName);
                if (ymlFile.exists()) {
                    // Lets upload it
                    JSONObject jsonObject = new JSONObject();
                    //jsonObject.

                    String parentJsonFormFileName = ruleFileRelations.get(ymlFileName);
                    String title = "Sample YML File";

                    if (parentJsonFormFileName != null) {
                        String titleRetrieved = formLabels.get(parentJsonFormFileName);
                        title = titleRetrieved == null ? title : titleRetrieved + " Rules File";
                    }

                    MultipartBuilder multipartBuilder = new MultipartBuilder()
                            .type(MultipartBuilder.FORM)
                            .addFormDataPart("form_version", formVersion)
                            .addFormDataPart("form_identifier", ymlFileName.replace(".yml", ""))
                            .addFormDataPart("form_name", title)
                            .addFormDataPart("form", ymlFileName, RequestBody
                                    .create(MediaType.parse("application/x-yaml"), Files.readAllBytes(ymlFile.toPath())));

                    //.addPart(RequestBody.create(MediaType.parse("application/x-yaml"), Files.readAllBytes(ymlFile.toPath())));

                    if (parentJsonFormFileName != null) {
                        multipartBuilder.addFormDataPart("form_relation", parentJsonFormFileName);
                    }

                    RequestBody requestBody = multipartBuilder.build();

                    Request request = new Request.Builder()
                            .header("Authorization", basicAuthValue)
                            .url(clientFormUrl)
                            .post(requestBody)
                            .build();
                    OkHttpClient httpClient = new OkHttpClient();
                    try {
                        httpClient.setReadTimeout(1, TimeUnit.SECONDS);
                        Response response = httpClient
                                .newCall(request)
                                .execute();
                        if (response.isSuccessful()) {
                            // notification about succesful request
                            System.out.println("Request was successful for " + ymlFileName);
                        } else {
                            // notification about failure request
                            System.out.println("Request was not successful for " + ymlFileName);
                        }
                    }
                    catch (IOException e1) {
                        // notification about other problems
                        System.out.println("Request exception for posting " + ymlFileName);
                        e1.printStackTrace();
                    }
                }
            }
            // TODO: Fetch each JSON sub-form file & upload
            // TODO: Check each JSON form file & upload

            // TODO: Upload the final manifest
        }
    }

    private ClientFormValidator() {
        initialiseSubFormJsonPathReferences();
        initialiseRuleJsonPathReferences();
        initialisePropertiesFileJsonPathReferences();
    }

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

		for (String jsonPath : jsonPathForSubFormReferences) {
			List<String> references = JsonPath.read(jsonForm, jsonPath);
			subFormReferences.addAll(references);
		}

		// Check if the references exist in the DB
		for (String subFormReference : subFormReferences) {
			// If the form does not exist, Add a .json extension & check again
			if (!clientFormService.isClientFormExists(subFormReference) && !clientFormService
					.isClientFormExists(subFormReference + ".json")) {
				missingSubFormReferences.add(subFormReference);
			}
		}

		return missingSubFormReferences;
	}

	@NonNull
	public HashSet<String> checkForMissingRuleReferences(@NonNull String jsonForm) {
		HashSet<String> ruleFileReferences = new HashSet<>();
		HashSet<String> missingRuleFileReferences = new HashSet<>();

		for (String jsonPath : jsonPathForRuleReferences) {
			List<String> references = JsonPath.read(jsonForm, jsonPath);
			ruleFileReferences.addAll(references);
		}

		// Check if the references exist in the DB
		for (String ruleFileReference : ruleFileReferences) {
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

		for (String jsonPath : jsonPathForPropertyFileReferences) {
			String reference = (JsonPath.using(conf)).parse(jsonForm).read(jsonPath);

			if (reference != null) {
				propertyFileReferences.add(reference);
			}
		}

		// Check if the references exist in the DB
		for (String propertyFileReference : propertyFileReferences) {
			if (!clientFormService.isClientFormExists(propertyFileReference) && !clientFormService
					.isClientFormExists(propertyFileReference + ".properties")) {
				missingPropertyFileReferences.add(propertyFileReference);
			}
		}

		return missingPropertyFileReferences;
	}

	public HashSet<String> checkForMissingYamlPropertyFileReferences(@NonNull String fileContent) {
		HashSet<String> propertyFileReferences = new HashSet<>();
		HashSet<String> missingPropertyFileReferences = new HashSet<>();
		try {
			Map<Object, Object> document = new Yaml().load(fileContent);
			if (document.containsKey(PROPERTIES_FILE_NAME)) {
				propertyFileReferences.add((String) document.get(PROPERTIES_FILE_NAME));
			}
			// Check if the references exist in the DB
			for (String propertyFileReference : propertyFileReferences) {
				if (!clientFormService.isClientFormExists(propertyFileReference) && !clientFormService
						.isClientFormExists(propertyFileReference + ".properties")) {
					missingPropertyFileReferences.add(propertyFileReference);
				}
			}
		}
		catch (ComposerException exception) {
			logger.error("Validator parsing a YAML file that doesn't conform in structure", exception);
			return missingPropertyFileReferences;
		}
		return missingPropertyFileReferences;
	}

	@NonNull
	public HashSet<String> performWidgetValidation(@NonNull ObjectMapper objectMapper, @NonNull String formIdentifier,
			@NonNull String clientFormContent) {
		ClientForm formValidator = clientFormService.getMostRecentFormValidator(formIdentifier);
		HashSet<String> fieldsMap = new HashSet<>();
		try {
			if (formValidator != null && formValidator.getJson() != null && !((String) formValidator.getJson()).isEmpty()) {
				logger.info((String) formValidator.getJson());
				objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
				objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
				objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

				String formValidatorString = (String) formValidator.getJson();
				formValidatorString = formValidatorString.substring(1, formValidatorString.length() - 1);
				formValidatorString = StringEscapeUtils.unescapeJava(formValidatorString);

				logger.info(formValidatorString);
				JsonWidgetValidatorDefinition jsonWidgetValidatorDefinition =
						objectMapper.readValue(formValidatorString, JsonWidgetValidatorDefinition.class);
				JsonWidgetValidatorDefinition.WidgetCannotRemove widgetCannotRemove = jsonWidgetValidatorDefinition
						.getCannotRemove();

				if (widgetCannotRemove != null && widgetCannotRemove.getFields() != null
						&& widgetCannotRemove.getFields().size() > 0) {
					JsonNode jsonNode = objectMapper.readTree(clientFormContent);
					fieldsMap.addAll(widgetCannotRemove.getFields());

					Iterator<Map.Entry<String, JsonNode>> iterator = jsonNode.fields();
					while (iterator.hasNext()) {
						Map.Entry<String, JsonNode> jsonFormField = iterator.next();
						if (jsonFormField.getKey().startsWith(Constants.JsonForm.Key.STEP)) {
							JsonNode step = jsonFormField.getValue();
							if (step.has(Constants.JsonForm.Key.FIELDS)) {
								JsonNode fields = step.get(Constants.JsonForm.Key.FIELDS);
								if (fields instanceof ArrayNode) {
									ArrayNode fieldsArray = (ArrayNode) fields;
									for (int i = 0; i < fieldsArray.size(); i++) {
										JsonNode jsonNodeField = fieldsArray.get(i);
										String key = jsonNodeField.get(Constants.JsonForm.Key.KEY).asText();

										if (fieldsMap.remove(key) && fieldsMap.size() == 0) {
											return fieldsMap;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (IOException exception) {
			logger.info("", exception);
		}
		return fieldsMap;
	}
}
