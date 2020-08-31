package org.opensrp.web.listener;

import org.opensrp.connector.openmrs.FetchLocationsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {
	
	private static Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class.toString());
	
	public static final String APPLICATION_ID = "/opensrp";
	
	@Autowired
	private FetchLocationsHelper fetchLocationsHelper;
	
	@Value("#{opensrp['openmrs.location.cache.enabled'] ?: false }")
	private boolean cacheLocationEnabled;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		logger.info(contextRefreshedEvent.getApplicationContext().getId());
		if (contextRefreshedEvent.getApplicationContext().getId().endsWith(APPLICATION_ID + APPLICATION_ID)
		        && cacheLocationEnabled) {
			
			new Thread() {
				
				@Override
				public void run() {
					
					logger.info("Populating OpenMRS location cache");
					
					fetchLocationsHelper.getAllOpenMRSlocations();
					
					logger.info("Completed populating OpenMRS location cache");
				}
			}.start();
			
		}
	}
}
