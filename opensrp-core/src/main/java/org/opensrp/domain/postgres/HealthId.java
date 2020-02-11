package org.opensrp.domain.postgres;

public class HealthId {
    private int id;
	
	private String hId;	
	
	private String type;	
	

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

	@Override
	public String toString() {
		return "HealthId [id=" + id + ", hId=" + hId + ", type=" + type + "]";
	}


	

}
