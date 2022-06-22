package org.opensrp.web.utils;

import org.smartregister.domain.Client;

public class ChildMother {

    private Client child;

    private Client mother;

    public ChildMother(Client child, Client mother) {
        this.child = child;
        this.mother = mother;
    }

    @SuppressWarnings("unused")
    public Client getMother() {
        return mother;
    }

    @SuppressWarnings("unused")
    public Client getChild() {
        return child;
    }
}
