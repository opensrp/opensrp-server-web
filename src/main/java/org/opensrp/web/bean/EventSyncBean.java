package org.opensrp.web.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;

public class EventSyncBean {

    private List<Event> events;

    private List<Client> clients;

    @JsonProperty("no_of_events")
    private Integer noOfEvents;

    private String msg;

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
}
