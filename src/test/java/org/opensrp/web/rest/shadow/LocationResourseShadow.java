package org.opensrp.web.rest.shadow;

import org.opensrp.connector.dhis2.location.DHIS2ImportLocationsStatusService;
import org.opensrp.connector.dhis2.location.DHIS2ImportOrganizationUnits;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.web.rest.LocationResource;
import org.springframework.stereotype.Component;

@Component
public class LocationResourseShadow extends LocationResource {

	@Override
	public void setLocationService(PhysicalLocationService locationService) {
		super.setLocationService(locationService);
	}
	
	@Override
	public void setPlanService(PlanService planService) {
		super.setPlanService(planService);
	}
	
	
	@Override
	public void setDhis2ImportLocationsStatusService(DHIS2ImportLocationsStatusService dhis2ImportLocationsStatusService) {
		super.setDhis2ImportLocationsStatusService(dhis2ImportLocationsStatusService);
	}
	
	@Override
	public void setDhis2ImportOrganizationUnits(DHIS2ImportOrganizationUnits dhis2ImportOrganizationUnits) {
		super.setDhis2ImportOrganizationUnits(dhis2ImportOrganizationUnits);
	}

}
