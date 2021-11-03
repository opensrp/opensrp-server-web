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


    String RETURN_COUNT = "return_count";

    String TOTAL_RECORDS = "total_records";

    String PAGE_NUMBER = "pageNumber";

    String PAGE_SIZE = "pageSize";

    String ORDER_BY_TYPE = "orderByType";

    String ORDER_BY_FIELD_NAME = "orderByFieldName";

    String LOCATIONS = "locations";

    interface ContentType {
        String APPLICATION_YAML = "application/x-yaml";
        String TEXT_YAML = "text/yaml";
    }

    interface RestEndpointUrls {
        String SETTINGS_V2_URL = "/rest/v2/settings";
    }

    interface RestPartVariables {
        String ID = "id";
    }

    interface EndpointParam {
        String APP_ID = "app_id";
        String STRICT = "strict";
        String APP_VERSION = "app_version";
        String IS_DRAFT= "is_draft";
        String IS_JSON_VALIDATOR= "is_json_validator";
    }

    interface DefaultEndpointParam {
        String FALSE = "false";
    }

    interface JsonForm {
        interface Key {
            String STEP = "step";
            String FIELDS = "fields";
            String KEY = "key";
        }
    }

    interface HealthIndicator {
        String PROBLEMS = "problems";
        String SERVICES = "services";
        String STATUS = "status";
        String INDICATOR = "indicator";
        String EXCEPTION = "exception";
        String TIME = "serverTime";
        String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
        String VERSION = "version";
	    String GIT_BUILD_TIME = "buildTime";
        String GIT_COMMIT_ID = "gitCommitId";
    }
}
