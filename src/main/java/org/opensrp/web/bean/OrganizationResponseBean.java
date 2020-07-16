package org.opensrp.web.bean;

import java.util.List;

import org.opensrp.domain.Organization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizationResponseBean {
	
	private List<Organization> organizations;
	
	@JsonProperty("total")
	private Integer total;
	
	private String msg;
	
	public List<Organization> getOrganizations() {
		return organizations;
	}
	
	public void setOrganizations(List<Organization> organizations) {
		this.organizations = organizations;
	}
	
	public Integer getTotal() {
		return total;
	}
	
	public void setTotal(Integer total) {
		this.total = total;
	}
	
	public String getMsg() {
		return msg;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
