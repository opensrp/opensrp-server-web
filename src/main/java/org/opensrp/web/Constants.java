package org.opensrp.web;

public interface Constants {
	
	String LIMIT = "limit";
	
	int DEFAULT_LIMIT = 25;
	
	int DEFAULT_GET_ALL_IDS_LIMIT = 5000;
	
	String WITH_FAMILY_EVENTS = "withFamilyEvents";
	String BASE_ENTITY_IDS = "baseEntityIds";
	String CLIENTS = "clients";
	String FAMILY = "family";
	String MSG = "msg";
	String EVENTS = "events";
	String NO_OF_EVENTS = "no_of_events";
	String ERROR_OCCURRED = "Error occurred";
	String FAILURE = "Failure occurred";
	String DEFAULT_EXCEPTION_HANDLER_MESSAGE = "The server encountered an error processing the request.";
	
	interface ContentType {
		String APPLICATION_YAML = "application/x-yaml";
		String TEXT_YAML = "text/yaml";
	}
	
	interface RestEndpointUrls {
		String SETTINGS_V2_URL = "/rest/v2/settings";
	}
	
	interface RestPartVariables {
		String IDENTIFIER = "identifier";
		String ID = "id";
	}
}
