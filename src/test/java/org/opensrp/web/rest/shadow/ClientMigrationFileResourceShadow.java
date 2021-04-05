package org.opensrp.web.rest.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensrp.service.ClientFormService;
import org.opensrp.service.ClientMigrationFileService;
import org.opensrp.service.ManifestService;
import org.opensrp.web.controller.ClientMigrationFileResource;
import org.opensrp.web.rest.ClientFormResource;
import org.springframework.stereotype.Component;

@Component
public class ClientMigrationFileResourceShadow extends ClientMigrationFileResource {

	@Override
	public void setClientMigrationFileService(ClientMigrationFileService clientMigrationFileService) {
		super.setClientMigrationFileService(clientMigrationFileService);
	}

	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}

}
