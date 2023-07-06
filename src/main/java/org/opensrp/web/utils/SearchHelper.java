package org.opensrp.web.utils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.web.rest.RestUtils;
import org.smartregister.domain.Client;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opensrp.common.AllConstants.BaseEntity.LAST_UPDATE;
import static org.opensrp.common.AllConstants.Client.GENDER;

public class SearchHelper {

	public static SearchEntityWrapper childSearchParamProcessor(HttpServletRequest request) throws ParseException {

		ClientSearchBean searchBean = new ClientSearchBean();

		String ZEIR_ID = "zeir_id";
		String OPENSRP_ID = "opensrp_id";

		String SIM_PRINT_GUID = "simprints_guid";

		String FIRST_NAME = "first_name";
		String MIDDLE_NAME = "middle_name";
		String LAST_NAME = "last_name";
		String BIRTH_DATE = "birth_date";

		//Attributes
		String ACTIVE = "active";
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
		String opensrpId = RestUtils.getStringFilter(OPENSRP_ID, request);
		String simprintsGuid = RestUtils.getStringFilter(SIM_PRINT_GUID, request);

		searchBean.setFirstName(RestUtils.getStringFilter(FIRST_NAME, request));
		searchBean.setMiddleName(RestUtils.getStringFilter(MIDDLE_NAME, request));
		searchBean.setLastName(RestUtils.getStringFilter(LAST_NAME, request));
		searchBean.setGender(RestUtils.getStringFilter(GENDER, request));

		String active = RestUtils.getStringFilter(ACTIVE, request);
		String inActive = RestUtils.getStringFilter(INACTIVE, request);
		String lostToFollowUp = RestUtils.getStringFilter(LOST_TO_FOLLOW_UP, request);
		String nfcCardIdentifier = RestUtils.getStringFilter(NFC_CARD_IDENTIFIER, request);

		DateTime[] birthdate = RestUtils
				.getDateRangeFilter(BIRTH_DATE, request);//TODO add ranges like fhir do http://hl7.org/fhir/search.html

		//TODO lookinto Swagger https://slack-files.com/files-pri-safe/T0EPSEJE9-F0TBD0N77/integratingswagger.pdf?c=1458211183-179d2bfd2e974585c5038fba15a86bf83097810a

		if (birthdate != null) {
			searchBean.setBirthdateFrom(birthdate[0]);
			searchBean.setBirthdateTo(birthdate[1]);
		}
		Map<String, String> identifiers = new HashMap<String, String>();
		//
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
		if (!StringUtils.isBlank(active) || !StringUtils.isBlank(inActive)
				|| !StringUtils.isBlank(lostToFollowUp) || !StringUtils.isBlank(nfcCardIdentifier)) {

			if (!StringUtils.isBlank(active)) {
				attributes.put(ACTIVE, active);
			}

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

		boolean isValid = isSearchValid(searchBean);

		return new SearchEntityWrapper(isValid, searchBean, limit);
	}

	public static SearchEntityWrapper motherSearchParamProcessor(HttpServletRequest request) throws ParseException {

		ClientSearchBean motherSearchBean = new ClientSearchBean();

		Integer limit = setCoreFilters(request, motherSearchBean);

		// Mother
		String MOTHER_GUARDIAN_FIRST_NAME = "mother_first_name";
		String MOTHER_GUARDIAN_LAST_NAME = "mother_last_name";
		String MOTHER_GUARDIAN_NRC_NUMBER = "mother_nrc_number";
		String MOTHER_COMPASS_RELATIONSHIP_ID = "mother_compass_relationship_id";

		String motherGuardianNrc = RestUtils.getStringFilter(MOTHER_GUARDIAN_NRC_NUMBER, request);
		String compassRelationshipId = RestUtils.getStringFilter(MOTHER_COMPASS_RELATIONSHIP_ID, request);

		motherSearchBean.setFirstName(RestUtils.getStringFilter(MOTHER_GUARDIAN_FIRST_NAME, request));
		motherSearchBean.setLastName(RestUtils.getStringFilter(MOTHER_GUARDIAN_LAST_NAME, request));

		String NRC_NUMBER_KEY = "NRC_Number";
		String COMPASS_RELATIONSHIP_ID = "Compass_Relationship_ID";

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
		if (clients == null || clients.isEmpty() || c == null || c.getBaseEntityId() == null) {
			return false;
		}
		for (Client client : clients) {

			if (client != null && client.getBaseEntityId() != null
					&& c.getBaseEntityId().equals(client.getBaseEntityId())) {

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

	public static List<ChildMother> processSearchResult(List<Client> children, List<Client> mothers,
			String RELATIONSHIP_KEY) {
		List<ChildMother> childMotherList = new ArrayList<ChildMother>();
		for (Client child : children) {
			for (Client mother : mothers) {
				String relationalId = getRelationalId(child, RELATIONSHIP_KEY);
				String motherEntityId = mother.getBaseEntityId();
				if (relationalId != null && relationalId.equalsIgnoreCase(motherEntityId)) {
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
