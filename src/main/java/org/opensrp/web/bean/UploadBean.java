package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UploadBean {

	@JsonProperty
	private String identifier;

	@JsonProperty
	private String fileName;

	@JsonProperty
	private Date uploadDate;

	@JsonProperty
	private String providerID;

	@JsonProperty
	private String url;
}
