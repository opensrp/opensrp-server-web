package org.opensrp.web.utils;

import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.Client.GENDER;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.opensrp.domain.Client;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.web.rest.RestUtils;

import com.mysql.jdbc.StringUtils;

public class SearchHelper {
	
	public static SearchEntityWrapper childSearchParamProcessor(HttpServletRequest request) throws ParseException {
		
		ClientSearchBean searchBean = new ClientSearchBean();
		
		String ZEIR_ID = "zeir_id";
		
		String FIRST_NAME = "first_name";
		String MIDDLE_NAME = "middle_name";
		String LAST_NAME = "last_name";
		String BIRTH_DATE = "birth_date";
		
		//Attributes
		String INACTIVE = "inactive";
		String LOST_TO_FOLLOW_UP = "lost_to_follow_up";
		String NFC_CARD_IDENTIFIER = "nfc_card_identifier";
		
		Integer limit = RestUtils.getIntegerFilter("limit", request);
		if (limit == null || limit.intValue() == 0) {
			limit = 100;
		}
		
		DateTime[] lastEdit = RestUtils.getDateRangeFilter(LAST_UPDATE, request);//TODO client by provider id
		if (lastEdit != null) {
			searchBean.setLastEditFrom(lastEdit[0]);
			searchBean.setLastEditTo(lastEdit[1]);
		}
		
		String zeirId = RestUtils.getStringFilter(ZEIR_ID, request);
		
		searchBean.setFirstName(RestUtils.getStringFilter(FIRST_NAME, request));
		searchBean.setMiddleName(RestUtils.getStringFilter(MIDDLE_NAME, request));
		searchBean.setLastName(RestUtils.getStringFilter(LAST_NAME, request));
		searchBean.setGender(RestUtils.getStringFilter(GENDER, request));
		
		String inActive = RestUtils.getStringFilter(INACTIVE, request);
		String lostToFollowUp = RestUtils.getStringFilter(LOST_TO_FOLLOW_UP, request);
		String nfcCardIdentifier = RestUtils.getStringFilter(NFC_CARD_IDENTIFIER, request);
		
		DateTime[] birthdate = RestUtils.getDateRangeFilter(BIRTH_DATE, request);//TODO add ranges like fhir do http://hl7.org/fhir/search.html
		
		//TODO lookinto Swagger https://slack-files.com/files-pri-safe/T0EPSEJE9-F0TBD0N77/integratingswagger.pdf?c=1458211183-179d2bfd2e974585c5038fba15a86bf83097810a
		
		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}
		
		Map<String, String> identifiers = getZEIRIdentifierMap(zeirId);
		
		Map<String, String> attributes = new HashMap<String, String>();
		if (!StringUtils.isEmptyOrWhitespaceOnly(inActive) || !StringUtils.isEmptyOrWhitespaceOnly(lostToFollowUp)
		        || !StringUtils.isEmptyOrWhitespaceOnly(nfcCardIdentifier)) {
			
			if (!StringUtils.isEmptyOrWhitespaceOnly(inActive)) {
				attributes.put(INACTIVE, inActive);
			}
			
			if (!StringUtils.isEmptyOrWhitespaceOnly(lostToFollowUp)) {
				attributes.put(LOST_TO_FOLLOW_UP, lostToFollowUp);
			}
			
			if (!StringUtils.isEmptyOrWhitespaceOnly(nfcCardIdentifier)) {
				attributes.put("NFC_Card_Identifier", nfcCardIdentifier);//Key different case than constant
			}
		}
		
		searchBean.setIdentifiers(identifiers);
		searchBean.setAttributes(attributes);
		
		boolean isValid = isSearchValid(searchBean);
		
