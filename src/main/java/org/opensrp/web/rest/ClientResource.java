package org.opensrp.web.rest;

import static org.opensrp.common.AllConstants.BaseEntity.ADDRESS_TYPE;
import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.BaseEntity.CITY_VILLAGE;
import static org.opensrp.common.AllConstants.BaseEntity.COUNTRY;
import static org.opensrp.common.AllConstants.BaseEntity.COUNTY_DISTRICT;
import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.BaseEntity.STATE_PROVINCE;
import static org.opensrp.common.AllConstants.BaseEntity.SUB_DISTRICT;
import static org.opensrp.common.AllConstants.BaseEntity.SUB_TOWN;
import static org.opensrp.common.AllConstants.BaseEntity.TOWN;
import static org.opensrp.common.AllConstants.Client.BIRTH_DATE;
import static org.opensrp.common.AllConstants.Client.CLIENTTYPE;
import static org.opensrp.common.AllConstants.Client.DEATH_DATE;
import static org.opensrp.common.AllConstants.Client.FIRST_NAME;
import static org.opensrp.common.AllConstants.Client.GENDER;
import static org.opensrp.common.AllConstants.Client.ORDERBYFIELDNAAME;
import static org.opensrp.common.AllConstants.Client.ORDERBYTYPE;
import static org.opensrp.common.AllConstants.Client.PAGENUMBER;
import static org.opensrp.common.AllConstants.Client.PAGESIZE;
import static org.opensrp.common.AllConstants.Client.PROVIDERID;
import static org.opensrp.common.AllConstants.Client.SEARCHTEXT;
import static org.opensrp.common.AllConstants.Client.HOUSEHOLD;
import static org.opensrp.common.AllConstants.Client.HOUSEHOLDMEMEBR;
import static org.opensrp.web.rest.RestUtils.getDateRangeFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.opensrp.domain.Event;
import org.opensrp.domain.Client;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.util.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mysql.jdbc.StringUtils;

@Controller
@RequestMapping(value = "/rest/client")
public class ClientResource extends RestResource<Client> {
	
	private ClientService clientService;
	
	private EventService eventService;
	
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Autowired
	public ClientResource(ClientService clientService, EventService eventService) {
		this.clientService = clientService;
		this.eventService = eventService;
	}
	
	@Override
	public Client getByUniqueId(String uniqueId) {
		return clientService.find(uniqueId);
	}
	
	@Override
	public Client create(Client o) {
		return clientService.addClient(o);
	}
	
	@Override
	public List<String> requiredProperties() {
		List<String> p = new ArrayList<>();
		p.add(FIRST_NAME);
		p.add(BIRTH_DATE);
		p.add(GENDER);
		p.add(BASE_ENTITY_ID);
		return p;
	}
	
	@Override
	public Client update(Client entity) {//TODO check if send property and id matches
		return clientService.mergeClient(entity);//TODO update should only be based on baseEntityId
	}
	
	@Override
	public List<Client> search(HttpServletRequest request) throws ParseException {//TODO search should not call different url but only add params
		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setNameLike(getStringFilter("name", request));
		searchBean.setGender(getStringFilter(GENDER, request));
		DateTime[] birthdate = getDateRangeFilter(BIRTH_DATE, request);//TODO add ranges like fhir do http://hl7.org/fhir/search.html
		DateTime[] deathdate = getDateRangeFilter(DEATH_DATE, request);
		String clientId = getStringFilter("identifier", request);
		if (!StringUtils.isEmptyOrWhitespaceOnly(clientId)) {
			Client c = clientService.find(clientId);
			List<Client> clients = new ArrayList<Client>();
			clients.add(c);
			return clients;
		}
		
		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}
		if (deathdate != null) {
			searchBean.setDeathdateFrom(deathdate[0]);
			searchBean.setDeathdateTo(deathdate[1]);
		}
		
		AddressSearchBean addressSearchBean = new AddressSearchBean();
		addressSearchBean.setAddressType(getStringFilter(ADDRESS_TYPE, request));
		addressSearchBean.setCountry(getStringFilter(COUNTRY, request));
		addressSearchBean.setStateProvince(getStringFilter(STATE_PROVINCE, request));
		addressSearchBean.setCityVillage(getStringFilter(CITY_VILLAGE, request));
		addressSearchBean.setCountyDistrict(getStringFilter(COUNTY_DISTRICT, request));
		addressSearchBean.setSubDistrict(getStringFilter(SUB_DISTRICT, request));
		addressSearchBean.setTown(getStringFilter(TOWN, request));
		addressSearchBean.setSubTown(getStringFilter(SUB_TOWN, request));
		DateTime[] lastEdit = getDateRangeFilter(LAST_UPDATE, request);//TODO client by provider id
		//TODO lookinto Swagger https://slack-files.com/files-pri-safe/T0EPSEJE9-F0TBD0N77/integratingswagger.pdf?c=1458211183-179d2bfd2e974585c5038fba15a86bf83097810a
		String attributes = getStringFilter("attribute", request);
		searchBean.setAttributeType(StringUtils.isEmptyOrWhitespaceOnly(attributes) ? null : attributes.split(":", -1)[0]);
		searchBean.setAttributeValue(StringUtils.isEmptyOrWhitespaceOnly(attributes) ? null : attributes.split(":", -1)[1]);
		
