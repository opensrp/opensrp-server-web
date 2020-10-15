package org.opensrp.web.rest.shadow;

import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.controller.UserController;
import org.springframework.stereotype.Component;

@Component
public class UserControllerShadow extends UserController {
	
	@Override
	public void setPractitionerService(PractitionerService practitionerService) {
		super.setPractitionerService(practitionerService);
	}
	
	@Override
	public void setOrganizationService(OrganizationService organizationService) {
		super.setOrganizationService(organizationService);
	}
	
	@Override
	public void setLocationService(PhysicalLocationService locationService) {
		super.setLocationService(locationService);
	}
	
	@Override
	public void setPlanService(PlanService planService) {
		super.setPlanService(planService);
	}
}
