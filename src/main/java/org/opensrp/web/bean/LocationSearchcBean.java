package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.smartregister.domain.PhysicalLocation;

import java.util.List;

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
