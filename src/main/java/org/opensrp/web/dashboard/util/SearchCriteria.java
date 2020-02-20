package org.opensrp.web.dashboard.util;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class SearchCriteria {

	public static Criteria createCriteriaCondition(SearchBuilder searchBuilder, Criteria criteria) {
		if (searchBuilder.getDivision() != null && !searchBuilder.getDivision().isEmpty()) {
			
			criteria.add(Restrictions.eq("division", searchBuilder.getDivision().toUpperCase()));
		}
		if (searchBuilder.getDistrict() != null && !searchBuilder.getDistrict().isEmpty()) {
			
			criteria.add(Restrictions.eq("district", searchBuilder.getDistrict().toUpperCase()));
		}
		if (searchBuilder.getUpazila() != null && !searchBuilder.getUpazila().isEmpty()) {
			
			criteria.add(Restrictions.eq("upazila", searchBuilder.getUpazila()));
		}
		if (searchBuilder.getUnion() != null && !searchBuilder.getUnion().isEmpty()) {
			criteria.add(Restrictions.eq("union", searchBuilder.getUnion()));
		}
		if (searchBuilder.getWard() != null && !searchBuilder.getWard().isEmpty()) {
			criteria.add(Restrictions.eq("ward", searchBuilder.getWard()));
		}
		if (searchBuilder.getMauzapara() != null && !searchBuilder.getMauzapara().isEmpty()) {
			criteria.add(Restrictions.eq("mauzaPara", searchBuilder.getMauzapara()));
		}
		if (searchBuilder.getSubunit() != null && !searchBuilder.getSubunit().isEmpty()) {
			criteria.add(Restrictions.eq("subunit", searchBuilder.getSubunit()));
		}
		if (searchBuilder.getProvider() != null && !searchBuilder.getProvider().isEmpty()) {
			criteria.add(Restrictions.eq("provider", searchBuilder.getProvider()));
		}
		if (searchBuilder.getName() != null && !searchBuilder.getName().isEmpty()) {
			criteria.add(Restrictions.ilike("name", searchBuilder.getName(), MatchMode.ANYWHERE));
		}
		if (searchBuilder.getUserName() != null && !searchBuilder.getUserName().isEmpty()) {
			criteria.add(Restrictions.ilike("username", searchBuilder.getUserName(), MatchMode.ANYWHERE));
		}
		
		return criteria;
	}
	
	public static String getFilterString(SearchBuilder searchBuilder) {

		String filterString = "provider_id != ''";

		if (searchBuilder.getDivision() != null && !searchBuilder.getDivision().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " division = '" + searchBuilder.getDivision() + "'";
		}
		if (searchBuilder.getDistrict() != null && !searchBuilder.getDistrict().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " district = '" + searchBuilder.getDistrict() + "'";
		}
		if (searchBuilder.getUpazila() != null && !searchBuilder.getUpazila().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " upazila = '" + searchBuilder.getUpazila() + "'";
		}
		if (searchBuilder.getUnion() != null && !searchBuilder.getUnion().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " client_union = '" + searchBuilder.getUnion() + "'";
		}
		if (searchBuilder.getWard() != null && !searchBuilder.getWard().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " ward = '" + searchBuilder.getWard() + "'";
		}
		if (searchBuilder.getCommunityClinic() != null && !searchBuilder.getCommunityClinic().equals("")){
			if (!filterString.equals("")) filterString += " and";
			filterString += " cc_name = '" + searchBuilder.getCommunityClinic() + "'";
		}
		if (searchBuilder.getProvider() != null && !searchBuilder.getProvider().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " provider_id = '" + searchBuilder.getProvider() + "'";
		}
		if (searchBuilder.getPregStatus() != null && !searchBuilder.getPregStatus().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " pregnancy_status = '" + searchBuilder.getPregStatus() + "'";
		}

		if (searchBuilder.getStart() != null && !searchBuilder.getStart().equals("")
				&& searchBuilder.getEnd() != null && !searchBuilder.getEnd().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " date_created between '" + searchBuilder.getStart() + "' and '" + searchBuilder.getEnd() + "'";
		}
		if (searchBuilder.getAgeFrom() != null && !searchBuilder.getAgeFrom().equals("")
				&& searchBuilder.getAgeTo() != null && !searchBuilder.getAgeTo().equals("")) {
			if (!filterString.equals("")) filterString += " and";
			filterString += " DATE_PART('day', now() - birth_date) > '" + searchBuilder.getAgeFrom()
					+ "' and DATE_PART('day', now() - birth_date) < '" + searchBuilder.getAgeTo() + "'";
		}
		if (!filterString.equals("")) filterString = "where " + filterString;

		return filterString;
	}
}