		return new SearchEntityWrapper(isValid, searchBean, limit);
	}
	
	public static Map<String, String> getZEIRIdentifierMap(String zeirId) {
		Map<String, String> identifiers = new HashMap<String, String>();
		if (!StringUtils.isEmptyOrWhitespaceOnly(zeirId)) {
			zeirId = formatChildUniqueId(zeirId);
			identifiers.put("ZEIR_ID", zeirId);
		}
		return identifiers;
	}
	
	public static SearchEntityWrapper motherSearchParamProcessor(HttpServletRequest request) throws ParseException {
		
		ClientSearchBean motherSearchBean = new ClientSearchBean();
		
		Integer limit = setCoreFilters(request, motherSearchBean);
		
		// Mother
		String MOTHER_GUARDIAN_FIRST_NAME = "mother_first_name";
		String MOTHER_GUARDIAN_LAST_NAME = "mother_last_name";
		String MOTHER_GUARDIAN_NRC_NUMBER = "mother_nrc_number";
		
		String motherGuardianNrc = RestUtils.getStringFilter(MOTHER_GUARDIAN_NRC_NUMBER, request);
		
		motherSearchBean.setFirstName(RestUtils.getStringFilter(MOTHER_GUARDIAN_FIRST_NAME, request));
		motherSearchBean.setLastName(RestUtils.getStringFilter(MOTHER_GUARDIAN_LAST_NAME, request));
		
		String NRC_NUMBER_KEY = "NRC_Number";
		Map<String, String> motherAttributes = new HashMap<String, String>();
		if (!StringUtils.isEmptyOrWhitespaceOnly(motherGuardianNrc)) {
			motherAttributes.put(NRC_NUMBER_KEY, motherGuardianNrc);
		}
		
		String nameLike = null;
		
		if (!StringUtils.isEmptyOrWhitespaceOnly(motherSearchBean.getFirstName())
		        && org.apache.commons.lang3.StringUtils.containsWhitespace(motherSearchBean.getFirstName().trim())
		        && StringUtils.isEmptyOrWhitespaceOnly(motherSearchBean.getLastName())) {
			String[] arr = motherSearchBean.getFirstName().split("\\s+");
			motherSearchBean.setFirstName(arr[0]);
			motherSearchBean.setLastName(arr[1]);
		} else {
			nameLike = motherSearchBean.getFirstName();
			motherSearchBean.setFirstName(null);
			motherSearchBean.setLastName(null);
		}
		
		motherSearchBean.setNameLike(nameLike);
		motherSearchBean.setAttributes(motherAttributes);
		
		boolean isValid = isSearchValid(motherSearchBean);
		
		return new SearchEntityWrapper(isValid, motherSearchBean, limit);
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
	
	/**
	 * Here we check to see if the search entity param is valid for use in child search Some form of
	 * reflections and custom annotations might have been better
	 * 
	 * @param searchBean model with search params
	 * @return boolean whether the search entity is valid
	 */
	
	public static boolean isSearchValid(ClientSearchBean searchBean) {
		
		return !StringUtils.isEmptyOrWhitespaceOnly(searchBean.getFirstName())
		        || !StringUtils.isEmptyOrWhitespaceOnly(searchBean.getMiddleName())
		        || !StringUtils.isEmptyOrWhitespaceOnly(searchBean.getLastName())
		        || !StringUtils.isEmptyOrWhitespaceOnly(searchBean.getGender())
		        || (searchBean.getAttributes() != null && !searchBean.getAttributes().isEmpty())
		        || searchBean.getBirthdateFrom() != null || searchBean.getBirthdateTo() != null
		        || searchBean.getLastEditFrom() != null || searchBean.getLastEditTo() != null
		        || (searchBean.getIdentifiers() != null && !searchBean.getIdentifiers().isEmpty())
		        || !StringUtils.isEmptyOrWhitespaceOnly(searchBean.getNameLike());
		
	}
	
	public static String formatChildUniqueId(String unformattedId_) {
		String unformattedId = unformattedId_;
		if (!StringUtils.isEmptyOrWhitespaceOnly(unformattedId) && unformattedId.contains("-")) {
			
			unformattedId = unformattedId.split("-")[0];
			
		}
		
		return unformattedId;
	}
	
	/**
	 * // Method returns the intersection of two lists
	 * 
	 * @param list1
	 * @param list2
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
	
	public static List<Client> createClientListIfEmpty(List<Client> list) {
		
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
		motherGuardianPhoneNumber = StringUtils.isEmptyOrWhitespaceOnly(motherGuardianPhoneNumber)
		        ? RestUtils.getStringFilter(CONTACT_PHONE_NUMBER, request)
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
	
	public static String getChildIndentifierFromMotherClient(Client client, String motherIdentifier, String relationshipKey) {
		String identifier = client.getIdentifier(motherIdentifier);
		if (!StringUtils.isEmptyOrWhitespaceOnly(identifier)) {
			String[] arr = identifier.split("_");
			if (arr != null && arr.length == 2 && arr[1].contains(relationshipKey)) {
				return arr[0];
			}
		}
		return null;
	}
	
}
