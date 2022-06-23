/**
 *
 */
package org.opensrp.web.rest.shadow;

import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.rest.UserResource;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 10/14/19
 */
@Component
public class UserResourceShadow extends UserResource {

    @Override
    public void setUserService(OpenmrsUserService userService) {
        super.setUserService(userService);
    }

}
