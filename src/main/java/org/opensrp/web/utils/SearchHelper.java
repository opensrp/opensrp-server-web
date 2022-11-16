package org.opensrp.web.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.smartregister.domain.Client;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.web.rest.RestUtils;

import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.Client.GENDER;

public class SearchHelper {

	public static final String ZEIR_ID = "zeir_id";
	public static final String OPENSRP_ID = "opensrp_id";

	public static final String SIM_PRINT_GUID = "simprints_guid";

	public static final String FIRST_NAME = "first_name";
	public static final String MIDDLE_NAME = "middle_name";
	public static final String LAST_NAME = "last_name";
	public static final String BIRTH_DATE = "birth_date";

	//Attributes
	public static final String INACTIVE = "inactive";
	public static final String LOST_TO_FOLLOW_UP = "lost_to_follow_up";
	public static final String NFC_CARD_IDENTIFIER = "nfc_card_identifier";

	// Mother
	public static final String MOTHER_GUARDIAN_FIRST_NAME = "mother_first_name";
	public static final String MOTHER_GUARDIAN_LAST_NAME = "mother_last_name";
	public static final String MOTHER_GUARDIAN_NRC_NUMBER = "mother_nrc_number";
	public static final String MOTHER_COMPASS_RELATIONSHIP_ID = "mother_compass_relationship_id";

	public static final String NRC_NUMBER_KEY = "NRC_Number";
	public static final String COMPASS_RELATIONSHIP_ID = "Compass_Relationship_ID";

	public static SearchEntityWrapper childSearchParamProcessor(HttpServletRequest request) throws ParseException {
		
		ClientSearchBean searchBean = new ClientSearchBean();



		Integer limit = RestUtils.getIntegerFilter("limit", request);
		if (limit == null || limit.intValue() == 0) {
			limit = 100;
		}
		
		DateTime[] lastEdit = RestUtils.getDateRangeFilter(LAST_UPDATE, request);//TODO client by provider id
		if (lastEdit != null) {
			searchBean.setLastEditFrom(lastEdit[0]);
			searchBean.setLastEditTo(lastEdit[1]);
		}



		searchBean.setFirstName(RestUtils.getStringFilter(FIRST_NAME, request));
		searchBean.setMiddleName(RestUtils.getStringFilter(MIDDLE_NAME, request));
		searchBean.setLastName(RestUtils.getStringFilter(LAST_NAME, request));
		searchBean.setGender(RestUtils.getStringFilter(GENDER, request));

		DateTime[] birthdate = RestUtils
				.getDateRangeFilter(BIRTH_DATE, request);//TODO add ranges like fhir do http://hl7.org/fhir/search.html

		//TODO lookinto Swagger https://slack-files.com/files-pri-safe/T0EPSEJE9-F0TBD0N77/integratingswagger.pdf?c=1458211183-179d2bfd2e974585c5038fba15a86bf83097810a

		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}


		Map<String, String> commonSearchParams = new HashMap<>();
		commonSearchParams.put(ZEIR_ID,  RestUtils.getStringFilter(ZEIR_ID, request));
		commonSearchParams.put(OPENSRP_ID, RestUtils.getStringFilter(OPENSRP_ID, request));
		commonSearchParams.put(SIM_PRINT_GUID, RestUtils.getStringFilter(SIM_PRINT_GUID, request));
		commonSearchParams.put(INACTIVE, RestUtils.getStringFilter(INACTIVE, request));
		commonSearchParams.put(LOST_TO_FOLLOW_UP, RestUtils.getStringFilter(LOST_TO_FOLLOW_UP, request));
		commonSearchParams.put(NFC_CARD_IDENTIFIER, RestUtils.getStringFilter(NFC_CARD_IDENTIFIER, request));

		setIdentifiersAndAttributeToChildSearchBean(commonSearchParams, searchBean);

		boolean isValid = isSearchValid(searchBean);

