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
import static org.opensrp.common.AllConstants.Client.PROVIDERID;
import static org.opensrp.common.AllConstants.Client.SEARCHTEXT;
import static org.opensrp.common.AllConstants.Event.LOCATION_ID;
import static org.opensrp.web.rest.RestUtils.getDateFilter;
import static org.opensrp.web.rest.RestUtils.getDateRangeFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.smartregister.domain.Client;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.web.bean.ClientSyncBean;
import org.opensrp.web.bean.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.opensrp.web.Constants.DEFAULT_LIMIT;

@Controller
@RequestMapping(value = "/rest/client")
public class ClientResource extends RestResource<Client> {
	
	private static Logger logger = LoggerFactory.getLogger(ClientResource.class.toString());
	
	private ClientService clientService;
	
	public static final String PAGE_SIZE = "pageSize";
	
	public static final String PAGE_NUMBER = "pageNumber";
	
	public static final String ALLCLIENTS = "clients";
	
	public static final String HOUSEHOLD = "ec_family";
	
	public static final String HOUSEHOLDMEMEBR = "householdMember";
	
	public static final int FIRST_PAGE = 0;
	
	public static final int NO_TOTAL_COUNT = 0;
	
	public int total = 0;
	
	public static final String ANC = "anc";
	
	public static final String CHILD = "child";
	
	public static final String STARTDATE = "startDate";
	
	public static final String ENDDATE = "endDate";
	
	private static final String IS_ARCHIVED = "is_archived";

	private static final String FALSE = "false";
	
	@Autowired
	public ClientResource(ClientService clientService) {
		this.clientService = clientService;
		
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
		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}
		if (deathdate != null) {
			searchBean.setDeathdateFrom(deathdate[0]);
			searchBean.setDeathdateTo(deathdate[1]);
		}
		
		String clientId = getStringFilter("identifier", request);
		if (!StringUtils.isBlank(clientId)) {
			Client c = clientService.find(clientId);
			List<Client> clients = new ArrayList<Client>();
			clients.add(c);
			return clients;
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
		searchBean.setAttributeType(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[0]);
		searchBean.setAttributeValue(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[1]);
		
		return clientService.findByCriteria(searchBean, addressSearchBean, lastEdit == null ? null : lastEdit[0],
		    lastEdit == null ? null : lastEdit[1]);
	}
	
	@Override
	public List<Client> filter(String query) {
		return clientService.findByDynamicQuery(query);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/searchByCriteria", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> searchByCriteria(HttpServletRequest request) throws JsonProcessingException {
		ClientSyncBean response = new ClientSyncBean();
		List<Client> clientList;
		
		String baseEntityId = getStringFilter(BASE_ENTITY_ID, request);
		String pageNumberParam = getStringFilter(PAGE_NUMBER, request);
		String pageSizeParam = getStringFilter(PAGE_SIZE, request);
		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setNameLike(getStringFilter(SEARCHTEXT, request));
		searchBean.setGender(getStringFilter(GENDER, request));
		Integer pageNumber = 1; // default page number
		Integer pageSize = 0; // default page size
		String clientType = getStringFilter(CLIENTTYPE, request);
		searchBean.setOrderByField(getStringFilter(ORDERBYFIELDNAAME, request));
		searchBean.setOrderByType(getStringFilter(ORDERBYTYPE, request));
		searchBean.setClientType(clientType);
		searchBean.setProviderId(getStringFilter(PROVIDERID, request));
		String locationId = getStringFilter(LOCATION_ID, request);
		
		if (locationId != null) {
			String[] locationIds = locationId.split(",");
			List<String> locationIdList = Arrays.asList(locationIds);
			searchBean.setLocations(locationIdList);
		}
		
		if (pageNumberParam != null) {
			pageNumber = Integer.parseInt(pageNumberParam) - 1;
		}
		if (pageSizeParam != null) {
			pageSize = Integer.parseInt(pageSizeParam);
		}
		
		searchBean.setPageNumber(pageNumber);
		searchBean.setPageSize(pageSize);
		AddressSearchBean addressSearchBean = new AddressSearchBean();
		
		String attributes = getStringFilter("attribute", request);
		searchBean.setAttributeType(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[0]);
		searchBean.setAttributeValue(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[1]);
		
		DateTime startDate = null;
		DateTime endDate = null;
		try {
			startDate = getDateFilter(STARTDATE, request);
			searchBean.setStartDate(startDate);
			endDate = getDateFilter(ENDDATE, request);
			searchBean.setEndDate(endDate);
		}
		catch (ParseException e) {
			logger.error(e.getMessage());
		}
		
		if (clientType.equalsIgnoreCase(HOUSEHOLD)) {
			return getHouseholds(searchBean, addressSearchBean);
		} else if (clientType.equalsIgnoreCase(HOUSEHOLDMEMEBR)) {
			
			clientList = clientService.findMembersByRelationshipId(baseEntityId);
			response.setClients(clientList);
			
		} else if (clientType.equalsIgnoreCase(ALLCLIENTS)) {
			searchBean.setClientType(HOUSEHOLD);
			return getAllClients(searchBean, addressSearchBean);
		}
		
		else if (clientType.equalsIgnoreCase(ANC)) {
			
			return getAllANC(searchBean, addressSearchBean);
		}
		
		else if (clientType.equalsIgnoreCase(CHILD)) {
			return getAllChild(searchBean, addressSearchBean);
		} else {
			logger.info("no matched client type");
		}
		
		return new ResponseEntity<>(objectMapper.writeValueAsString((response)), HttpStatus.OK);
		
	}
	
