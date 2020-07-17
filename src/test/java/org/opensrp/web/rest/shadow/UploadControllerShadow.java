package org.opensrp.web.rest.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensrp.repository.MultimediaRepository;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.IdentifierSourceService;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.UniqueIdentifierService;
import org.opensrp.service.UploadService;
import org.opensrp.web.rest.UploadController;
import org.springframework.stereotype.Component;

@Component
public class UploadControllerShadow extends UploadController {

	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}

	@Override
	public void setMultimediaService(MultimediaService multimediaService) {
		super.setMultimediaService(multimediaService);
	}

	@Override
	public void setUploadService(UploadService uploadService) {
		super.setUploadService(uploadService);
	}

	@Override
	public void setMultimediaRepository(MultimediaRepository multimediaRepository) {
		super.setMultimediaRepository(multimediaRepository);
	}

	@Override
	public void setIdentifierSourceService(IdentifierSourceService identifierSourceService) {
		super.setIdentifierSourceService(identifierSourceService);
	}

	@Override
	public void setUniqueIdentifierService(UniqueIdentifierService uniqueIdentifierService) {
		super.setUniqueIdentifierService(uniqueIdentifierService);
	}

	@Override
	public void setClientService(ClientService clientService) {
		super.setClientService(clientService);
	}

	@Override
	public void setEventService(EventService eventService) {
		super.setEventService(eventService);
	}
}
