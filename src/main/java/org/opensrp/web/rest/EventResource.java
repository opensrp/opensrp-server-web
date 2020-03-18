package org.opensrp.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.search.EventSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.util.DateTimeTypeConverter;
import org.opensrp.web.bean.EventSyncBean;
import org.opensrp.web.bean.SyncParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.text.MessageFormat.format;
import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.CLIENTS_FETCH_BATCH_SIZE;
import static org.opensrp.common.AllConstants.Event.ENTITY_TYPE;
import static org.opensrp.common.AllConstants.Event.EVENT_DATE;
import static org.opensrp.common.AllConstants.Event.EVENT_TYPE;
import static org.opensrp.common.AllConstants.Event.LOCATION_ID;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.opensrp.common.AllConstants.Event.TEAM;
import static org.opensrp.common.AllConstants.Event.TEAM_ID;
import static org.opensrp.web.rest.RestUtils.getDateRangeFilter;
import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(value = "/rest/event")
public class EventResource extends RestResource<Event> {
	
	private static Logger logger = LoggerFactory.getLogger(EventResource.class.toString());
	
	private EventService eventService;
	
	private ClientService clientService;

	@Autowired
	public ObjectMapper objectMapper;
	
	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Value("#{opensrp['opensrp.sync.search.missing.client']}")
	private boolean searchMissingClients;
	
	public static final String DATE_DELETED = "dateDeleted";
	
	@Autowired
	public EventResource(ClientService clientService, EventService eventService) {
		this.clientService = clientService;
		this.eventService = eventService;
	}
	
	@Override
	public Event getByUniqueId(String uniqueId) {
		return eventService.find(uniqueId);
	}
	