	public ResponseEntity<String> getAllClients(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean)
	        throws JsonProcessingException {
		
		ClientSyncBean response = new ClientSyncBean();
		
		List<Client> clients = clientService.findAllClientsByCriteria(clientSearchBean, addressSearchBean);
		
		total = getTotal(clientSearchBean, addressSearchBean);
		response.setClients(clients);
		response.setTotal(total);
		return new ResponseEntity<>(objectMapper.writeValueAsString((response)), HttpStatus.OK);
	}
	
	public ResponseEntity<String> getHouseholds(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean)
	        throws JsonProcessingException {
		
		DateTime[] lastEdit = null;
		ClientSyncBean response = new ClientSyncBean();
		List<Client> clients;
		
		clients = clientService.findHouseholdByCriteria(clientSearchBean, addressSearchBean,
		    lastEdit == null ? null : lastEdit[0], lastEdit == null ? null : lastEdit[1]);
		total = getTotal(clientSearchBean, addressSearchBean);
		response.setClients(clients);
		response.setTotal(total);
		return new ResponseEntity<>(objectMapper.writeValueAsString((response)), HttpStatus.OK);
	}
	
	private int getTotal(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean) {
		
		String clientType = clientSearchBean.getClientType();
		int pageNumber = clientSearchBean.getPageNumber();
		if (pageNumber == FIRST_PAGE) {
			if (clientType.equalsIgnoreCase(HOUSEHOLD)) {
				total = clientService.findTotalCountHouseholdByCriteria(clientSearchBean, addressSearchBean).getTotalCount();
			} else if (clientType.equalsIgnoreCase(ALLCLIENTS)) {
				total = clientService.findTotalCountAllClientsByCriteria(clientSearchBean, addressSearchBean)
				        .getTotalCount();
			} else if (clientType.equalsIgnoreCase(ANC)) {
				clientSearchBean.setClientType(null);
				total = clientService.findCountANCByCriteria(clientSearchBean, addressSearchBean);
			} else if (clientType.equalsIgnoreCase(CHILD)) {
				clientSearchBean.setClientType(null);
				total = clientService.findCountChildByCriteria(clientSearchBean, addressSearchBean);
			} else {
				total = NO_TOTAL_COUNT;
			}
		}
		return total;
	}
	
	public ResponseEntity<String> getAllANC(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean)
	        throws JsonProcessingException {
		
		ClientSyncBean response = new ClientSyncBean();
		clientSearchBean.setClientType(null);
		List<Client> clients = clientService.findAllANCByCriteria(clientSearchBean, addressSearchBean);
		clientSearchBean.setClientType(ANC);
		total = getTotal(clientSearchBean, addressSearchBean);
		response.setClients(clients);
		response.setTotal(total);
		return new ResponseEntity<>(objectMapper.writeValueAsString((response)), HttpStatus.OK);
	}
	
	public ResponseEntity<String> getAllChild(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean)
	        throws JsonProcessingException {
		
		ClientSyncBean response = new ClientSyncBean();
		clientSearchBean.setClientType(null);
		List<Client> clients = clientService.findAllChildByCriteria(clientSearchBean, addressSearchBean);
		clientSearchBean.setClientType(CHILD);
		total = getTotal(clientSearchBean, addressSearchBean);
		response.setClients(clients);
		response.setTotal(total);
		return new ResponseEntity<>(objectMapper.writeValueAsString((response)), HttpStatus.OK);
	}
	
	/**
	 * This methods provides an API endpoint that searches for all clients Ids
	 * ordered by server version ascending
	 *
	 * @param serverVersion serverVersion using to filter by
	 * @param isArchived whether a client has been archived
	 * @return A list of task Ids
	 */
	@RequestMapping(value = "/findIds", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Identifier> findIds(
			@RequestParam(value = SERVER_VERSIOIN)  long serverVersion,
			@RequestParam(value = IS_ARCHIVED, defaultValue = FALSE, required = false) boolean isArchived) {

		Pair<List<String>, Long> taskIdsPair = clientService.findAllIds(serverVersion, DEFAULT_GET_ALL_IDS_LIMIT, isArchived);
		Identifier identifiers = new Identifier();
		identifiers.setIdentifiers(taskIdsPair.getLeft());
		identifiers.setLastServerVersion(taskIdsPair.getRight());
		return new ResponseEntity<>(identifiers, HttpStatus.OK);
	}
	
	/**
	 * Fetch clients ordered by serverVersion ascending order
	 *
	 * @return a response with clients
	 */
	@GetMapping(value = "/getAll", produces = {MediaType.APPLICATION_JSON_VALUE })
	public List<Client> getAll(
			@RequestParam(value = SERVER_VERSIOIN)  long serverVersion,
			@RequestParam(required = false, defaultValue = DEFAULT_LIMIT + "") int limit){

		return clientService.findByServerVersion(serverVersion, limit);
	}

	/**
	 * Get client using the client id
	 *
	 * @param clientId the event id
	 * @return client with the client id
	 */
	@GetMapping(value = "/findById", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Client getById(@RequestParam("id") String clientId) {
		return clientService.findById(clientId);
	}
	
}
