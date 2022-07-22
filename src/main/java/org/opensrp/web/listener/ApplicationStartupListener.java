package org.opensrp.web.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    public static final String APPLICATION_ID = "/opensrp";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        System.out.println(contextRefreshedEvent.getApplicationContext().getId());
        if (contextRefreshedEvent.getApplicationContext().getId().endsWith(APPLICATION_ID)) {
//            scheduler.startJob(eventsSchedule);
//            scheduler.startJob(atomfeedSchedule);
//            scheduler.startJob(encounterSchedule);
//            scheduler.startJob(dhis2Schedule);
//            scheduler.startJob(validateSyncedToOMRS);
            System.out.println("STARTED ALL SCHEDULES");
        }
    }

}
