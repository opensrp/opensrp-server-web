package org.opensrp.web.rest;

import static org.opensrp.common.AllConstants.BaseEntity.*;
import static org.opensrp.common.AllConstants.Client.BIRTH_DATE;
import static org.opensrp.common.AllConstants.Client.DEATH_DATE;
import static org.opensrp.common.AllConstants.Client.FIRST_NAME;
import static org.opensrp.common.AllConstants.Client.GENDER;
import static org.opensrp.web.rest.RestUtils.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.Client;
import org.opensrp.domain.DataApprove;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mysql.jdbc.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/rest/client")
public class ClientResource extends RestResource<Client> {
	
	private ClientService clientService;
	
	private EventService eventService;
	
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
		return clientService.mergeClient(entity, null);//TODO update should only be based on baseEntityId
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
	public List<Client> filter(String query) {
		return clientService.findByDynamicQuery(query);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(headers = { "Accept=application/json;charset=UTF-8" }, method = POST, value = "/data-approval")
	public ResponseEntity<String> dataApprove(@RequestBody String requestData, HttpServletRequest request)
	    throws JSONException {
		JSONObject jsonObj = new JSONObject();
		try {
			boolean isApproved = true;
			Gson jsonObject = new Gson();
			DataApprove approvalData = jsonObject.fromJson(requestData, DataApprove.class);
			String baseEntityId = approvalData.getBaseEntityId();
			String comments = approvalData.getComment();
			String status = approvalData.getStatus();
			Client client = clientService.find(baseEntityId);
			client.withDataApprovalComments(comments);
			client.withDataApprovalStatus(status);
			if (status.equalsIgnoreCase(AllConstants.APPROVED)) {
				isApproved = false;
			}
			clientService.addOrUpdate(client, isApproved);
			
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
														@RequestParam("rightName") String rightName) throws JSONException {

		List<Client> clients = clientService.findAllClientByUpazila(currentName);

		System.out.println("TOTAL SIZE:->");
		System.out.println(clients.size());

		try {
			for (Client client : clients) {
				Map<String, String> addressFields = client.getAddresses().get(0).getAddressFields();
				addressFields.put(changeAbleField, rightName);
				client.getAddresses().get(0).setAddressFields(addressFields);
				clientService.updateClient(client);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>("done", HttpStatus.OK);
	}
	
}
