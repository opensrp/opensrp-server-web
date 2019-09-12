/**
 * 
 */
package org.opensrp.web.bean;

import java.util.Date;

/**
 * @author Samuel Githengi created on 09/10/19
 */
public class OrganizationAssigmentBean {

	private Long organizationId;

	private String planIdentifier;

	private String jurisdictionIdentifier;

	private Date dateTo;

	private Date dateFrom;

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
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
