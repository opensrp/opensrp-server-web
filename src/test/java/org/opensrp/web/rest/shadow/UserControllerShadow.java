package org.opensrp.web.rest.shadow;

import org.opensrp.service.OrganizationService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PractitionerService;
import org.opensrp.web.controller.UserController;
import org.opensrp.web.custom.service.CustomUserService;
import org.opensrp.web.security.DrishtiAuthenticationProvider;

public class UserControllerShadow extends UserController {

	public UserControllerShadow(CustomUserService userService,
								DrishtiAuthenticationProvider opensrpAuthenticationProvider) {
		super(opensrpAuthenticationProvider, userService);
	}

	public UserControllerShadow() {
		super(null, null);
	}

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
}
