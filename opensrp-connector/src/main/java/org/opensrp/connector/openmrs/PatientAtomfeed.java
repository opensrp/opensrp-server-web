package org.opensrp.connector.openmrs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.repository.AllFailedEvents;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.AllMarkers;
import org.ict4h.atomfeed.client.repository.datasource.WebClient;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.ict4h.atomfeed.transaction.AFTransactionManager;
import org.ict4h.atomfeed.transaction.AFTransactionWork;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.connector.atomfeed.AtomfeedService;
import org.opensrp.connector.openmrs.constants.OpenmrsConstants;
import org.opensrp.connector.openmrs.service.EncounterService;
import org.opensrp.connector.openmrs.service.OpenmrsService;
import org.opensrp.connector.openmrs.service.PatientService;
import org.opensrp.domain.Client;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PatientAtomfeed extends OpenmrsService implements EventWorker, AtomfeedService {
	
	private Logger log = Logger.getLogger(getClass().getSimpleName());
	
	private static final String CATEGORY_URL = "/patient/recent";
	
	private AtomFeedProperties atomFeedProperties;
	
	private AFTransactionManager transactionManager;
	
	private AtomFeedClient client;
	
	private PatientService patientService;
	
	private ClientService clientService;
	
	private EventService eventService;
	
	@Value("#{opensrp['opensrp.household.relationship']}")
	protected String householdRelationType;
	
	@Value("#{opensrp['opensrp.mother.relationship']}")
	protected String motherRelationType;
	
	@Autowired
	private EncounterService encounterService;
	
	@Autowired
	public PatientAtomfeed(AllMarkers allMarkers, AllFailedEvents allFailedEvents,
	    @Value("#{opensrp['openmrs.url']}") String baseUrl, PatientService patientService, ClientService clientService,
	    EventService eventService) throws URISyntaxException {
		if (baseUrl != null) {
			OPENMRS_BASE_URL = baseUrl;
		}
		
		this.atomFeedProperties = new AtomFeedProperties();
		this.transactionManager = new AFTransactionManager() {
			
			@Override
			public <T> T executeWithTransaction(AFTransactionWork<T> action) throws RuntimeException {
				return action.execute();
			}
		};
		WebClient webClient = new WebClient();
		
		URI uri = new URI(OPENMRS_BASE_URL + OpenmrsConstants.ATOMFEED_URL + CATEGORY_URL);
		this.client = new AtomFeedClient(new AllFeeds(webClient), allMarkers, allFailedEvents, atomFeedProperties,
		        transactionManager, uri, this);
		
		this.patientService = patientService;
		this.clientService = clientService;
		this.eventService = eventService;
	}
	
	@Override
	public void process(Event event) {
		log.info("Processing item : " + event.getContent());
		try {
			String content = event.getContent().substring(event.getContent().lastIndexOf("/") + 1);
			JSONObject p = patientService.getPatientByUuid(content, true);
			if (p == null) {
				throw new RuntimeException("Patient uuid specified in atomfeed content (" + content
				        + ") did not return any patient.");
			}
			Client c = patientService.convertToClient(p);
			Client existing = clientService.findClient(c, "");
			c.withIsSendToOpenMRS("no");
			JSONArray relationships = patientService.getPersonRelationShip(p.getString("uuid"));
			JSONObject relationship = getRelationship(relationships, householdRelationType);
			if (existing == null) {
				/// back sync to opensrp APP
				String eventType = "Household Registration";
				String entityType = "ec_household";
				c.setBaseEntityId(UUID.randomUUID().toString());
				if (c.getAddresses().size() != 0) {
					c.getAddresses().get(0).withAddressField("country", "BANGLADESH");
				}
				JSONObject pr = p.getJSONObject("person");
				String age = pr.getString("age");
				int ageYear = Integer.parseInt(age);
				if (relationship != null) {
					eventType = "Woman Member Registration";
					entityType = "ec_woman";
					if (c.getGender().equalsIgnoreCase("F") && ageYear >= 5) {
						eventType = "Member Registration";
						entityType = "ec_member";
					} else if (ageYear <= 5) {
						eventType = "Child Registration";
						entityType = "ec_child";
					}
					JSONObject personB = relationship.getJSONObject("personB");
					List<Client> clients = clientService.findAllByIdentifier("OPENMRS_UUID", personB.getString("uuid"));
					if (clients.size() != 0) {
						c.addRelationship("household", clients.get(0).getBaseEntityId());
					}
				}
				clientService.addClient(c, "");
				JSONObject newId = patientService.addThriveId(c.getBaseEntityId(), p);
				log.info("New Client -> Posted Thrive ID back to OpenMRS : " + newId);
				encounterService.makeNewEventForNewClient(c, eventType, entityType);
			} else {
				if (c.getBaseEntityId() != null) {
					List<org.opensrp.domain.Event> events = eventService.findByBaseEntityAndEventTypeContaining(
					    c.getBaseEntityId(), "Registration", "");
					if (events.size() != 0) {
						eventService.updateEventServerVersion(events.get(0), "");
					}
					String srpIdInOpenmrs = c.getBaseEntityId();
					Client cmerged = clientService.mergeClient(c, relationship, "");
					//TODO what if in any case thrive id is assigned to some other patient 
					if (StringUtils.isBlank(srpIdInOpenmrs) || !srpIdInOpenmrs.equalsIgnoreCase(cmerged.getBaseEntityId())) {
						// if openmrs doesnot have openSRP UID or have a different UID then update
						JSONObject newId = patientService.addThriveId(cmerged.getBaseEntityId(), p);
						log.info("Existing Client missing Valid SRP UID -> Posted Thrive ID back to OpenMRS : " + newId);
					}
				}
				
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
			log.warning("client atomfeed error message:" + e.getMessage() + ", and cause :" + e.getCause());
			throw new RuntimeException(e);
		}
	}
	
	private JSONObject getRelationship(JSONArray relationships, String relationshipType) throws JSONException {
		for (int i = 0; i < relationships.length(); i++) {
			JSONObject relationship = new JSONObject();
			relationship = relationships.getJSONObject(i);
			JSONObject relationType = relationship.getJSONObject("relationshipType");
			String relationTypeUuid = relationType.getString("uuid");
			if (relationshipType.equalsIgnoreCase(relationTypeUuid)) {
				return relationship;
			}
		}
		return null;
	}
	
	@Override
	public void cleanUp(Event event) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void processEvents() {
		Logger.getLogger(getClass().getName()).info("Processing PatientAtomfeeds");
		client.processEvents();
	}
	
	@Override
	public void processFailedEvents() {
		client.processFailedEvents();
	}
	
	void setUrl(String url) {
		OPENMRS_BASE_URL = url;
	}
	
	PatientService getPatientService() {
		return patientService;
	}
	
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	
	ClientService getClientService() {
		return clientService;
	}
	
	void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}
	
}
