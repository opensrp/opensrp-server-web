package org.opensrp.web.listener;

import org.opensrp.service.rapidpro.RapidProService;
import org.opensrp.service.rapidpro.ZeirRapidProStateService;
import org.opensrp.util.constants.RapidProConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Profile("rapidpro")
@Service
@EnableScheduling
public class RapidProListener {

	private RapidProService rapidProService;

	private ZeirRapidProStateService rapidProStateService;

	@Value("#{opensrp['rapidpro.project']}")
	private String rapidProProject;

	@Autowired
	public void setRapidProService(RapidProService rapidProService) {
		this.rapidProService = rapidProService;
	}

	@Autowired
	public void setRapidProStateService(ZeirRapidProStateService rapidProStateService) {
		this.rapidProStateService = rapidProStateService;
	}

	public void requestRapidProContacts() {
		rapidProService.queryContacts(() -> {
			if (RapidProConstants.RapidProProjects.ZEIR_RAPIDPRO.equalsIgnoreCase(rapidProProject)) {
				rapidProStateService.postDataToRapidPro();
			}
		});
	}
}
