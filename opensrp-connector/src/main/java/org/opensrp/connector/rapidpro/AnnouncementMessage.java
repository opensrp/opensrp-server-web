package org.opensrp.connector.rapidpro;

public class AnnouncementMessage extends MessageFactory {
	
	@Override
	public Message getClientType(ClientType type) {
		// TODO Auto-generated method stub
		Message message = null;
		if (type == ClientType.mother) {
			message = new WomanAnnouncementMessage();
		} else if (type == ClientType.child) {
			message = new ChildAnnouncementMessage();
		} else {}
		return message;
	}
	
}
