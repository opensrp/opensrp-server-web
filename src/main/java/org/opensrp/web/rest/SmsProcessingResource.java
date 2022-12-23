package org.opensrp.web.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.postgres.SmsApiProcessingStatus;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.SmsApiProcessingStatusService;
import org.opensrp.util.constants.EventConstants;
import org.opensrp.web.config.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartregister.common.AddressField;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.smartregister.utils.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/rest/sms/")
public class SmsProcessingResource {

	private static Logger logger = LoggerFactory.getLogger(SmsProcessingResource.class.toString());

	private SmsApiProcessingStatusService smsService;

	private ClientService clientService;

	private EventService eventService;

	private RestTemplate restTemplate;

	@Autowired
	ServletContext context;

	Gson gson = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
			.create();

	@Value("#{opensrp['gifted.mom.base.url']}")
	private String baseUrl;

	@Value("#{opensrp['gifted.mom.api.key']}")
	private String apiKey;

	@Value("#{opensrp['gifted.mom.api.secret']}")
	private String apiSecret;

	@Value("#{opensrp['gifted.mom.organization.id']}")
	private String organizationId;

	@Value("#{opensrp['gifted.mom.token.timeout")
	private int tokenTimeout;

	@Value("#{opensrp['gifted.mom.request.size")
	private int requestSize;

	private static final String API_KEY = "apiKey";

	private static final String API_SECRET = "apiSecret";

	private static final String ORGANIZATION_ID = "organizationId";

	private static final String AUTH_URL = "/child/auth";

	private static final String REGISTER_URL = "/child/register";

	private static final String REMOVE_URL = "/child/remove";

	private static final String VISIT_URL = "/child/visit";

	private static final String REQUEST_STATUS_URL = "/child/request/%s/status";

	@Autowired
	public SmsProcessingResource(SmsApiProcessingStatusService smsService) {
		this.smsService = smsService;
	}

