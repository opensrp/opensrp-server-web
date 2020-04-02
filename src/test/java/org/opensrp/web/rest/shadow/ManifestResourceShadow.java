package org.opensrp.web.rest.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensrp.service.ManifestService;
import org.opensrp.web.rest.ManifestResource;

public class ManifestResourceShadow extends ManifestResource {

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
    }

    @Override
    public void setManifestService(ManifestService manifestService) {
        super.setManifestService(manifestService);
    }

}
