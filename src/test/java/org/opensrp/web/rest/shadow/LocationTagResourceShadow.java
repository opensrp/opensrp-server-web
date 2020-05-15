package org.opensrp.web.rest.shadow;

import org.opensrp.service.LocationTagService;
import org.opensrp.web.rest.LocationTagResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LocationTagResourceShadow extends LocationTagResource {
	
	@Override
	public void setLocationTagService(LocationTagService locationTagService) {
		super.setLocationTagService(locationTagService);
	}
	
	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}
	
}
