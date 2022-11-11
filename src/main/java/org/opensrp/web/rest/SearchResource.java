package org.opensrp.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.SearchService;
import org.opensrp.web.utils.ChildMother;
import org.opensrp.web.utils.SearchEntityWrapper;
import org.opensrp.web.utils.SearchHelper;
import org.smartregister.domain.Client;
import org.smartregister.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.Client.ALT_NAME;
import static org.opensrp.common.AllConstants.Client.PHONE_NUMBER;
import static org.opensrp.common.AllConstants.Client.FIRST_NAME;
import static org.opensrp.common.AllConstants.Client.LAST_NAME;
import static org.opensrp.common.AllConstants.Client.MIDDLE_NAME;
import static org.opensrp.common.AllConstants.Client.ATTRIBUTE;
import static org.opensrp.common.AllConstants.Client.IDENTIFIER;
import static org.opensrp.common.AllConstants.Client.GENDER;
import static org.opensrp.common.AllConstants.Client.NAME;
import static org.opensrp.common.AllConstants.Client.ALT_PHONE_NUMBER;
import static org.opensrp.common.AllConstants.Client.BIRTH_DATE;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

@Controller
@RequestMapping(value = "/rest/search")
public class SearchResource extends RestResource<Client> {
	
	private static Logger logger = LogManager.getLogger(SearchResource.class.toString());
	
	private SearchService searchService;
	
	private ClientService clientService;
	
	private EventService eventService;
	
	@Autowired
	public SearchResource(SearchService searchService, ClientService clientService, EventService eventService) {
		this.searchService = searchService;
		this.clientService = clientService;
		this.eventService = eventService;
	}

	/**
	 * @param request
	 * contains search parameter of with attributes and full colon e.g
	 * 1. search?attributes=phone_number:072700000
	 * or search parameter without attribute and without colon e.g
	 * 2. search?phone_number=072700000
	 * @throws ParseException
	 */
	@Override
	public List<Client> search(HttpServletRequest request) throws ParseException {//TODO search should not call different url but only add params
		String firstName = getStringFilter(FIRST_NAME, request);
		String middleName = getStringFilter(MIDDLE_NAME, request);
		String lastName = getStringFilter(LAST_NAME, request);
		Optional<String> phoneNumber = Optional.ofNullable(getStringFilter(PHONE_NUMBER, request));
		Optional<String> altPhoneNumber = Optional.ofNullable(getStringFilter(ALT_PHONE_NUMBER, request));
		Optional<String> alternateName = Optional.ofNullable(getStringFilter(ALT_NAME, request));
		ClientSearchBean searchBean = new ClientSearchBean();
		searchBean.setNameLike(getStringFilter(NAME, request));
		
		searchBean.setGender(getStringFilter(GENDER, request));
		DateTime[] birthdate = RestUtils.getDateRangeFilter(BIRTH_DATE, request);//TODO add ranges like fhir do http://hl7.org/fhir/search.html
		DateTime[] lastEdit = RestUtils.getDateRangeFilter(LAST_UPDATE, request);//TODO client by provider id
		//TODO lookinto Swagger https://slack-files.com/files-pri-safe/T0EPSEJE9-F0TBD0N77/integratingswagger.pdf?c=1458211183-179d2bfd2e974585c5038fba15a86bf83097810a
		
		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}
		if (lastEdit != null) {
			searchBean.setLastEditFrom(lastEdit[0]);
			searchBean.setLastEditTo(lastEdit[1]);
		}
		Map<String, String> attributeMap = new HashMap<>();
		String attributes = getStringFilter(ATTRIBUTE, request);
		if (!StringUtils.isBlank(attributes)) {
			String attributeType = StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[0];
			String attributeValue = StringUtils.isBlank(attributes) ? null : attributes.split(":", -1)[1];
			attributeMap.put(attributeType, attributeValue);
		}
		phoneNumber.ifPresent(phoneValue -> attributeMap.put(PHONE_NUMBER, phoneValue));
		altPhoneNumber.ifPresent(altPhoneValue -> attributeMap.put(ALT_PHONE_NUMBER, altPhoneValue));
		alternateName.ifPresent(altNameValue -> attributeMap.put(ALT_NAME, altNameValue));
		searchBean.setAttributes(attributeMap);

		Map<String, String> identifierMap = null;
		String identifiers = getStringFilter(IDENTIFIER, request);
		if (!StringUtils.isBlank(identifiers)) {
			String identifierType = StringUtils.isBlank(identifiers) ? null : identifiers.split(":", -1)[0];
			String identifierValue = StringUtils.isBlank(identifiers) ? null : identifiers.split(":", -1)[1];

			identifierMap = new HashMap<String, String>();
			identifierMap.put(identifierType, identifierValue);
		}
		
