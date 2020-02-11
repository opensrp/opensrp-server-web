package org.opensrp.connector.rapidpro;

import java.util.Map;

import org.opensrp.domain.Camp;
import org.opensrp.domain.Client;

public class ChildAnnouncementMessage implements Message {
	
	@Override
	public String message(Client client, Camp camp, Map<String, String> data) {
		String message = "Ajke " + camp.getCenterName() + " -e Tika deya hobe. Apnar shishuke tika deyar jonno taratari kendre niye ashun.";		
		return message;
		
	}
	
}
