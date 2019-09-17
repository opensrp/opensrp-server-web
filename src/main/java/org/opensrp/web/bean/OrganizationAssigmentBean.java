/**
 * 
 */
package org.opensrp.web.bean;

import java.util.Date;

/**
 * @author Samuel Githengi created on 09/10/19
 */
public class OrganizationAssigmentBean {

	private String organization;

	private String planIdentifier;

	private String jurisdictionIdentifier;

	private Date dateTo;

	private Date dateFrom;

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getPlanIdentifier() {
		return planIdentifier;
	}

	public void setPlanIdentifier(String planIdentifier) {
		this.planIdentifier = planIdentifier;
	}

	public String getJurisdictionIdentifier() {
		return jurisdictionIdentifier;
	}

	public void setJurisdictionIdentifier(String jurisdictionIdentifier) {
		this.jurisdictionIdentifier = jurisdictionIdentifier;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

}
