package org.opensrp.web.rest.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.web.rest.EventResource;
import org.springframework.stereotype.Component;

@Component
public class EventResourceShadow extends EventResource {

    public EventResourceShadow(ClientService clientService, EventService eventService) {
        super(clientService, eventService);
    }

    public EventResourceShadow() {
        super(null, null);
    }

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
    }
}
