/**
 * 
 */
package org.opensrp.web.rest.shadow;

import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.rest.UserResource;

/**
 * @author Samuel Githengi created on 10/14/19
 */
public class UserResourceShadow extends UserResource {
	
	@Override
	public void setUserService(OpenmrsUserService userService) {
		super.setUserService(userService);
	};
	
}
