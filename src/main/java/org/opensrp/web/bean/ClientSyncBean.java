package org.opensrp.web.bean;

import java.util.List;
import org.opensrp.domain.Client;

public class ClientSyncBean {

    private List<Client> clients;

    private Integer total;

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
