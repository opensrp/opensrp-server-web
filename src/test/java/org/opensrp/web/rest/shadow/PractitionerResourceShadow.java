package org.opensrp.web.rest.shadow;

import org.opensrp.service.PractitionerService;
import org.opensrp.web.rest.PractitionerResource;
import org.springframework.stereotype.Component;

@Component
public class PractitionerResourceShadow extends PractitionerResource {

    @Override
    public void setPractitionerService(PractitionerService practitionerService) {
        super.setPractitionerService(practitionerService);
    }
}