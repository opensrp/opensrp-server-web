package org.opensrp.web.rest.shadow;

import org.opensrp.service.PlanService;
import org.opensrp.web.rest.PlanResource;

/**
 * Created by Vincent Karuri on 10/05/2019
 */
public class PlanResourceShadow extends PlanResource {

    @Override
    public void setPlanService(PlanService planService) {
        super.setPlanService(planService);
    }
}