		return new SearchEntityWrapper(isValid, searchBean, limit);
	}

	public static SearchEntityWrapper motherSearchParamProcessor(HttpServletRequest request) throws ParseException {

		ClientSearchBean motherSearchBean = new ClientSearchBean();

		Integer limit = setCoreFilters(request, motherSearchBean);



		String motherGuardianNrc = RestUtils.getStringFilter(MOTHER_GUARDIAN_NRC_NUMBER, request);
		String compassRelationshipId = RestUtils.getStringFilter(MOTHER_COMPASS_RELATIONSHIP_ID, request);

		motherSearchBean.setFirstName(RestUtils.getStringFilter(MOTHER_GUARDIAN_FIRST_NAME, request));
		motherSearchBean.setLastName(RestUtils.getStringFilter(MOTHER_GUARDIAN_LAST_NAME, request));

		setNameLikeAndAtrributesOnMotherSearchBean(motherGuardianNrc,compassRelationshipId, motherSearchBean);

		boolean isValid = isSearchValid(motherSearchBean);

		return new SearchEntityWrapper(isValid, motherSearchBean, limit);
	}

	public static SearchEntityWrapper childSearchParamProcessor(JSONObject jsonObject) throws ParseException {

		ClientSearchBean searchBean = new ClientSearchBean();

		Integer limit = !jsonObject.optString("limit").equals("") ? Integer.parseInt(jsonObject.optString("limit"))
		: jsonObject.optInt("limit");
		if (limit == 0) {
			limit = 100;
		}

		DateTime[] lastEdit = RestUtils.getDateRangeFilter(LAST_UPDATE, jsonObject);//TODO client by provider id
		if (lastEdit != null) {
			searchBean.setLastEditFrom(lastEdit[0]);
			searchBean.setLastEditTo(lastEdit[1]);
		}

		searchBean.setFirstName(jsonObject.optString(FIRST_NAME));
		searchBean.setMiddleName(jsonObject.optString(MIDDLE_NAME));
		searchBean.setLastName(jsonObject.optString(LAST_NAME));
		searchBean.setGender(jsonObject.optString(GENDER));

		DateTime[] birthdate = RestUtils
				.getDateRangeFilter(BIRTH_DATE, jsonObject);//TODO add ranges like fhir do http://hl7.org/fhir/search.html

		//TODO lookinto Swagger https://slack-files.com/files-pri-safe/T0EPSEJE9-F0TBD0N77/integratingswagger.pdf?c=1458211183-179d2bfd2e974585c5038fba15a86bf83097810a
		
		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}

		Map<String, String> commonSearchParams = new HashMap<>();
		commonSearchParams.put(ZEIR_ID, jsonObject.optString(ZEIR_ID));
		commonSearchParams.put(OPENSRP_ID, jsonObject.optString(OPENSRP_ID));
		commonSearchParams.put(SIM_PRINT_GUID, jsonObject.optString(SIM_PRINT_GUID));
		commonSearchParams.put(INACTIVE, jsonObject.optString(INACTIVE));
		commonSearchParams.put(LOST_TO_FOLLOW_UP, jsonObject.optString(LOST_TO_FOLLOW_UP));
		commonSearchParams.put(NFC_CARD_IDENTIFIER, jsonObject.optString(NFC_CARD_IDENTIFIER));

		setIdentifiersAndAttributeToChildSearchBean(commonSearchParams, searchBean);

		boolean isValid = isSearchValid(searchBean);

		return new SearchEntityWrapper(isValid, searchBean, limit);
	}

	public static SearchEntityWrapper motherSearchParamProcessor(JSONObject jsonObject) throws ParseException {

		ClientSearchBean motherSearchBean = new ClientSearchBean();

		Integer limit = setCoreFilters(jsonObject, motherSearchBean);

		String motherGuardianNrc = jsonObject.optString(MOTHER_GUARDIAN_NRC_NUMBER);
		String compassRelationshipId = jsonObject.optString(MOTHER_COMPASS_RELATIONSHIP_ID);

		motherSearchBean.setFirstName(jsonObject.optString(MOTHER_GUARDIAN_FIRST_NAME));
		motherSearchBean.setLastName(jsonObject.optString(MOTHER_GUARDIAN_LAST_NAME));

		setNameLikeAndAtrributesOnMotherSearchBean(motherGuardianNrc,compassRelationshipId, motherSearchBean);

		boolean isValid = isSearchValid(motherSearchBean);

		return new SearchEntityWrapper(isValid, motherSearchBean, limit);
	}

	public static void setNameLikeAndAtrributesOnMotherSearchBean(String motherGuardianNrc,
																  String compassRelationshipId,
																  ClientSearchBean motherSearchBean){
		Map<String, String> motherAttributes = new HashMap<>();
		if (!StringUtils.isBlank(motherGuardianNrc)) {
			motherAttributes.put(NRC_NUMBER_KEY, motherGuardianNrc);
		}
		if (!StringUtils.isBlank(compassRelationshipId)) {
			motherAttributes.put(COMPASS_RELATIONSHIP_ID, compassRelationshipId);
		}

		String nameLike = null;

		if (!StringUtils.isBlank(motherSearchBean.getFirstName())
				&& StringUtils.containsWhitespace(motherSearchBean.getFirstName().trim())
				&& StringUtils.isBlank(motherSearchBean.getLastName())) {
			String[] arr = motherSearchBean.getFirstName().split("\\s+");
			nameLike = arr[0];
			motherSearchBean.setFirstName(null);
		}

		motherSearchBean.setNameLike(nameLike);
		motherSearchBean.setAttributes(motherAttributes);

	}

	public static void setIdentifiersAndAttributeToChildSearchBean(Map<String, String> commonSearchParams, ClientSearchBean searchBean){
		Map<String, String> identifiers = new HashMap<String, String>();

		String zeirId = commonSearchParams.get(ZEIR_ID);
		String opensrpId = commonSearchParams.get(OPENSRP_ID);
		String simprintsGuid = commonSearchParams.get(SIM_PRINT_GUID);
		String lostToFollowUp = commonSearchParams.get(LOST_TO_FOLLOW_UP);
		String inActive = commonSearchParams.get(INACTIVE);
		String nfcCardIdentifier = commonSearchParams.get(NFC_CARD_IDENTIFIER);

		if (!StringUtils.isBlank(zeirId)) {
			identifiers.put(ZEIR_ID, zeirId);
			identifiers.put("ZEIR_ID", zeirId); //Maintains backward compatibility with upper case key
		}

		if (!StringUtils.isBlank(opensrpId)) {
			identifiers.put(OPENSRP_ID, opensrpId);
		}
		if (!StringUtils.isBlank(simprintsGuid)) {
			identifiers.put(SIM_PRINT_GUID, simprintsGuid);
		}


		Map<String, String> attributes = new HashMap<String, String>();
		if (!StringUtils.isBlank(inActive) || !StringUtils.isBlank(lostToFollowUp)
		        || !StringUtils.isBlank(nfcCardIdentifier)) {
			
			if (!StringUtils.isBlank(inActive)) {
				attributes.put(INACTIVE, inActive);
			}
			
			if (!StringUtils.isBlank(lostToFollowUp)) {
				attributes.put(LOST_TO_FOLLOW_UP, lostToFollowUp);
			}
			
			if (!StringUtils.isBlank(nfcCardIdentifier)) {
				attributes.put("NFC_Card_Identifier", nfcCardIdentifier);//Key different case than constant
			}
		}
		
		searchBean.setIdentifiers(identifiers);
		searchBean.setAttributes(attributes);

	}
	
	public static Integer setCoreFilters(HttpServletRequest request, ClientSearchBean searchBean) throws ParseException {
		
		Integer limit = RestUtils.getIntegerFilter("limit", request);
		if (limit == null || limit.intValue() == 0) {
			limit = 100;
		}
		
		DateTime[] lastEdit = RestUtils.getDateRangeFilter(LAST_UPDATE, request);//TODO client by provider id
		if (lastEdit != null) {
			searchBean.setLastEditFrom(lastEdit[0]);
			searchBean.setLastEditTo(lastEdit[1]);
		}

		return limit;
	}

	public static Integer setCoreFilters(JSONObject jsonObject, ClientSearchBean searchBean) throws ParseException {

		Integer limit = !jsonObject.optString("limit").equals("") ? Integer.parseInt(jsonObject.optString("limit"))
				: jsonObject.optInt("limit");
		if (limit == 0) {
			limit = 100;
		}

		DateTime[] lastEdit = RestUtils.getDateRangeFilter(LAST_UPDATE, jsonObject);//TODO client by provider id
		if (lastEdit != null) {
			searchBean.setLastEditFrom(lastEdit[0]);
			searchBean.setLastEditTo(lastEdit[1]);
		}

		return limit;
	}
	
	/**
	 * Here we check to see if the search entity param is valid for use in child search Some form of
	 * reflections and custom annotations might have been better
	 * 
	 * @param searchBean model with search params
	 * @return boolean whether the search entity is valid
	 */
	
	public static boolean isSearchValid(ClientSearchBean searchBean) {
		
		return !StringUtils.isBlank(searchBean.getFirstName())
		        || !StringUtils.isBlank(searchBean.getMiddleName())
		        || !StringUtils.isBlank(searchBean.getLastName())
		        || !StringUtils.isBlank(searchBean.getGender())
		        || (searchBean.getAttributes() != null && !searchBean.getAttributes().isEmpty())
		        || searchBean.getBirthdateFrom() != null || searchBean.getBirthdateTo() != null
		        || searchBean.getLastEditFrom() != null || searchBean.getLastEditTo() != null
		        || (searchBean.getIdentifiers() != null && !searchBean.getIdentifiers().isEmpty())
		        || !StringUtils.isBlank(searchBean.getNameLike());
		
	}
	
	/**
	 * // Method returns the intersection of two lists
	 * 
	 * @param list1_
	 * @param list2_
	 * @return merged intersection list
	 */
	public static List<Client> intersection(List<Client> list1_, List<Client> list2_) {
		
		List<Client> list1 = list1_;
		List<Client> list2 = list2_;
		
		list1 = createClientListIfEmpty(list1);
		
		list2 = createClientListIfEmpty(list2);
		
		if (list1.isEmpty() && list2.isEmpty()) {
			return new ArrayList<Client>();
		}
		
		if (list1.isEmpty() && !list2.isEmpty()) {
			return list2;
		}
		
		if (list2.isEmpty() && !list1.isEmpty()) {
			return list1;
		}
		
		List<Client> list = new ArrayList<Client>();
		
		for (Client t : list1) {
			if (contains(list2, t)) {
				list.add(t);
			}
		}
		
		return list;
	}
	
	public static List<Client> createClientListIfEmpty(List<Client> list_) {
		List<Client> list = list_;
		
		if (list == null) {
			list = new ArrayList<Client>();
		}
		
		return list;
	}
	
	public static boolean contains(List<Client> clients, Client c) {
		if (clients == null || clients.isEmpty() || c == null) {
			return false;
		}
		for (Client client : clients) {
			
			if (client != null && client.getBaseEntityId() != null && c.getBaseEntityId() != null
			        && client.getBaseEntityId().equals(c.getBaseEntityId())) {
				
				return true;
				
			}
		}
		return false;
	}
	
	public static String getContactPhoneNumberParam(HttpServletRequest request) {
		//Search by mother contact number
		String MOTHER_GUARDIAN_PHONE_NUMBER = "mother_contact_phone_number";
		String CONTACT_PHONE_NUMBER = "contact_phone_number";
		String motherGuardianPhoneNumber = RestUtils.getStringFilter(MOTHER_GUARDIAN_PHONE_NUMBER, request);
		motherGuardianPhoneNumber = StringUtils.isBlank(motherGuardianPhoneNumber)
				? RestUtils.getStringFilter(CONTACT_PHONE_NUMBER, request)
				: motherGuardianPhoneNumber;

		return motherGuardianPhoneNumber;
	}

	public static String getContactPhoneNumberParam(JSONObject jsonObject) {
		//Search by mother contact number
		String MOTHER_GUARDIAN_PHONE_NUMBER = "mother_contact_phone_number";
		String CONTACT_PHONE_NUMBER = "contact_phone_number";
		String motherGuardianPhoneNumber = jsonObject.optString(MOTHER_GUARDIAN_PHONE_NUMBER);
		motherGuardianPhoneNumber = StringUtils.isBlank(motherGuardianPhoneNumber)
				? jsonObject.optString(CONTACT_PHONE_NUMBER)
				: motherGuardianPhoneNumber;

		return motherGuardianPhoneNumber;
	}
	
	public static List<ChildMother> processSearchResult(List<Client> children, List<Client> mothers,
	                                                    String RELATIONSHIP_KEY) {
		List<ChildMother> childMotherList = new ArrayList<ChildMother>();
		for (Client child : children) {
			for (Client mother : mothers) {
				String relationalId = getRelationalId(child, RELATIONSHIP_KEY);
				String motherEntityId = mother.getBaseEntityId();
				if (relationalId != null && motherEntityId != null && relationalId.equalsIgnoreCase(motherEntityId)) {
					childMotherList.add(new ChildMother(child, mother));
				}
			}
		}
		
		return childMotherList;
	}
	
	public static String getRelationalId(Client c, String relationshipKey) {
		Map<String, List<String>> relationships = c.getRelationships();
		if (relationships != null) {
			for (Map.Entry<String, List<String>> entry : relationships.entrySet()) {
				String key = entry.getKey();
				if (key.equalsIgnoreCase(relationshipKey)) {
					List<String> rList = entry.getValue();
					if (!rList.isEmpty()) {
						return rList.get(0);
					}
				}
			}
		}
		
		return null;
	}
}
