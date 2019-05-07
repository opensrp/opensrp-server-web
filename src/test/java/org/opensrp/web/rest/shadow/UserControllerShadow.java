package org.opensrp.web.rest.shadow;

import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.controller.UserController;
import org.opensrp.web.security.DrishtiAuthenticationProvider;

public class UserControllerShadow extends UserController {

	public UserControllerShadow(OpenmrsLocationService openmrsLocationService, OpenmrsUserService openmrsUserService,
			DrishtiAuthenticationProvider opensrpAuthenticationProvider) {
		super(openmrsLocationService, openmrsUserService, opensrpAuthenticationProvider);
	}

	public UserControllerShadow() {
		super(null, null, null);
	}

}
