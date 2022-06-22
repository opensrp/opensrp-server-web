package org.opensrp.web.rest.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.opensrp.service.ClientFormService;
import org.opensrp.service.ManifestService;
import org.opensrp.web.rest.ManifestResource;
import org.springframework.stereotype.Component;

@Component
public class ManifestResourceShadow extends ManifestResource {

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
    }

    @Override
    public void setManifestService(ManifestService manifestService) {
        super.setManifestService(manifestService);
    }

    @Override
    public void setClientFormService(ClientFormService clientFormService) {
        super.setClientFormService(clientFormService);
    }

}
