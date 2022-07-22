package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
