package org.opensrp.web.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.service.rapidpro.RapidProService;
import org.opensrp.service.rapidpro.ZeirRapidProStateService;
import org.opensrp.util.constants.RapidProConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("rapidpro")
@Component
public class RapidProListener {

	private final Logger logger = LogManager.getLogger(getClass());

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
		rapidProService.queryContacts(() -> logger.info("Completed processing RapidPro contacts"));
		if (RapidProConstants.RapidProProjects.ZEIR_RAPIDPRO.equalsIgnoreCase(rapidProProject)) {
			synchronized (this) {
				rapidProStateService.postDataToRapidPro();
			}
		}
	}
}
