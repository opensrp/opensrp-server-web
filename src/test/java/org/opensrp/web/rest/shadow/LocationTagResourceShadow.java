package org.opensrp.web.rest.shadow;

import org.opensrp.service.LocationTagService;
import org.opensrp.web.rest.LocationTagResource;

public class LocationTagResourceShadow extends LocationTagResource {
	
	@Override
	public void setLocationTagService(LocationTagService locationTagService) {
		super.setLocationTagService(locationTagService);
	}
}
