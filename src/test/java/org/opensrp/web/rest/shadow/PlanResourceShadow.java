package org.opensrp.web.rest.shadow;

import org.opensrp.service.EventService;
import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.PlanService;
import org.opensrp.service.PlanProcessingStatusService;
import org.opensrp.service.TemplateService;
import org.opensrp.web.rest.PlanResource;
import org.springframework.stereotype.Component;

/**
 * Created by Vincent Karuri on 10/05/2019
 */
@Component
public class PlanResourceShadow extends PlanResource {

    @Override
    public void setPlanService(PlanService planService) {
        super.setPlanService(planService);
    }

    @Override
    public void setLocationService(PhysicalLocationService locationService) {
        super.setLocationService(locationService);
    }

    @Override
    public void setEventService(EventService eventService) {
        super.setEventService(eventService);
    }

    @Override
    public void setProcessingStatusService(PlanProcessingStatusService processingStatusService) {
        super.setProcessingStatusService(processingStatusService);
    }

    @Override
    public void setTemplateService(TemplateService templateService) {
        super.setTemplateService(templateService);
    }
}
