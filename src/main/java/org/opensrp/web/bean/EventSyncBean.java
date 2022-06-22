package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.smartregister.domain.Client;
import org.smartregister.domain.Event;

import java.util.List;

public class EventSyncBean {

    private List<Event> events;

    private List<Client> clients;

    @JsonProperty("no_of_events")
    private Integer noOfEvents;

    private String msg;

    @JsonProperty("total_records")
    private Long totalRecords;

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public Integer getNoOfEvents() {
        return noOfEvents;
    }

    public void setNoOfEvents(Integer noOfEvents) {
        this.noOfEvents = noOfEvents;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    public Long getTotalRecords() {
        return totalRecords;
    }


    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }


}
