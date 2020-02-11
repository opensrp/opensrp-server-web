package org.opensrp.connector.rapidpro;

import static org.opensrp.dto.AlertStatus.normal;
import static org.opensrp.dto.AlertStatus.upcoming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.opensrp.common.util.DateUtil;
import org.opensrp.domain.Camp;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.service.ClientService;
import org.opensrp.service.RapidProServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
	
	private static Logger logger = LoggerFactory.getLogger(MessageService.class.toString());
	
	private RapidProServiceImpl rapidproService;
	
	private ClientService clientService;
	
	public MessageService() {
		
	}
	
	@Autowired
	public MessageService(RapidProServiceImpl rapidproService, ClientService clientService) {
		
		this.rapidproService = rapidproService;
		this.clientService = clientService;
	}
	
	public void sentMessageToClient(MessageFactory messageFactory, List<Event> events, Camp camp) throws JSONException {
		
		if (events != null) {
			for (Event event : events) {
				/*				Map<String, String> data = action.data();
								logger.info("sentMessageToClient actiondata:" +  data.toString());*/
				if (event.getEntityType().equalsIgnoreCase(ClientType.child.name())) {
					Client child = clientService.find(event.getBaseEntityId());
					if (child != null) {
						logger.info("sending message to child childBaseEntityId:" + child.getBaseEntityId());
						Map<String, List<String>> relationships = child.getRelationships();
						String motherId = relationships.get("mother").get(0);
						Client mother = clientService.find(motherId);
						logger.info("sending message to mother moterBaseEntityId:" + mother.getBaseEntityId());
						generateDataAndsendMessageToRapidpro(mother, ClientType.child, messageFactory, camp);
					}
				} else if (event.getEntityType().equalsIgnoreCase(ClientType.mother.name())) {
					Client mother = clientService.find(event.getBaseEntityId());
					if (mother != null) {
						logger.info("sending message to mother moterBaseEntityId:" + mother.getBaseEntityId());
						generateDataAndsendMessageToRapidpro(mother, ClientType.mother, messageFactory, camp);
					}
					
				} else {
					
				}
			}
		} else {
			logger.info("No vaccine data Found Today");
		}
	}
	
	private void generateDataAndsendMessageToRapidpro(Client client, ClientType clientType, MessageFactory messageFactory,
	                                                  Camp camp) {
		
		Map<String, Object> attributes = new HashMap<>();
		attributes = client.getAttributes();
		List<String> urns;
		urns = new ArrayList<String>();
		if (attributes.containsKey("phoneNumber")) {
			logger.info("sending mesage to mobileno:" + addExtensionToMobile((String) attributes.get("phoneNumber")));
			urns.add("tel:" + addExtensionToMobile((String) attributes.get("phoneNumber")));
			List<String> contacts;
			contacts = new ArrayList<String>();
			List<String> groups = new ArrayList<String>();
			rapidproService.sendMessage(urns, contacts, groups,
			    messageFactory.getClientType(clientType).message(client, camp, null), "");
		}
	}
	
	private boolean isEligible(Map<String, String> data) {
		boolean status = false;
		if (data.get("alertStatus").equalsIgnoreCase(normal.name())) {
			if (DateUtil.dateDiff(data.get("expiryDate")) == 0) {
				status = true;
			}
			
		} else if (data.get("alertStatus").equalsIgnoreCase(upcoming.name())) {
			if (DateUtil.dateDiff(data.get("startDate")) == 0) {
				status = true;
			}
		}
		return status;
	}
	
	private String addExtensionToMobile(String mobile) {
		if (mobile.length() == 10) {
			mobile = "+880" + mobile;
			
		} else if (mobile.length() > 10) {
			mobile = mobile.substring(mobile.length() - 10);
			mobile = "+880" + mobile;
		} else {
			
			throw new IllegalArgumentException("invalid mobile no!!");
		}
		return mobile;
		
	}
	
}
