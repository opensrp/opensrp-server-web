package org.opensrp.web.bean;

import java.util.List;

import org.opensrp.domain.Organization;

public class OrganizationResponseBean {
	
	private List<Organization> organizations;
	
	public List<Organization> getOrganizations() {
		return organizations;
	}
	
	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}
	
}
