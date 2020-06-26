package org.opensrp.web.rest.v2.shadow;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
import org.opensrp.service.LocationTagService;
import org.opensrp.service.SettingService;
import org.opensrp.web.rest.LocationTagResource;
import org.opensrp.web.rest.v2.SettingResource;
import org.springframework.stereotype.Component;

@Component
public class SettingsResourceShadow extends SettingResource {
	
	@Override
	public void setSettingService(SettingService settingService, OpenmrsLocationService openmrsLocationService) {
		super.setSettingService(settingService, openmrsLocationService);
	}
	
	@Override
	public void setObjectMapper(ObjectMapper objectMapper) {
		super.setObjectMapper(objectMapper);
	}
	
}
