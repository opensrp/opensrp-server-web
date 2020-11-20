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

import java.text.ParseException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.opensrp.web.utils.Utils;
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
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;

import static org.opensrp.common.AllConstants.BaseEntity.SERVER_VERSIOIN;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.opensrp.web.Constants.DEFAULT_LIMIT;
import static org.opensrp.web.rest.RestUtils.getDateFilter;
import static org.opensrp.web.rest.RestUtils.getDateRangeFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

@Controller
@RequestMapping(value = "/rest/client")
public class ClientResource extends RestResource<Client> {

	private static final Logger logger = LoggerFactory.getLogger(ClientResource.class.toString());

	private final ClientService clientService;

	public static final String RELATIONSHIPS = "relationships";

	public static final String SEARCH_RELATIONSHIP = "searchRelationship";

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
		List<Client> clients = new ArrayList<>();
		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setNameLike(getStringFilter("name", request));
		searchBean.setGender(getStringFilter(GENDER, request));
		addSearchDateFilters(request, searchBean);

		String clientId = getStringFilter("identifier", request);
		if (!StringUtils.isBlank(clientId)) {
			Client c = clientService.find(clientId);
			clients.add(c);
			return clients;
		}

		AddressSearchBean addressSearchBean = getAddressSearchBean(request);
		DateTime[] lastEdit = getDateRangeFilter(LAST_UPDATE, request);//TODO client by provider id
		//TODO lookinto Swagger https://slack-files.com/files-pri-safe/T0EPSEJE9-F0TBD0N77/integratingswagger.pdf?c=1458211183-179d2bfd2e974585c5038fba15a86bf83097810a
		String attributes = getStringFilter("attribute", request);
		searchBean.setAttributeType(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[0]);
		searchBean.setAttributeValue(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[1]);

		String locationIds = getStringFilter("locationIds", request);
		List<String> locations = StringUtils.isBlank(locationIds) ? new ArrayList<>() :
				Arrays.asList(locationIds.split(","));
		if (locations.size() != 0) {
			searchBean.setLocations(locations);
		}
		clients = clientService.findByCriteria(searchBean, addressSearchBean, lastEdit == null ? null : lastEdit[0],
				lastEdit == null ? null : lastEdit[1]);

		String searchRelationship = getStringFilter(SEARCH_RELATIONSHIP, request);
		logger.info("Search relationship: " + searchRelationship);
		if (StringUtils.isBlank(searchRelationship)) {
			String relationships = getStringFilter(RELATIONSHIPS, request);
			List<String> relationshipTypes = StringUtils.isBlank(relationships) ?
					new ArrayList<>() :
					Arrays.asList(relationships.split(","));
			logger.info("Relationship types: " + relationshipTypes.toString());
			if (!relationshipTypes.isEmpty()) {
				List<Client> clientRelationships = getRelationships(clients, relationshipTypes);
				clients.addAll(clientRelationships);
			}
		} else {
			List<Client> dependants = getDependants(clients, searchRelationship);
			clients.addAll(dependants);
		}

