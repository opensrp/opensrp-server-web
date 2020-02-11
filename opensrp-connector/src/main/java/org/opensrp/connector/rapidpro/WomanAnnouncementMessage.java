package org.opensrp.connector.rapidpro;

import java.util.Map;

import org.opensrp.domain.Camp;
import org.opensrp.domain.Client;

public class WomanAnnouncementMessage implements Message {
	
	@Override
	public String message(Client client, Camp camp, Map<String, String> data) {
		
		String message = "Ajke " + camp.getCenterName() + " -e Tika deya hobe. Tika newar jonno joto taratari shombhob kendre chole ashun.";
		return message;
		
	}
	
}
