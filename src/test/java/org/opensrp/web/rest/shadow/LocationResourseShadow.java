package org.opensrp.web.rest.shadow;

import org.opensrp.service.PhysicalLocationService;
import org.opensrp.web.rest.LocationResource;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LocationResourseShadow extends LocationResource {

	@Override
	public void setLocationService(PhysicalLocationService locationService) {
		super.setLocationService(locationService);
	}
	
	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}

}
