package org.opensrp.web.rest.shadow.v2;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.opensrp.service.PhysicalLocationService;
import org.opensrp.service.SettingService;
import org.opensrp.web.rest.v2.SettingResource;
import org.springframework.stereotype.Component;

@Component
public class SettingsResourceShadow extends SettingResource {

    @Override
    public void setSettingService(SettingService settingService, PhysicalLocationService physicalLocationService) {
        super.setSettingService(settingService, physicalLocationService);
    }

    @Override
    public void setObjectMapper(ObjectMapper objectMapper) {
        super.setObjectMapper(objectMapper);
    }

}
