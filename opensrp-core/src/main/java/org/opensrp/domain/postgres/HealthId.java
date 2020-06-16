package org.opensrp.domain.postgres;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

public class HealthId {

    private int id;
	
	private String hId;	
	
	private String type;	

	private Boolean status;

	private int locationId;

	@Temporal(TemporalType.TIMESTAMP)
	private Date created = new Date();

	@Temporal(TemporalType.TIMESTAMP)
	private Date updated = new Date();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String gethId() {
		return hId;
	}

	public void sethId(String hId) {
		this.hId = hId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Override
	public String toString() {
		return "HealthId [id=" + id + ", hId=" + hId + ", type=" + type + "]";
	}


	

}
