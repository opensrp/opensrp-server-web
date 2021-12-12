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
    String DEFAULT_EXCEPTION_HANDLER_MESSAGE = "The server encountered an error processing the request." ;

    public static final String RETURN_COUNT= "return_count";
    public static final String TOTAL_RECORDS = "total_records";

    public static final String PAGE_NUMBER = "pageNumber";

    public static final String PAGE_SIZE = "pageSize";

    public static final String ORDER_BY_TYPE = "orderByType";

    public static final String ORDER_BY_FIELD_NAME = "orderByFieldName";

    String LOCATIONS = "locations";

	String NULL = "null";

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
        String LIMIT = "limit";
        String PAGE = "page";
        String IDENTIFIER = "identifier";
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
        String VERSION = "buildVersion";
        String HEALTH_CHECK_PLACEHOLDER = "health_check_%s";
    }

    interface Metrics {
        String LOG4J2 = "log4j2";
        String JVM_THREAD = "jvm_thread";
        String JVM_GC = "jvm_gc";
        String JVM_MEMORY = "jvm_mem";
        String PROCESSOR = "cpu";
        String UPTIME = "uptime";
        String DATABASE = "db";
        String DISK_SPACE = "disk_space";
        String ALL = "all";
    }

    interface Plan {
        String PLAN_IDENTIFIER = "planIdentifier";
        String DATE_FORMAT = "yyyy-MM-dd";
        String CURRENT_DATE = "currentDate";
        String END_DATE = "endDate";
        String REGISTER_FAMILY_ACTION_ID = "registerFamilyActionId";
        String BLOOD_SCREENING_ACTION_ID = "bloodScreeningActionId";
        String BCC_ACTION_ID = "bccActionId";
        String BEDNET_DISTRIBUTION_ACTION_ID = "bednetDistributionActionId";
        String LARVAL_DIPPING_ACTION_ID = "larvalDippingActionId";
        String MOSQUITO_COLLECTION_ID = "mosquitoCollectionActionId";
        String OPENSRP_CASE_CLASSIFICATION_EVENT_ID = "opensrpCaseClassificationEventId";
        String TEMPLATE_NOT_FOUND = "Plan template not found";
        String JURISDICTION_NOT_FOUND = "Jurisdiction not found";
        String BIOPHICS_ID_MISSING = "Biophics id missing from case details event";
        String INVALID_CASE_DETAILS_EVENT = "Case details event is invalid";
        String MISSING_CASE_DETAILS_EVENT = "Case details event does not exist";
        String EXTERNAL_ID = "externalId";
        String PLAN_USER = "opensrp";
        String BFID = "bfid";
        int PLAN_DURATION = 5;
    }
}
