package org.opensrp.web.rest.shadow;

import org.opensrp.service.SettingService;
import org.opensrp.web.rest.SettingResource;
import org.springframework.stereotype.Component;

@Component
public class SettingResourceShadow extends SettingResource {
	
	@Override
	public void setSettingService(SettingService settingService) {
		super.setSettingService(settingService);
	}
	
}
