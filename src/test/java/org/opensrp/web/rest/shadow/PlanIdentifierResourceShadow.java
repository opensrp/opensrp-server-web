package org.opensrp.web.rest.shadow;

import org.opensrp.service.PlanService;
import org.opensrp.web.rest.PlanIdentifierResource;

/**
 * Created by Brian Mwasi
 */
public class PlanIdentifierResourceShadow extends PlanIdentifierResource {

	@Override
	public void setPlanService(PlanService planService) {
		super.setPlanService(planService);
	}

}
