package org.opensrp.web.rest;

import static org.opensrp.common.AllConstants.BaseEntity.*;
import static org.opensrp.common.AllConstants.Client.BIRTH_DATE;
import static org.opensrp.common.AllConstants.Client.DEATH_DATE;
import static org.opensrp.common.AllConstants.Client.FIRST_NAME;
import static org.opensrp.common.AllConstants.Client.GENDER;
import static org.opensrp.common.AllConstants.Client.PROVIDERID;
import static org.opensrp.common.AllConstants.Client.CLIENTTYPE;
import static org.opensrp.common.AllConstants.Client.PAGENUMBER;
import static org.opensrp.common.AllConstants.Client.PAGESIZE;
import static org.opensrp.common.AllConstants.Client.ORDERBYFIELD;
import static org.opensrp.common.AllConstants.Client.ORDERBYTYPE;

import static org.opensrp.web.rest.RestUtils.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.opensrp.connector.openmrs.constants.OpenmrsHouseHold.HouseholdMember;
import org.opensrp.domain.Client;
import org.opensrp.domain.postgres.HouseholdClient;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.mysql.jdbc.StringUtils;

@Controller
@RequestMapping(value = "/rest/client")
public class ClientResource extends RestResource<Client> {
	
	private ClientService clientService;
	
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
	
	@Override
	public ResponseEntity<String> searchByCriteria(HttpServletRequest request) throws ParseException {//TODO search should not call different url but only add params
		Map<String, Object> response = new HashMap<String, Object>();
		JsonArray clientsArray = new JsonArray();
		List<Client> clientList = new ArrayList<Client>();
		List<Client> clients = new ArrayList<Client>();
		int total = 0;
		Integer pageNumber = 0;
		Integer pageSize = 0;
		
		String pageNumberParam = getStringFilter(PAGENUMBER, request);
		String pageSizeParam = getStringFilter(PAGESIZE, request);
		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setNameLike(getStringFilter("name", request));
		searchBean.setGender(getStringFilter(GENDER, request));
		DateTime[] birthdate = getDateRangeFilter(BIRTH_DATE, request);//TODO add ranges like fhir do http://hl7.org/fhir/search.html
		DateTime[] deathdate = getDateRangeFilter(DEATH_DATE, request);
		String clientType = getStringFilter(CLIENTTYPE, request);
		searchBean.setOrderByField(getStringFilter(ORDERBYFIELD, request));
		searchBean.setOrderByType(getStringFilter(ORDERBYTYPE, request));
		searchBean.setClientType(clientType);
		searchBean.setProviderId(getStringFilter(PROVIDERID, request));
		searchBean.setPageNumber(pageNumber);
		searchBean.setPageSize(pageSize);
		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}
		if (deathdate != null) {
			searchBean.setDeathdateFrom(deathdate[0]);
			searchBean.setDeathdateTo(deathdate[1]);
		}
		
		if (pageNumberParam != null) {
			pageNumber = Integer.parseInt(pageNumberParam);
		}
		if (pageSizeParam != null) {
			pageSize = Integer.parseInt(pageSizeParam);
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
		List<String> ids = new ArrayList<String>();
		
		clients = clientService.findByCriteria(searchBean, addressSearchBean, lastEdit == null ? null : lastEdit[0],
		    lastEdit == null ? null : lastEdit[1]);
		if (clients.size() != 0) {
			for (Client client : clients) {
				ids.add(client.getBaseEntityId());
			}
			Map<String, HouseholdClient> householdClients = clientService.getMemberCountHouseholdHeadProviderByClients(ids,
			    clientType);
			
			total = clientService.findTotalCountByCriteria(searchBean, addressSearchBean).getTotalCount();
			
			for (Client client : clients) {
				HouseholdClient householdClient = householdClients.get(client.getBaseEntityId());
				if (householdClient != null) {
					client.addAttribute("memberCount", householdClient.getMemebrCount());
					client.addAttribute("HouseholdHead", householdClient.getHouseholdHead());
					client.addAttribute("ProvierId", householdClient.getProviderId());
				} else {
					client.addAttribute("memberCount", 0);
					client.addAttribute("HouseholdHead", "");
					client.addAttribute("ProvierId", "");
				}
				clientList.add(client);
			}
		}
		clientsArray = (JsonArray) EventResource.gson.toJsonTree(clientList, new TypeToken<List<Client>>() {}.getType());
		response.put("clients", clientsArray);
		response.put("total", total);
		return new ResponseEntity<>(new Gson().toJson(response), HttpStatus.OK);
		
	}
	
	@Override
	public List<Client> filter(String query) {
		return clientService.findByDynamicQuery(query);
	}
	
}
