package org.opensrp.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Vincent Karuri on 26/11/2020
 */

@SpringBootApplication
@MapperScan("org.opensrp.repository.postgres.mapper")
@ComponentScan(basePackages = {"org.opensrp"}, excludeFilters={
		@ComponentScan.Filter(type= FilterType.REGEX, pattern={
				"org.opensrp.repository.couch.*",
				"org.opensrp.repository.lucene.*",
				"org.opensrp.scheduler.repository.couch.*",
				"org.opensrp.service.formSubmission.FormSubmission..*",
				"org.opensrp.service.formSubmission.*",
				"org.opensrp.service.FormSubmissionDataMigrationService",
				"org.opensrp.referrals.*",
				"org.opensrp.connector.dhis2.DHIS2SyncerListener",
				"org.opensrp.connector.dhis2.VaccinationTracker",
				"org.opensrp.connector.openmrs.schedule.OpenmrsSyncerListener",
				"org.opensrp.connector.openmrs.schedule.OpenmrsValidateDataSync"
		})})
@ImportResource({"classpath:spring/applicationContext-opensrp-web.xml"})
public class OpenSRPWebApp {
	public static void main(String[] args) {
		SpringApplication.run(OpenSRPWebApp.class, args);
	}
}
