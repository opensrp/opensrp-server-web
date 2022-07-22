package org.opensrp.web.utils;

import org.opensrp.search.ClientSearchBean;

public class SearchEntityWrapper {

    private boolean isValid;

    private ClientSearchBean clientSearchBean;

    private Integer limit;

    public SearchEntityWrapper(boolean isValid, ClientSearchBean clientSearchBean, Integer limit) {

        setValid(isValid);
        setClientSearchBean(clientSearchBean);
        setLimit(limit);
    }

    public SearchEntityWrapper(boolean isValid, ClientSearchBean clientSearchBean) {

        this(isValid, clientSearchBean, 100);
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    public ClientSearchBean getClientSearchBean() {
        return clientSearchBean;
    }

    public void setClientSearchBean(ClientSearchBean clientSearchBean) {
        this.clientSearchBean = clientSearchBean;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}
