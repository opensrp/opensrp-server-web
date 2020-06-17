/**
 * 
 */
package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Samuel Githengi created on 10/07/19
 */
public class SyncParam {
	private String providerId;
	private String locationId;
	private String baseEntityId;
	private String serverVersion;
	private String team;
	private String teamId;
	private Integer limit;
	@JsonProperty("return_count")
	private boolean returnCount;

	public String getProviderId() {
		return providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public String getBaseEntityId() {
		return baseEntityId;
	}

	public void setBaseEntityId(String baseEntityId) {
		this.baseEntityId = baseEntityId;
	}

	public String getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	
	public boolean isReturnCount() {
		return returnCount;
	}
	
	public void setReturnCount(boolean returnCount) {
		this.returnCount = returnCount;
	}
	
}
