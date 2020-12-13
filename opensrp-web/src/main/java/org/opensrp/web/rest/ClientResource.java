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
import static org.opensrp.common.AllConstants.Client.DEATH_DATE;
import static org.opensrp.common.AllConstants.Client.FIRST_NAME;
import static org.opensrp.common.AllConstants.Client.GENDER;
import static org.opensrp.web.rest.RestUtils.getDateRangeFilter;
import static org.opensrp.web.rest.RestUtils.getIntegerFilter;
import static org.opensrp.web.rest.RestUtils.getStringFilter;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.Client;
import org.opensrp.domain.DataApprove;
import org.opensrp.domain.UserLocationTableName;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.util.DateTimeTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mysql.jdbc.StringUtils;

@Controller
@RequestMapping(value = "/rest/client")
public class ClientResource extends RestResource<Client> {
	
	private ClientService clientService;
	
	private EventService eventService;
	
	Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
	        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();
	
	@Autowired
	public ClientResource(ClientService clientService, EventService eventService) {
		this.clientService = clientService;
		this.eventService = eventService;
	}
	
	@Override
	public Client getByUniqueId(String uniqueId, String district, String username) {
		UserLocationTableName userLocation = clientService.getUserLocationAndTable(username, district, "", "", "");
		String table = userLocation.getTableName();
		return clientService.find(uniqueId, table);
	}
	
	@Override
	public Client create(Client o, String district, String division, String branch, String village, String username) {
		UserLocationTableName userLocation = clientService.getUserLocationAndTable(username, district, division, branch,
		    village);
		String table = userLocation.getTableName();
		return clientService.addClient(o, table, userLocation.getDistrict(), userLocation.getDivision(),
		    userLocation.getBranch(), userLocation.getVillage());
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
	public Client update(Client entity, String district, String division, String branch, String village, String username) {//TODO check if send property and id matches
	
		UserLocationTableName userLocation = clientService.getUserLocationAndTable(username, district, division, branch,
		    village);
		String table = userLocation.getTableName();
		return clientService.mergeClient(entity, null, table, userLocation.getDistrict(), userLocation.getDivision(),
		    userLocation.getBranch(), userLocation.getVillage());//TODO update should only be based on baseEntityId
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
		String district = getStringFilter("district", request);
		String username = request.getRemoteUser();
		UserLocationTableName userLocation = clientService.getUserLocationAndTable(username, district, "", "", "");
		String table = userLocation.getTableName();
		return clientService.findByCriteria(searchBean, addressSearchBean, lastEdit == null ? null : lastEdit[0],
		    lastEdit == null ? null : lastEdit[1], table);
	}
	
	@Override
	public List<Client> filter(String query, String district, String username) {
		
		return clientService.findByDynamicQuery(query);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, method = POST, value = "/data-approval")
	public ResponseEntity<String> dataApprove(@RequestBody String requestData, HttpServletRequest request)
	    throws JSONException {
		JSONObject jsonObj = new JSONObject();
		try {
			String district = getStringFilter("district", request);
			
			String division = getStringFilter("division", request);
			String branch = getStringFilter("branch", request);
			String village = getStringFilter("village", request);
			String username = request.getRemoteUser();
			
			UserLocationTableName userLocation = clientService.getUserLocationAndTable(username, district, division, branch,
			    village);
			String table = userLocation.getTableName();
			boolean isApproved = true;
			Gson jsonObject = new Gson();
			DataApprove approvalData = jsonObject.fromJson(requestData, DataApprove.class);
			String baseEntityId = approvalData.getBaseEntityId();
			String comments = approvalData.getComment();
			String status = approvalData.getStatus();
			Client client = clientService.find(baseEntityId, table);
			client.withDataApprovalComments(comments);
			client.withDataApprovalStatus(status);
			if (status.equalsIgnoreCase(AllConstants.APPROVED)) {
				isApproved = false;
			}
			clientService.addOrUpdate(client, isApproved, table, userLocation.getDistrict(), userLocation.getDivision(),
			    userLocation.getBranch(), userLocation.getVillage());
			
			/*List<Event> getEvents = eventService.findByBaseEntityAndEventTypeContaining(baseEntityId, "Registration");
			if (getEvents.size() != 0) {
				Event getEvent = getEvents.get(0);
				getEvent.withIsSendToOpenMRS("no");
				eventService.addorUpdateEvent(getEvent);
			}*/
		}
		catch (JsonSyntaxException e) {
			jsonObj.put("msg", INTERNAL_SERVER_ERROR);
			return new ResponseEntity<>(jsonObj.toString(), HttpStatus.OK);
		}
		jsonObj.put("msg", CREATED);
		return new ResponseEntity<>(jsonObj.toString(), HttpStatus.OK);
		
	}
	
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, method = POST, value = "/update-union")
	public ResponseEntity<String> updateClientByUpazila(@RequestParam("changeAbleField") String changeAbleField,
	                                                    @RequestParam("currentName") String currentName,
	                                                    @RequestParam("rightName") String rightName,
	                                                    @RequestParam("district") String district) throws JSONException {
		
		List<Client> clients = clientService.findAllClientByUpazila(currentName, district);
		
		System.out.println("TOTAL SIZE:->");
		System.out.println(clients.size());
		
		try {
			for (Client client : clients) {
				Map<String, String> addressFields = client.getAddresses().get(0).getAddressFields();
				addressFields.put(changeAbleField, rightName);
				client.getAddresses().get(0).setAddressFields(addressFields);
				clientService.updateClient(client, district, "", "", "", "");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<>("done", HttpStatus.OK);
	}
	
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, value = "/is_resync", method = RequestMethod.GET)
	@ResponseBody
	protected ResponseEntity<String> clientListToDeleteFromAPP(@RequestParam("username") String username)
	    throws JSONException {
		
		try {
			
			String is_resync = clientService.getIsResync(username);
			
			return new ResponseEntity<>(is_resync, HttpStatus.OK);
		}
		catch (Exception e) {
			
			return new ResponseEntity<>("", HttpStatus.OK);
		}
	}
	
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, value = "/search-client", method = RequestMethod.GET)
	@ResponseBody
	protected ResponseEntity<String> searchClient(HttpServletRequest request) {
		Map<String, Object> response = new HashMap<String, Object>();
		try {
			Integer villageId = getIntegerFilter("villageId", request);
			String gender = getStringFilter("gender", request);
			Integer startAge = getIntegerFilter("startAge", request);
			Integer endAge = getIntegerFilter("endAge", request);
			String type = getStringFilter("type", request);
			
			List<Client> clients = clientService.searchClient(villageId, gender, startAge, endAge, type);
			
			JsonArray clientsArray = (JsonArray) gson.toJsonTree(clients, new TypeToken<List<Client>>() {}.getType());
			response.put("clients", clientsArray);
			return new ResponseEntity<>(gson.toJson(response), HttpStatus.OK);
		}
		catch (Exception e) {
			response.put("msg", "Error occurred");
			return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		
	}
	
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, value = "/migrated", method = RequestMethod.GET)
	@ResponseBody
	public List<String> getStockInfo(@RequestParam("username") String provider, @RequestParam("type") String type,
	                                 @RequestParam("timestamp") Long timestamp) {
		
		return clientService.getMigratedList(provider, type, timestamp + 1);
	}
}
