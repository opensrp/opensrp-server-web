package org.opensrp.web.rest.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensrp.service.ClientFormService;
import org.opensrp.web.rest.ClientFormResource;
import org.springframework.stereotype.Component;

@Component
public class ClientFormResourceShadow extends ClientFormResource {

	@Override
	public void setClientFormService(ClientFormService clientFormService) {
		super.setClientFormService(clientFormService);
	}


	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}

}
