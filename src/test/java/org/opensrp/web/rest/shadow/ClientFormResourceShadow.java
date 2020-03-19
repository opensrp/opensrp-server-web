package org.opensrp.web.rest.shadow;

import org.opensrp.service.ClientFormService;
import org.opensrp.web.rest.ClientFormResource;

public class ClientFormResourceShadow extends ClientFormResource {
	
	@Override
	public void setClientFormService(ClientFormService clientFormService) {
		super.setClientFormService(clientFormService);
	}

}
