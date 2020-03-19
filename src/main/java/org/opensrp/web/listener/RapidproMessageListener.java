package org.opensrp.web.listener;

import java.util.List;

import org.json.JSONException;
import org.opensrp.common.util.DateUtil;
import org.opensrp.connector.rapidpro.MessageFactory;
import org.opensrp.connector.rapidpro.MessageService;
import org.opensrp.connector.rapidpro.MessageType;
import org.opensrp.connector.repository.couch.AllCamp;
import org.opensrp.domain.Camp;
import org.opensrp.domain.Event;
import org.opensrp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Profile("rapidpro")
@Service
@EnableScheduling
public class RapidproMessageListener {
	
	private MessageService messageService;
	
	private static Logger logger = LoggerFactory.getLogger(RapidproMessageListener.class.toString());
	
	@Autowired
	private AllCamp allCamp;
	
	@Autowired
	private EventService eventService;
	
	public RapidproMessageListener() {
		
	}
	
	@Autowired
	public RapidproMessageListener( MessageService messageService) {
		this.messageService = messageService;
		
	}
	
	public void campAnnouncementListener(String provider) {
		MessageFactory messageFactory = null;
		messageFactory = MessageFactory.getMessageFactory(MessageType.ANNOUNCEMENT);
		logger.info("request receive for camp announchment message provider: " + provider);
		try {
			List<Camp> camps = allCamp.findAllActiveByProvider(provider);
			if (camps != null) {
				for (Camp camp : camps) {
					if (DateUtil.dateDiff(camp.getDate()) == 0) {
						//List<Action> actions = actionService.findAllActionByProviderNotExpired(camp.getProviderName());
						List<Event> events = eventService.findByProviderAndEntityType(camp.getProviderName());
						logger.info("total events found for announcement eventSize: " + events.size() + " ,provider:"
						        + camp.getProviderName());
						messageService.sentMessageToClient(messageFactory, events, camp);
						allCamp.updateCamp(camp);
					} else {
						logger.info("No Camp Found for camp announchment message");
					}
				}
			} else {
				logger.info("No Camp Found for camp announchment message");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void fetchClient() throws JSONException {
		MessageFactory messageFactory = null;
		messageFactory = MessageFactory.getMessageFactory(MessageType.REMINDER);
		logger.info("started processing camp reminder messages");
		try {
			List<Camp> camps = allCamp.findAllActive();
			//List<Action> actions = null;
			if (camps != null) {
				for (Camp camp : camps) {
					if (DateUtil.dateDiff(camp.getDate()) == -1) {
						if (camp.getProviderName() == null || camp.getProviderName().isEmpty()
						        || camp.getProviderName().equalsIgnoreCase("")) {
							logger.info("problem with camp definition!!");
							//actions = actionService.findAllActionNotExpired();
							//List<Event> events = eventService.findByProviderAndEntityType(camp.getProviderName());
						} else {
							logger.info("finding all events for provider:" + camp.getProviderName());
							//actions = actionService.findAllActionByProviderNotExpired(camp.getProviderName());
							List<Event> events = eventService.findByProviderAndEntityType(camp.getProviderName());
							logger.info("total events found for reminder eventSize: " + events.size() + " ,provider:"
							        + camp.getProviderName());
							messageService.sentMessageToClient(messageFactory, events, camp);
						}
						
						//allCamp.updateCamp(camp);
					} else {
						logger.info("No Camp Found for camp reminder message");
					}
				}
				
			} else {
				logger.info("No Camp Found for camp reminder message");
			}
			
		}
		catch (Exception e) {
			logger.info("Fetch client:" + e.getMessage());
		}
	}
}
