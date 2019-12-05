package org.opensrp.web.kipService;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.repository.EventsRepository;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class KipEventService extends EventService {
//    private final EventsRepository allEvents;
    private ClientService clientService;
    private static Logger logger = LoggerFactory.getLogger(EventService.class.toString());

    @Autowired
    public KipEventService(EventsRepository allEvents, ClientService clientService) {
        super(allEvents, clientService);
        this.clientService = clientService;
    }

    @Override
    public synchronized Event processOutOfArea(Event event) {
        try {
            String BIRTH_REGISTRATION_EVENT = "Child Enrollment";
            String GROWTH_MONITORING_EVENT = "Growth Monitoring";
            String VACCINATION_EVENT = "Vaccination";
            String OUT_OF_AREA_SERVICE = "Out of Area Service";
            String NFC_CARD_IDENTIFIER = "NFC_Card_Identifier";
            String CARD_ID_PREFIX = "c_";
            if (StringUtils.isNotBlank(event.getBaseEntityId())) {
                return event;
            } else {
                String identifier = event.getIdentifier("OPENMRS_ID");
                if (StringUtils.isBlank(identifier)) {
                    return event;
                } else {
                    boolean isCardId = identifier.startsWith(CARD_ID_PREFIX);
                    List<Client> clients = isCardId ? this.clientService.findAllByAttribute(NFC_CARD_IDENTIFIER, identifier.substring(CARD_ID_PREFIX.length())) : this.clientService.findAllByIdentifier("openmrs_id".toUpperCase(), identifier);
                    if (clients != null && !clients.isEmpty()) {
                        Iterator var11 = clients.iterator();

                        do {
                            if (!var11.hasNext()) {
                                return event;
                            }

                            Client client = (Client)var11.next();
                            List<Event> existingEvents = this.findByBaseEntityAndType(client.getBaseEntityId(), BIRTH_REGISTRATION_EVENT);
                            if (existingEvents == null || existingEvents.isEmpty()) {
                                return event;
                            }

                            Event birthRegEvent = (Event)existingEvents.get(0);
                            event.getIdentifiers().remove("openmrs_id".toUpperCase());
                            event.setBaseEntityId(client.getBaseEntityId());
                            if (!event.getEventType().startsWith(OUT_OF_AREA_SERVICE)) {
                                event.setProviderId(birthRegEvent.getProviderId());
                                event.setLocationId(birthRegEvent.getLocationId());
                                Map<String, String> details = new HashMap();
                                details.put("out_of_catchment_provider_id", event.getProviderId());
                                event.setDetails(details);
                            } else if (event.getEventType().contains(GROWTH_MONITORING_EVENT) || event.getEventType().contains(VACCINATION_EVENT)) {
                                String eventType = event.getEventType().contains(GROWTH_MONITORING_EVENT) ? GROWTH_MONITORING_EVENT : (event.getEventType().contains(VACCINATION_EVENT) ? VACCINATION_EVENT : null);
                                if (eventType != null) {
                                    Event newEvent = new Event();
                                    newEvent.withBaseEntityId(event.getBaseEntityId()).withEventType(eventType).withEventDate(event.getEventDate()).withEntityType(event.getEntityType()).withProviderId(birthRegEvent.getProviderId()).withLocationId(birthRegEvent.getLocationId()).withFormSubmissionId(UUID.randomUUID().toString()).withDateCreated(event.getDateCreated());
                                    newEvent.setObs(event.getObs());
                                    this.addEvent(newEvent);
                                }
                            }
                        } while(isCardId);

                        return event;
                    } else {
                        return event;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            return event;
        }
    }
}