	/**
	 * Get an event using the event id
	 * 
	 * @param eventId the event id
	 * @return event with the event id
	 */
	@RequestMapping(value = "/findById", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Event getById(@RequestParam("id") String eventId) {
		return eventService.findById(eventId);
	}
	
	/**
	 * Fetch events ordered by serverVersion ascending order and return the clients associated with
	 * the events
	 * 
	 * @param request
	 * @return a map response with events, clients and optionally msg when an error occurs
	 */
	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	protected ResponseEntity<String> sync(HttpServletRequest request) throws JsonProcessingException {
		EventSyncBean response = new EventSyncBean();
		try {
			String providerId = getStringFilter(PROVIDER_ID, request);
			String locationId = getStringFilter(LOCATION_ID, request);
			String baseEntityId = getStringFilter(BASE_ENTITY_ID, request);
			String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
			String team = getStringFilter(TEAM, request);
			String teamId = getStringFilter(TEAM_ID, request);
			Integer limit = getIntegerFilter("limit", request);
			
			if (team != null || providerId != null || locationId != null || baseEntityId != null || teamId != null) {
				
				return new ResponseEntity<>(objectMapper.writeValueAsString(sync(providerId, locationId, baseEntityId, serverVersion, team, teamId, limit)),
				        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
			} else {
				response.setMsg("specify atleast one filter");
				return new ResponseEntity<>(objectMapper.writeValueAsString(response), BAD_REQUEST);
			}
			
		}
		catch (
		
		Exception e) {
			
			response.setMsg("Error occurred");
			logger.error("", e);
			return new ResponseEntity<>(objectMapper.writeValueAsString(response), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Fetch events ordered by serverVersion ascending order and return the clients associated with
	 * the events
	 * 
	 * @param request
	 * @return a map response with events, clients and optionally msg when an error occurs
	 */
	@RequestMapping(value = "/sync", method = POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	protected ResponseEntity<EventSyncBean> syncByPost(@RequestBody SyncParam syncParam) {
		EventSyncBean response = new EventSyncBean();
		try {
			
			if (syncParam.getTeam() != null || syncParam.getProviderId() != null || syncParam.getLocationId() != null
			        || syncParam.getBaseEntityId() != null || syncParam.getTeamId() != null) {
				
				return new ResponseEntity<>(
				        sync(syncParam.getProviderId(), syncParam.getLocationId(), syncParam.getBaseEntityId(),
				            syncParam.getServerVersion(), syncParam.getTeam(), syncParam.getTeamId(), syncParam.getLimit()),
				        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
			} else {
				response.setMsg("specify atleast one filter");
				return new ResponseEntity<>(response, BAD_REQUEST);
			}
			
		}
		catch (Exception e) {
			
			response.setMsg("Error occurred");
			logger.error("", e);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private EventSyncBean sync(String providerId, String locationId, String baseEntityId, String serverVersion, String team,
	        String teamId, Integer limit) {
		Long lastSyncedServerVersion = null;
		if (serverVersion != null) {
			lastSyncedServerVersion = Long.valueOf(serverVersion) + 1;
		}
		
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setTeam(team);
		eventSearchBean.setTeamId(teamId);
		eventSearchBean.setProviderId(providerId);
		eventSearchBean.setLocationId(locationId);
		eventSearchBean.setBaseEntityId(baseEntityId);
		eventSearchBean.setServerVersion(lastSyncedServerVersion);
		
		return getEventsAndClients(eventSearchBean, limit == null || limit.intValue() == 0 ? 25 : limit);
		
	}
	
	private EventSyncBean getEventsAndClients(EventSearchBean eventSearchBean, Integer limit) {
		Map<String, Object> response = new HashMap<String, Object>();
		List<Event> events = new ArrayList<Event>();
		List<String> clientIds = new ArrayList<String>();
		List<Client> clients = new ArrayList<Client>();
		long startTime = System.currentTimeMillis();
		events = eventService.findEvents(eventSearchBean, BaseEntity.SERVER_VERSIOIN, "asc", limit == null ? 25 : limit);
		logger.info("fetching events took: " + (System.currentTimeMillis() - startTime));
		if (!events.isEmpty()) {
			for (Event event : events) {
				if (org.apache.commons.lang.StringUtils.isNotBlank(event.getBaseEntityId())
				        && !clientIds.contains(event.getBaseEntityId())) {
					clientIds.add(event.getBaseEntityId());
				}
			}
			for (int i = 0; i < clientIds.size(); i = i + CLIENTS_FETCH_BATCH_SIZE) {
				int end = i + CLIENTS_FETCH_BATCH_SIZE < clientIds.size() ? i + CLIENTS_FETCH_BATCH_SIZE : clientIds.size();
				clients.addAll(clientService.findByFieldValue(BASE_ENTITY_ID, clientIds.subList(i, end)));
			}
			logger.info("fetching clients took: " + (System.currentTimeMillis() - startTime));
			
			searchMissingClients(clientIds, clients, startTime);
		}
		
		EventSyncBean eventSyncBean = new EventSyncBean();
		eventSyncBean.setClients(clients);
		eventSyncBean.setEvents(events);
		eventSyncBean.setNoOfEvents(events.size());
		return eventSyncBean;
	}
	
	private void searchMissingClients(List<String> clientIds, List<Client> clients, long startTime) {
		if (searchMissingClients) {
			
			List<String> foundClientIds = new ArrayList<>();
			for (Client client : clients) {
				foundClientIds.add(client.getBaseEntityId());
			}
			
			boolean removed = clientIds.removeAll(foundClientIds);
			if (removed) {
				for (String clientId : clientIds) {
					Client client = clientService.getByBaseEntityId(clientId);
					if (client != null) {
						clients.add(client);
					}
				}
			}
			logger.info("fetching missing clients took: " + (System.currentTimeMillis() - startTime));
		}
	}
	
	/**
	 * Fetch events ordered by serverVersion ascending order and return the clients associated with
	 * the events
	 * 
	 * @param request
	 * @return a map response with events, clients and optionally msg when an error occurs
	 */
	@RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	protected ResponseEntity<EventSyncBean> getAll(@RequestParam long serverVersion,
	        @RequestParam(required = false) String eventType, @RequestParam(required = false) Integer limit) {
		
		try {
			EventSearchBean eventSearchBean = new EventSearchBean();
			eventSearchBean.setServerVersion(serverVersion > 0 ? serverVersion + 1 : serverVersion);
			eventSearchBean.setEventType(eventType);
			return new ResponseEntity<>(getEventsAndClients(eventSearchBean, limit == null ? 25 : limit),
			        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
			
		}
		catch (Exception e) {
			EventSyncBean response = new EventSyncBean();
			response.setMsg("Error occurred");
			logger.error("", e);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(headers = { "Accept=application/json" }, method = POST, value = "/add")
	public ResponseEntity<HttpStatus> save(@RequestBody String data) {
		try {
			JSONObject syncData = new JSONObject(data);
			if (!syncData.has("clients") && !syncData.has("events")) {
				return new ResponseEntity<>(BAD_REQUEST);
			}
			
			if (syncData.has("clients")) {
				
				ArrayList<Client> clients = (ArrayList<Client>) gson.fromJson(syncData.getString("clients"),
				    new TypeToken<ArrayList<Client>>() {}.getType());
				for (Client client : clients) {
					try {
						clientService.addorUpdate(client);
					}
					catch (Exception e) {
						logger.error(
						    "Client" + client.getBaseEntityId() == null ? "" : client.getBaseEntityId() + " failed to sync",
						    e);
					}
				}
				
			}
			if (syncData.has("events")) {
				ArrayList<Event> events = (ArrayList<Event>) gson.fromJson(syncData.getString("events"),
				    new TypeToken<ArrayList<Event>>() {}.getType());
				for (Event event : events) {
					try {
						event = eventService.processOutOfArea(event);
						eventService.addorUpdateEvent(event);
					}
					catch (Exception e) {
						logger.error(
						    "Event of type " + event.getEventType() + " for client " + event.getBaseEntityId() == null ? ""
						            : event.getBaseEntityId() + " failed to sync",
						    e);
					}
				}
			}
			
		}
		catch (
		
		Exception e) {
			logger.error(format("Sync data processing failed with exception {0}.- ", e));
			return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(CREATED);
	}
	
	/*
	 * @RequestMapping(method=RequestMethod.GET)
	 * 
	 * @ResponseBody public Event getByBaseEntityAndFormSubmissionId(@RequestParam
	 * String baseEntityId, @RequestParam String formSubmissionId) { return
	 * eventService.getByBaseEntityAndFormSubmissionId(baseEntityId,
	 * formSubmissionId); }
	 */
	
	@Override
	public Event create(Event o) {
		return eventService.addEvent(o);
	}
	
	@Override
	public List<String> requiredProperties() {
		List<String> p = new ArrayList<>();
		p.add(BASE_ENTITY_ID);
		// p.add(FORM_SUBMISSION_ID);
		p.add(EVENT_TYPE);
		// p.add(LOCATION_ID);
		// p.add(EVENT_DATE);
		p.add(PROVIDER_ID);
		// p.add(ENTITY_TYPE);
		return p;
	}
	
	@Override
	public Event update(Event entity) {
		return eventService.mergeEvent(entity);
	}
	
	public static void main(String[] args) {
		
	}
	
	@Override
	public List<Event> search(HttpServletRequest request) throws ParseException {
		String clientId = getStringFilter("identifier", request);
		DateTime[] eventDate = getDateRangeFilter(EVENT_DATE, request);// TODO
		String eventType = getStringFilter(EVENT_TYPE, request);
		String location = getStringFilter(LOCATION_ID, request);
		String provider = getStringFilter(PROVIDER_ID, request);
		String entityType = getStringFilter(ENTITY_TYPE, request);
		DateTime[] lastEdit = getDateRangeFilter(LAST_UPDATE, request);
		String team = getStringFilter(TEAM, request);
		String teamId = getStringFilter(TEAM_ID, request);
		
		if (!StringUtils.isBlank(clientId)) {
			Client c = clientService.find(clientId);
			if (c == null) {
				return new ArrayList<>();
			}
			
			clientId = c.getBaseEntityId();
		}
		EventSearchBean eventSearchBean = new EventSearchBean();
		eventSearchBean.setBaseEntityId(clientId);
		eventSearchBean.setEventDateFrom(eventDate == null ? null : eventDate[0]);
		eventSearchBean.setEventDateTo(eventDate == null ? null : eventDate[1]);
		eventSearchBean.setEventType(eventType);
		eventSearchBean.setEntityType(entityType);
		eventSearchBean.setProviderId(provider);
		eventSearchBean.setLocationId(location);
		eventSearchBean.setLastEditFrom(lastEdit == null ? null : lastEdit[0]);
		eventSearchBean.setLastEditTo(lastEdit == null ? null : lastEdit[1]);
		eventSearchBean.setTeam(team);
		eventSearchBean.setTeamId(teamId);
		
		return eventService.findEventsBy(eventSearchBean);
	}
	
	@Override
	public List<Event> filter(String query) {
		return eventService.findEventsByDynamicQuery(query);
	}
	
	/**
	 * Fetch events ids filtered by eventType
	 *
	 * @param eventType
	 * @return A list of event ids
	 */
	@RequestMapping(value = "/findIdsByEventType", method = RequestMethod.GET, produces = {
	        MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	protected ResponseEntity<List<String>> getAllIdsByEventType(
	        @RequestParam(value = EVENT_TYPE, required = false) String eventType,
	        @RequestParam(value = DATE_DELETED, required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date dateDeleted) {
		
		try {
			
			List<String> eventIds = eventService.findAllIdsByEventType(eventType, dateDeleted);
			return new ResponseEntity<>(eventIds, RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
			
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
		}
	}
	
	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}
	
	public void setClientService(ClientService clientService) {
		this.clientService = clientService;
	}
	
}
