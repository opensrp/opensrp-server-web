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

	private String plan;

	private String jurisdiction;

	private Date toDate;

	private Date fromDate;

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getPlan() {
		return plan;
	}

	public void setPlan(String plan) {
		this.plan = plan;
	}

	public String getJurisdiction() {
		return jurisdiction;
	}

	public void setJurisdiction(String jurisdiction) {
		this.jurisdiction = jurisdiction;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

}
