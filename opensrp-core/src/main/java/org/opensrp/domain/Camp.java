package org.opensrp.domain;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.motechproject.model.MotechBaseDataObject;

@TypeDiscriminator("doc.type == 'Camp'")
public class Camp extends MotechBaseDataObject {
	
	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty
	private String providerName;
	
	@JsonProperty
	private String date;
	
	@JsonProperty
	private String campName;
	
	@JsonProperty
	private String centerName;
	
	@JsonProperty
	private boolean status;
	
	@JsonProperty
	private long timestamp;
	
	@JsonProperty
	private String created_by;
	
	@JsonProperty
	private Date created_date;
	
	public String getProviderName() {
		return providerName;
	}
	
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getCampName() {
		return campName;
	}
	
	public void setCampName(String campName) {
		this.campName = campName;
	}
	
	public boolean isStatus() {
		return status;
	}
	
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getCreatedBy() {
		return created_by;
	}
	
	public void setCreatedBy(String created_by) {
		this.created_by = created_by;
	}
	
	public Date getCreatedDate() {
		return created_date;
	}
	
	public void setCreatedDate(Date created_date) {
		this.created_date = created_date;
	}
	
	public String getCenterName() {
		return centerName;
	}
	
	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}
	
}
