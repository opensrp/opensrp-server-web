package org.opensrp.web.rest.shadow;

import org.opensrp.service.PhysicalLocationService;
import org.opensrp.web.rest.LocationResource;
import org.springframework.stereotype.Component;

@Component
public class LocationResourseShadow extends LocationResource {

	@Override
	public void setLocationService(PhysicalLocationService locationService) {
		super.setLocationService(locationService);
	}

}