		searchBean.setIdentifiers(identifierMap);
		return searchService.searchClient(searchBean, firstName, middleName, lastName, null);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/path", produces = { MediaType.APPLICATION_JSON_VALUE })
	private List<ChildMother> searchPathBy(HttpServletRequest request) throws ParseException {

			String contactPhoneNumber = SearchHelper.getContactPhoneNumberParam(request);
			SearchEntityWrapper childSearchEntity = SearchHelper.childSearchParamProcessor(request);
			SearchEntityWrapper motherSearchEntity = SearchHelper.motherSearchParamProcessor(request);

			return searchAndProcess(childSearchEntity, motherSearchEntity, contactPhoneNumber);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/path", produces = { MediaType.APPLICATION_JSON_VALUE })
	private List<ChildMother> searchPathByPost(@RequestBody String jsonRequestBody) throws ParseException {

			JSONObject jsonRequestBodyObject = new JSONObject(jsonRequestBody);
			SearchEntityWrapper childSearchEntity = SearchHelper.childSearchParamProcessor(jsonRequestBodyObject);
			SearchEntityWrapper motherSearchEntity = SearchHelper.motherSearchParamProcessor(jsonRequestBodyObject);
			String contactPhoneNumber = SearchHelper.getContactPhoneNumberParam(jsonRequestBodyObject);

			return searchAndProcess(childSearchEntity, motherSearchEntity,contactPhoneNumber);

	}

	private  List<ChildMother> searchAndProcess(SearchEntityWrapper childSearchEntity, SearchEntityWrapper motherSearchEntity,
		String contactPhoneNumber){
		try {
			//Process clients search via demographics

			ClientSearchBean searchBean = new ClientSearchBean();
			List<Client> children = new ArrayList<Client>();
			if (childSearchEntity.isValid()) {
				searchBean = childSearchEntity.getClientSearchBean();
				children = searchService.searchClient(searchBean, searchBean.getFirstName(), searchBean.getMiddleName(),
						searchBean.getLastName(), childSearchEntity.getLimit());
			}

			//Process mothers search via mother demographics

			ClientSearchBean motherSearchBean = new ClientSearchBean();
			List<Client> mothers = new ArrayList<Client>();

			if (motherSearchEntity.isValid()) {
				motherSearchBean = motherSearchEntity.getClientSearchBean();
				mothers = searchService.searchClient(motherSearchBean, motherSearchBean.getFirstName(),
						motherSearchBean.getMiddleName(), motherSearchBean.getLastName(), motherSearchEntity.getLimit());
			}

			//Process clients search via contact phone number


			List<String> clientBaseEntityIds = getClientBaseEntityIdsByContactPhoneNumber(contactPhoneNumber);

			List<Client> eventChildren = clientService.findByFieldValue(BaseEntity.BASE_ENTITY_ID, clientBaseEntityIds);

			children = SearchHelper.intersection(children, eventChildren);// Search conjunction is "AND" find intersection

			List<Client> linkedMothers = new ArrayList<Client>();

			String RELATIONSHIP_KEY = "mother";
			if (!children.isEmpty()) {
				List<String> clientIds = new ArrayList<String>();
				for (Client c : children) {
					String relationshipId = SearchHelper.getRelationalId(c, RELATIONSHIP_KEY);
					if (relationshipId != null && !clientIds.contains(relationshipId)) {
						clientIds.add(relationshipId);
					}
				}

				linkedMothers = clientService.findByFieldValue(BaseEntity.BASE_ENTITY_ID, clientIds);

			}

			List<Client> linkedChildren = new ArrayList<Client>();

			if (!mothers.isEmpty()) {
				for (Client client : mothers) {
					linkedChildren.addAll(clientService.findByRelationship(client.getBaseEntityId()));
				}
			}

			children = SearchHelper.intersection(children, linkedChildren);// Search conjunction is "AND" find intersection

			for (Client linkedMother : linkedMothers) {
				if (!SearchHelper.contains(mothers, linkedMother)) {
					mothers.add(linkedMother);
				}
			}

			return SearchHelper.processSearchResult(children, mothers, RELATIONSHIP_KEY);

		}
		catch (Exception e) {

			logger.error("", e);
			return new ArrayList<ChildMother>();
		}
	}

	public List<String> getClientBaseEntityIdsByContactPhoneNumber(String motherGuardianPhoneNumber) {
		List<String> clientBaseEntityIds = new ArrayList<String>();
		
		if (!StringUtils.isBlank(motherGuardianPhoneNumber)) {
			
			List<Event> events = eventService.findEventsByConceptAndValue("159635AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
			    motherGuardianPhoneNumber);
			if (events != null && !events.isEmpty()) {
				for (Event event : events) {
					String entityId = event.getBaseEntityId();
					if (entityId != null && !clientBaseEntityIds.contains(entityId)) {
						clientBaseEntityIds.add(entityId);
					}
				}
				
			}
		}
		return clientBaseEntityIds;
	}
	
	@Override
	public List<Client> filter(String query) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Client getByUniqueId(String uniqueId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> requiredProperties() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Client create(Client entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Client update(Client entity) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
