package org.opensrp.web.rest.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensrp.service.TemplateService;
import org.opensrp.web.rest.TemplateResource;
import org.springframework.stereotype.Component;

@Component
public class TemplateResourceShadow extends TemplateResource {

    @Override
    public void setTemplateService(TemplateService templateService) {
        super.setTemplateService(templateService);
    }

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
    }
}
