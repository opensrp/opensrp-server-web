package org.opensrp.web.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.domain.postgres.Structure;
import org.opensrp.repository.StructureCreateOrUpdateEvent;
import org.opensrp.service.PhysicalLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StructureRepositoryEventListener  implements ApplicationListener<StructureCreateOrUpdateEvent> {
	private static final Logger logger = LogManager.getLogger(StructureRepositoryEventListener.class.toString());
	@Autowired
	private PhysicalLocationService physicalLocationService;
	@Override
	public void onApplicationEvent(StructureCreateOrUpdateEvent structureCreateOrUpdateEvent) {
		Structure structure = (Structure) structureCreateOrUpdateEvent.getSource();
		logger.info("Receiving Structure Event");
		physicalLocationService.regenerateTasksForOperationalArea(structure);
	}
}