	@Autowired
	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}

	@Autowired
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(headers = { "Accept=application/json" }, method = RequestMethod.POST, value = "/status")
	public ResponseEntity<HttpStatus> requestStatusHook(@RequestBody String data) {
		JSONObject statusData = null;

		try {
			if (StringUtils.isEmpty(data)) {
				statusData = new JSONObject(data);
			}
		}
		catch (JSONException e) {
			logger.error("Invalid request data");
			return new ResponseEntity<>(org.springframework.http.HttpStatus.BAD_REQUEST);
		}

		ArrayList<SmsApiProcessingStatus> requests = gson.fromJson(statusData.toString(),
				new TypeToken<ArrayList<SmsApiProcessingStatus>>() {

				}.getType());

		for (SmsApiProcessingStatus request : requests) {
			try {
				smsService.addOrUpdateSmsApiProcessingEntry(request);
			}
			catch (Exception e) {
				logger.error("Request " + request.getId() + " failed to sync", e);
			}
		}

		return new ResponseEntity<>(org.springframework.http.HttpStatus.CREATED);
	}

	public void sendChildRegistrationEvents() {
		logger.info("Sending Child Registration Events...");

		int attempts = 0;
		List<SmsApiProcessingStatus> requests = smsService
				.getStatusListByEventTypeAndRequestStatusAndAttempts(
						SmsApiProcessingStatusService.EventTypes.NEW_BIRTH_REGISTRATION,
						SmsApiProcessingStatusService.RequestStatuses.NEW,
						attempts, requestSize, 0);

		if (!requests.isEmpty()) {
			JSONArray children = new JSONArray();
			JSONObject child = null;

			for (SmsApiProcessingStatus request : requests) {
				Client client = clientService.getByBaseEntityId(request.getBaseEntityId());
				Client mother = clientService.getByBaseEntityId(client.getRelationships().get("mother").get(0));
				Event event = eventService.getById(request.getEventId());

				child = new JSONObject();
				child.put(REQUEST_FIELDS.REQUEST_ID, request.getRequestId());
				child.put(REQUEST_FIELDS.PATIENT_RECORD_ID, client.getBaseEntityId());
				child.put(REQUEST_FIELDS.CARD_ID,
						client.getAttributes().getOrDefault(EventConstants.NFC_CARD_IDENTIFIER, ""));
				child.put(REQUEST_FIELDS.CAREGIVER_NUMBER, mother.getBaseEntityId());
				child.put(REQUEST_FIELDS.DATE_OF_BIRTH, client.getBirthdate());
				child.put(REQUEST_FIELDS.EVENT_DATE, event.getEventDate());
				child.put(REQUEST_FIELDS.FIRST_NAME, client.getFirstName());
				child.put(REQUEST_FIELDS.HEALTH_FACILITY,
						client.getAddresses().get(0).getAddressField(AddressField.LOCALITY));
				child.put(REQUEST_FIELDS.CAREGIVER_LANG, mother.getAttributes().get("Preferred_Language").toString());
				child.put(REQUEST_FIELDS.NEXT_APPOINTMENT_DATE, event.get);
				child.put(REQUEST_FIELDS.NEXT_SERVICES_EXPECTED, "Immunization");
				child.put(REQUEST_FIELDS.REQUESTED_DATE, request.getDateCreated());

				children.put(child);

				updateRequestStatus(request, SmsApiProcessingStatusService.RequestStatuses.SENT,
						SmsApiProcessingStatusService.RequestStatuses.QUEUED);
			}

			String response = sendPostRequest(REGISTER_URL, children);
		}
	}

	public void sendChildVisitEvents() {
		logger.info("Sending Child Visit Events...");

		int attempts = 0;
		List<SmsApiProcessingStatus> requests = smsService
				.getStatusListByEventTypeAndRequestStatusAndAttempts(
						SmsApiProcessingStatusService.EventTypes.NEXT_APPOINTMENT_EVENT,
						SmsApiProcessingStatusService.RequestStatuses.NEW,
						attempts, requestSize, 0);

		if (!requests.isEmpty()) {
			JSONArray children = new JSONArray();
			JSONObject child = null;

			for (SmsApiProcessingStatus request : requests) {
				Client client = clientService.getByBaseEntityId(request.getBaseEntityId());
				Client mother = clientService.getByBaseEntityId(client.getRelationships().get("mother").get(0));

				child = new JSONObject();

				child.put(REQUEST_FIELDS.REQUEST_ID, request.getRequestId());
				child.put(REQUEST_FIELDS.PATIENT_RECORD_ID, client.getBaseEntityId());
				child.put(REQUEST_FIELDS.CARD_ID,
						client.getAttributes().getOrDefault(EventConstants.NFC_CARD_IDENTIFIER, ""));
				child.put(REQUEST_FIELDS.CAREGIVER_NUMBER, mother.getBaseEntityId());
				child.put(REQUEST_FIELDS.DATE_OF_BIRTH, client.getBirthdate());
				child.put(REQUEST_FIELDS.DIFFERENT_HOSPITAL, true);
				child.put(REQUEST_FIELDS.EVENT_DATE, request.getDateCreated());
				child.put(REQUEST_FIELDS.FIRST_NAME, client.getFirstName());
				child.put(REQUEST_FIELDS.CAREGIVER_LANG, mother.getAttributes().get("Preferred_Language").toString());
				child.put(REQUEST_FIELDS.NEXT_APPOINTMENT_DATE, "2020-04-25 00:00:12+00:00");
				child.put(REQUEST_FIELDS.NEXT_SERVICES_EXPECTED, "Immunization");
				child.put(REQUEST_FIELDS.REQUESTED_DATE, request.getDateCreated());
				child.put(REQUEST_FIELDS.TREATMENT_PROVIDED, "Polio immunization");

				children.put(child);

				updateRequestStatus(request, SmsApiProcessingStatusService.RequestStatuses.SENT,
						SmsApiProcessingStatusService.RequestStatuses.QUEUED);
			}

			String response = sendPostRequest(VISIT_URL, children);
		}
	}

	public void sendRemoveChildEvents() {
		logger.info("Sending Remove Child Events...");

		int attempts = 0;
		List<SmsApiProcessingStatus> requests = smsService
				.getStatusListByEventTypeAndRequestStatusAndAttempts(
						SmsApiProcessingStatusService.EventTypes.DEATH,
						SmsApiProcessingStatusService.RequestStatuses.NEW,
						attempts, requestSize, 0);

		if (!requests.isEmpty()) {
			JSONArray requestData = new JSONArray();
			JSONObject entry = null;

			for (SmsApiProcessingStatus request : requests) {
				Client client = clientService.getByBaseEntityId(request.getBaseEntityId());
				Client mother = clientService.getByBaseEntityId(client.getRelationships().get("mother").get(0));

				entry = new JSONObject();

				entry.put(REQUEST_FIELDS.CARD_ID, client.getAttributes().getOrDefault("NFC_Card_Identifier", ""));
				entry.put(REQUEST_FIELDS.CAREGIVER_NUMBER, mother.getBaseEntityId());
				entry.put(REQUEST_FIELDS.EVENT_DATE, request.getDateCreated());
				entry.put(REQUEST_FIELDS.REASON, "Opted out");
				entry.put(REQUEST_FIELDS.PATIENT_RECORD_ID, client.getBaseEntityId());
				// entry.put(REQUEST_FIELDS.REQUEST_ID, request.getRequestId());
				entry.put(REQUEST_FIELDS.REQUESTED_DATE, request.getDateCreated());

				requestData.put(entry);

				updateRequestStatus(request, SmsApiProcessingStatusService.RequestStatuses.SENT,
						SmsApiProcessingStatusService.RequestStatuses.QUEUED);
			}

			String response = sendPostRequest(REMOVE_URL, requestData);
		}
	}

	private void updateRequestStatus(SmsApiProcessingStatus request, String requestStatus, String deliveryStatus) {
		SmsApiProcessingStatus processingStatus = new SmsApiProcessingStatus();
		processingStatus.setId(request.getId());
		processingStatus.setRequestStatus(requestStatus);
		processingStatus.setSmsDeliveryStatus(deliveryStatus);
		processingStatus.setAttempts(request.getAttempts() + 1);

		smsService.addOrUpdateSmsApiProcessingEntry(processingStatus);
	}

	private String getUrl(String uri) {
		return String.format("%s%s", baseUrl, uri);
	}

	private String getAuthToken() {
		// check if bearer token has timed out
		// if timed out, re-fetch anew

		restTemplate = new RestTemplate();

		Map<String, String> urlParams = new HashMap<>();
		urlParams.put(API_KEY, apiKey);
		urlParams.put(API_SECRET, apiSecret);

		return restTemplate.getForObject(getUrl(AUTH_URL), String.class, urlParams);
	}

	private String getBearerToken() {
		return "Bearer " + getAuthToken();
	}

	private String sendPostRequest(String uri, JSONArray requestData) {
		restTemplate = new RestTemplate();

		HttpEntity<String> requestBody = new HttpEntity<>(requestData.toString());

		Map<String, String> urlParams = new HashMap<>();
		urlParams.put(ORGANIZATION_ID, organizationId);

		return restTemplate.postForObject(getUrl(uri), requestBody, String.class, urlParams);
	}

	private String sendRequest(JSONArray requests, Client client) {
		restTemplate = new RestTemplate();

		HttpEntity<String> requestBody = new HttpEntity<>(requests.toString());

		Map<String, String> urlParams = new HashMap<>();
		urlParams.put(ORGANIZATION_ID, organizationId);

		return restTemplate.postForObject(getUrl(REGISTER_URL), requestBody, String.class, urlParams);
	}

	private String getRequestStatus(String requestId) {
		restTemplate = new RestTemplate();

		Map<String, String> urlParams = new HashMap<>();

		return restTemplate.getForObject(getUrl(String.format(REQUEST_STATUS_URL, requestId)), String.class, urlParams);
	}

	public final class REQUEST_FIELDS {

		public static final String REQUEST_ID = "requestID";

		public static final String CARD_ID = "cardID";

		public static final String PATIENT_RECORD_ID = "patientRecordID";

		public static final String CAREGIVER_NUMBER = "careGiverNumber";

		public static final String DATE_OF_BIRTH = "dateOfBirth";

		public static final String EVENT_DATE = "eventDate";

		public static final String FIRST_NAME = "firstName";

		public static final String HEALTH_FACILITY = "healthFacility";

		public static final String CAREGIVER_LANG = "lang";

		public static final String NEXT_APPOINTMENT_DATE = "nextAppointmentDate";

		public static final String NEXT_SERVICES_EXPECTED = "nextServicesExpected";

		public static final String REQUESTED_DATE = "requestedDate";

		public static final String DIFFERENT_HOSPITAL = "differentHospital";

		public static final String TREATMENT_PROVIDED = "treatmentProvided";

		public static final String REASON = "reason";
	}

	public final class REASONS {

		public static final String OPTED_OUT = "Opted out";

		public static final String DEATH = "Death";
	}
}
