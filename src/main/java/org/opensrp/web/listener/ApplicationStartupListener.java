package org.opensrp.web.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.opensrp.connector.openmrs.service.OpenmrsLocationService;
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
	
	@Value("#{opensrp['openmrs.location.cache.uuids']}")
	private String cacheLocationIds;
	
	@Value("#{opensrp['openmrs.location.cache.level'] ?: 'District'}")
	private String cacheLocationLevel;
	
	@Value("#{opensrp['openmrs.location.cache.tags'] ?: 'MOH Jhpiego Facility Name,Health Facility,Facility'}")
	private String cacheLocationTag;
	
	@Autowired
	private OpenmrsLocationService openmrsLocationService;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		logger.info(contextRefreshedEvent.getApplicationContext().getId());
		if (contextRefreshedEvent.getApplicationContext().getId().endsWith(APPLICATION_ID + APPLICATION_ID)
		        && StringUtils.isNotBlank(cacheLocationIds)) {
			ExecutorService executor = Executors.newFixedThreadPool(5);
			String[] locations = cacheLocationIds.split(",");
			String[] locationTags = cacheLocationTag.split(",");
			for (String id : locations) {
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						JSONArray tags = new JSONArray(locationTags);
						logger.info(String.format("populating OpenMRS location cache for %s, %s, %s ", id,
						    cacheLocationLevel, tags));
						openmrsLocationService.getLocationsByLevelAndTags(id, cacheLocationTag, tags);
						logger.info(String.format("completed populating OpenMRS location cache for %s, %s, %s ", id,
						    cacheLocationLevel, tags));
						
					}
				});
			}
		}
	}
}
