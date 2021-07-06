package org.opensrp.web.listener;

import org.opensrp.service.ConfigService;
import org.opensrp.service.RapidProService;
import org.opensrp.web.Constants.AppStateToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Profile("rapidpro")
@Service
@EnableScheduling
public class RapidProListener {

	private RapidProService rapidProService;

	private ConfigService configService;

	@Autowired
	public void setRapidProService(RapidProService rapidProService) {
		this.rapidProService = rapidProService;
	}

	@Autowired
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
		this.configService.registerAppStateToken(AppStateToken.RAPIDPRO_STATE_TOKEN, null,
				"Token to keep track of the date of the last processed rapidpro contacts", true);
	}

	public void requestRapidProContacts() {
		rapidProService.queryContacts((String) this.configService.getAppStateTokenByName(AppStateToken.RAPIDPRO_STATE_TOKEN)
				.getValue());
	}
}
