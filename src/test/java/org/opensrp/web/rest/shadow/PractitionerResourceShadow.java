package org.opensrp.web.rest.shadow;

import org.opensrp.service.PractitionerService;
import org.opensrp.web.rest.PractitionerResource;

public class PractitionerResourceShadow extends PractitionerResource {

    @Override
    public void setPractitionerService(PractitionerService practitionerService) {
        super.setPractitionerService(practitionerService);
    }
}