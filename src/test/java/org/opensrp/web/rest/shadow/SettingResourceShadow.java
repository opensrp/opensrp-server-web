package org.opensrp.web.rest.shadow;

import org.opensrp.service.SettingService;
import org.opensrp.web.rest.SettingResource;

public class SettingResourceShadow extends SettingResource {
	
	@Override
	public void setSettingService(SettingService settingService) {
		super.setSettingService(settingService);
	}
	
}