		return clientService.findByCriteria(searchBean, addressSearchBean, lastEdit == null ? null : lastEdit[0],
		    lastEdit == null ? null : lastEdit[1]);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/searchByCriteria", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<String> searchByCriteria(HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		JsonArray clientsArray = new JsonArray();
		List<Client> clientList = new ArrayList<Client>();
		List<Client> clients = new ArrayList<Client>();
		int total = 0;
		Integer pageNumber = 0;
		Integer pageSize = 0;
		String baseEntityId = getStringFilter(BASE_ENTITY_ID, request);
		String pageNumberParam = getStringFilter(PAGENUMBER, request);
		String pageSizeParam = getStringFilter(PAGESIZE, request);
		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setNameLike(getStringFilter(SEARCHTEXT, request));
		searchBean.setGender(getStringFilter(GENDER, request));
		
		DateTime[] lastEdit = null;
		
		String clientType = getStringFilter(CLIENTTYPE, request);
		searchBean.setOrderByField(getStringFilter(ORDERBYFIELDNAAME, request));
		searchBean.setOrderByType(getStringFilter(ORDERBYTYPE, request));
		searchBean.setClientType(clientType);
		searchBean.setProviderId(getStringFilter(PROVIDERID, request));
		
		if (pageNumberParam != null) {
			pageNumber = Integer.parseInt(pageNumberParam);
		}
		if (pageSizeParam != null) {
			pageSize = Integer.parseInt(pageSizeParam);
		}
		
		List<String> locationUuids = new ArrayList<String>();
		locationUuids.add("56b112d2-21ce-4818-b603-277bb57f5528");
		locationUuids.add("1902417a-d9f4-44f5-adc4-49a55f0eab0d");
		searchBean.setLocations(locationUuids);
		searchBean.setPageNumber(pageNumber);
		searchBean.setPageSize(pageSize);
		AddressSearchBean addressSearchBean = new AddressSearchBean();
		
		String attributes = getStringFilter("attribute", request);
		searchBean.setAttributeType(StringUtils.isEmptyOrWhitespaceOnly(attributes) ? null : attributes.split(":", -1)[0]);
		searchBean.setAttributeValue(StringUtils.isEmptyOrWhitespaceOnly(attributes) ? null : attributes.split(":", -1)[1]);
		List<String> ids = new ArrayList<String>();
		if (clientType.equalsIgnoreCase(HOUSEHOLD)) {
			
			clients = clientService.findByCriteria(searchBean, addressSearchBean, lastEdit == null ? null : lastEdit[0],
			    lastEdit == null ? null : lastEdit[1]);
			if (clients.size() != 0) {
				for (Client client : clients) {
					ids.add(client.getBaseEntityId());
				}
				
				total = clientService.findTotalCountByCriteria(searchBean, addressSearchBean).getTotalCount();
				clientList = clientService.getHouseholdList(ids, clientType, addressSearchBean, searchBean, clients);
			}
			
		} else if (clientType.equalsIgnoreCase(HOUSEHOLDMEMEBR)) {
			clientList = clientService.findMembersByRelationshipId(baseEntityId);
		}
		clientsArray = (JsonArray) gson.toJsonTree(clientList, new TypeToken<List<Client>>() {}.getType());
		response.put("clients", clientsArray);
		response.put("total", total);
		return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.OK);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/get-client-and-events", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<String> getClientAndEventsBybaseEntityId(HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		JsonObject clientsArray = new JsonObject();
		JsonArray eventsArray = new JsonArray();
		List<Event> events = new ArrayList<Event>();
		
		String baseEntityId = getStringFilter(BASE_ENTITY_ID, request);
		if (org.apache.commons.lang3.StringUtils.isBlank(baseEntityId)) {
			response.put("client", clientsArray);
			response.put("events", eventsArray);
			return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.OK);
		}
		
		Client client = clientService.find(baseEntityId);
		if (client != null) {
			clientsArray = (JsonObject) gson.toJsonTree(client, new TypeToken<Client>() {}.getType());
		}
		events = eventService.findByBaseEntityId(baseEntityId);
		if (events.size() != 0) {
			eventsArray = (JsonArray) gson.toJsonTree(events, new TypeToken<List<Event>>() {}.getType());
		}
		
		response.put("events", eventsArray);
		response.put("clients", clientsArray);
		return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.OK);
		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/members", produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseEntity<String> getMembersByRelationshipIdAndType(HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		JsonArray clientsArray = new JsonArray();
		JsonArray eventsArray = new JsonArray();
		String baseEntityId = getStringFilter(BASE_ENTITY_ID, request);
		String relationshipType = getStringFilter("relationshipType", request);
		if (org.apache.commons.lang3.StringUtils.isBlank(baseEntityId)
		        && org.apache.commons.lang3.StringUtils.isBlank(relationshipType)) {
			response.put("client", clientsArray);
			response.put("events", eventsArray);
			return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.OK);
		}
		List<Client> clients = clientService.findByRelationshipIdAndType(relationshipType, baseEntityId);
		if (!clients.isEmpty()) {
			clientsArray = (JsonArray) gson.toJsonTree(clients, new TypeToken<List<Client>>() {}.getType());
		}
		
		response.put("clients", clientsArray);
		
		return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.OK);
	}
	
	@Override
	public List<Client> filter(String query) {
		return clientService.findByDynamicQuery(query);
	}
	
}