		return clients;
	}

	private void addSearchDateFilters(HttpServletRequest request, ClientSearchBean searchBean) throws ParseException {
		DateTime[] birthDate = getDateRangeFilter(BIRTH_DATE,
				request);//TODO add ranges like fhir do http://hl7.org/fhir/search.html
		DateTime[] deathDate = getDateRangeFilter(DEATH_DATE, request);
		if (birthDate != null) {
			searchBean.setBirthdateFrom(birthDate[0]);
			searchBean.setBirthdateTo(birthDate[1]);
		}
		if (deathDate != null) {
			searchBean.setDeathdateFrom(deathDate[0]);
			searchBean.setDeathdateTo(deathDate[1]);
		}
	}

	private AddressSearchBean getAddressSearchBean(HttpServletRequest request) {
		AddressSearchBean addressSearchBean = new AddressSearchBean();
		addressSearchBean.setAddressType(getStringFilter(ADDRESS_TYPE, request));
		addressSearchBean.setCountry(getStringFilter(COUNTRY, request));
		addressSearchBean.setStateProvince(getStringFilter(STATE_PROVINCE, request));
		addressSearchBean.setCityVillage(getStringFilter(CITY_VILLAGE, request));
		addressSearchBean.setCountyDistrict(getStringFilter(COUNTY_DISTRICT, request));
		addressSearchBean.setSubDistrict(getStringFilter(SUB_DISTRICT, request));
		addressSearchBean.setTown(getStringFilter(TOWN, request));
		addressSearchBean.setSubTown(getStringFilter(SUB_TOWN, request));
		return addressSearchBean;
	}

	/**
	 * This method is used to get all the dependants for the the provided clients. Example when you you want to return all
	 * the children for the list of mothers that you provided. The query expects each of the clients to at least have
	 * one dependant otherwise we are not fetching the correct dependants hence the client will be removed from the final clients list
	 *
	 * @param clients            List of clients to fetch their dependants
	 * @param searchRelationship The type of relationship used for querying e.g. "mother" or "father" as defined in the "relationships" attribute
	 *                           of the dependant.
	 * @return A list of dependants belonging to the clients
	 */
	private List<Client> getDependants(List<Client> clients, String searchRelationship) {
		List<Client> dependantClients = new ArrayList<>();
		List<Client> clientsToRemove = new ArrayList<>();
		for (Client client : clients) {
			List<Client> dependants = clientService.findByRelationshipIdAndType(searchRelationship, client.getBaseEntityId());
			if (dependants.size() > 0) {
				dependantClients.addAll(dependants);
			} else {
				clientsToRemove.add(client);
			}
		}
		clients.removeIf(clientsToRemove::contains);
		return dependantClients;
	}

	/**
	 * Get all the relationship for the provided clients. For example when you have a list of children and you want to get all their mothers and
	 * father. The objects for the related clients will be included in the final list returned by the query.
	 *
	 * @param clients           list of clients which you want to use to fetch their relations
	 * @param relationshipTypes the type of relationships you want to include in the final list
	 * @return a list client relationship objects
	 */
	private List<Client> getRelationships(List<Client> clients, List<String> relationshipTypes) {
		List<Client> relationshipClients = new ArrayList<>();
		HashSet<String> fetchedRelationships = new HashSet<>();
		for (Client client : clients) {
			if (client.getRelationships() != null) {
				for (Map.Entry<String, List<String>> relationshipEntry : client.getRelationships().entrySet())
					if (relationshipTypes.contains(relationshipEntry.getKey())) {
						relationshipEntry.getValue().forEach(relationalId -> {
									if (!fetchedRelationships.contains(relationalId)) {
										Client clientRelative = clientService.find(relationalId);
										if (!clients.contains(clientRelative))
											relationshipClients.add(clientRelative);
										fetchedRelationships.add(relationalId);
									}
								}
						);
					}
			}
		}
		return relationshipClients;
	}

	@Override
	public List<Client> filter(String query) {
		return clientService.findByDynamicQuery(query);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/searchByCriteria", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> searchByCriteria(HttpServletRequest request) throws JsonProcessingException,
			ParseException {

		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setNameLike(getStringFilter(SEARCHTEXT, request));
		searchBean.setGender(getStringFilter(GENDER, request));
		DateTime[] lastEdit = getDateRangeFilter(LAST_UPDATE, request);
		searchBean.setLastEditFrom(lastEdit == null ? null : lastEdit[0]);
		searchBean.setLastEditTo(lastEdit == null ? null : lastEdit[1]);
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

		setPageNumberAndSize(request, searchBean);

		String attributes = getStringFilter("attribute", request);
		searchBean.setAttributeType(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[0]);
		searchBean.setAttributeValue(StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[1]);

		try {
			DateTime startDate = getDateFilter(STARTDATE, request);
			searchBean.setStartDate(startDate);
			DateTime endDate = getDateFilter(ENDDATE, request);
			searchBean.setEndDate(endDate);
		} catch (ParseException e) {
			logger.error(e.getMessage());
		}

		AddressSearchBean addressSearchBean = new AddressSearchBean();
		ClientSyncBean response = new ClientSyncBean();
		if (HOUSEHOLD.equalsIgnoreCase(clientType)) {
			return getHouseholds(searchBean, addressSearchBean);
		} else if (HOUSEHOLDMEMEBR.equalsIgnoreCase(clientType)) {
			response.setClients(clientService.findMembersByRelationshipId(getStringFilter(BASE_ENTITY_ID, request)));
		} else if (ALLCLIENTS.equalsIgnoreCase(clientType)) {
			searchBean.setClientType(HOUSEHOLD);
			return getAllClients(searchBean, addressSearchBean);
		} else if (ANC.equalsIgnoreCase(clientType)) {
			return getAllANC(searchBean, addressSearchBean);
		} else if (CHILD.equalsIgnoreCase(clientType)) {
			return getAllChild(searchBean, addressSearchBean);
		} else {
			logger.info("no matched client type");
		}

		return new ResponseEntity<>(objectMapper.writeValueAsString((response)), HttpStatus.OK);
	}

	private void setPageNumberAndSize(HttpServletRequest request, ClientSearchBean searchBean) {
		int pageNumber = 1; // default page number
		int pageSize = 0; // default page size
		String pageNumberParam = getStringFilter(PAGE_NUMBER, request);
		String pageSizeParam = getStringFilter(PAGE_SIZE, request);
		if (pageNumberParam != null) {
			pageNumber = Integer.parseInt(pageNumberParam) - 1;
		}
		if (pageSizeParam != null) {
			pageSize = Integer.parseInt(pageSizeParam);
		}

		searchBean.setPageNumber(pageNumber);
		searchBean.setPageSize(pageSize);
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
		ClientSyncBean response = new ClientSyncBean();
		List<Client> clients = clientService.findHouseholdByCriteria(clientSearchBean, addressSearchBean,
				clientSearchBean.getLastEditFrom(), clientSearchBean.getLastEditTo());
		total = getTotal(clientSearchBean, addressSearchBean);
		response.setClients(clients);
		response.setTotal(total);
		return new ResponseEntity<>(objectMapper.writeValueAsString((response)), HttpStatus.OK);
	}

	private int getTotal(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean) {

		String clientType = clientSearchBean.getClientType();
		int pageNumber = clientSearchBean.getPageNumber();
		if (pageNumber == FIRST_PAGE) {
			if (HOUSEHOLD.equalsIgnoreCase(clientType)) {
				total = clientService.findTotalCountHouseholdByCriteria(clientSearchBean, addressSearchBean).getTotalCount();
			} else if (ALLCLIENTS.equalsIgnoreCase(clientType)) {
				total = clientService.findTotalCountAllClientsByCriteria(clientSearchBean, addressSearchBean).getTotalCount();
			} else if (ANC.equalsIgnoreCase(clientType)) {
				clientSearchBean.setClientType(null);
				total = clientService.findCountANCByCriteria(clientSearchBean, addressSearchBean);
			} else if (CHILD.equalsIgnoreCase(clientType)) {
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
	 * @param isArchived    whether a client has been archived
	 * @return A list of task Ids
	 */
	@RequestMapping(value = "/findIds", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Identifier> findIds(
			@RequestParam(value = SERVER_VERSIOIN)  long serverVersion,
			@RequestParam(value = IS_ARCHIVED, defaultValue = FALSE, required = false) boolean isArchived,
			@RequestParam(value = "fromDate", required = false) String fromDate,
			@RequestParam(value = "toDate", required = false) String toDate) {
		Pair<List<String>, Long> taskIdsPair = clientService.findAllIds(serverVersion, DEFAULT_GET_ALL_IDS_LIMIT, isArchived,
				Utils.getDateTimeFromString(fromDate), Utils.getDateTimeFromString(toDate));
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
	 * Fetch clients ordered by serverVersion ascending order
	 *
	 * @return a response with clients
	 */
	@GetMapping(value = "/countAll", produces = {MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<ModelMap> countAll(
			@RequestParam(value = SERVER_VERSIOIN)  long serverVersion){
		Long countOfClients = clientService.countAll(serverVersion);
		ModelMap modelMap = new ModelMap();
		modelMap.put("count", countOfClients != null ? countOfClients : 0);
		return ResponseEntity.ok(modelMap);
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
