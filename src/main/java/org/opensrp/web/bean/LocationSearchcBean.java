package org.opensrp.web.bean;

import java.util.List;

import org.smartregister.domain.PhysicalLocation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationSearchcBean {
	
	private List<PhysicalLocation> locations;
	
	@JsonProperty("total")
	private Integer total;
	
	private String msg;
	
	public List<PhysicalLocation> getLocations() {
		return locations;
	}
	
	public void setLocations(List<PhysicalLocation> locations) {
		this.locations = locations;
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
