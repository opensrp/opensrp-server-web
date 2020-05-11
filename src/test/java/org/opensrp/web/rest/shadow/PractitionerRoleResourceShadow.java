package org.opensrp.web.rest.shadow;

import org.opensrp.service.PractitionerRoleService;
import org.opensrp.web.rest.PractitionerRoleResource;
import org.springframework.stereotype.Component;

@Component
public class PractitionerRoleResourceShadow extends PractitionerRoleResource {

    @Override
    public void setPractitionerRoleService(PractitionerRoleService practitionerRoleService) {
        super.setPractitionerRoleService(practitionerRoleService);
    }
}
