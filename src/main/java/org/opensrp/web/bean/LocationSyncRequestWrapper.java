package org.opensrp.web.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationSyncRequestWrapper {
		@JsonProperty("is_jurisdiction")
		private Boolean isJurisdiction;

		@JsonProperty("location_names")
		private List<String> locationNames;

		@JsonProperty("parent_id")
		private List<String> parentId;

		@JsonProperty
		private long serverVersion;

		public Boolean getIsJurisdiction() {
			return isJurisdiction;
		}

		public List<String> getLocationNames() {
			return locationNames;
		}

		public List<String> getParentId() {
			return parentId;
		}

		public long getServerVersion() {
			return serverVersion;
		}
	}