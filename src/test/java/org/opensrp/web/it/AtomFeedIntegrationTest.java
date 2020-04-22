package org.opensrp.web.it;

import java.io.IOException;
import java.net.URISyntaxException;

import org.ektorp.CouchDbConnector;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.connector.atomfeed.AllFailedEventsCouchImpl;
import org.opensrp.connector.atomfeed.AllMarkersCouchImpl;
import org.opensrp.connector.openmrs.EncounterAtomfeed;
import org.opensrp.connector.openmrs.PatientAtomfeed;
import org.opensrp.connector.openmrs.constants.OpenmrsConstants;
import org.opensrp.repository.postgres.ClientsRepositoryImpl;
import org.opensrp.repository.postgres.EventsRepositoryImpl;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.web.utils.TestResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-opensrp-web.xml")
public class AtomFeedIntegrationTest extends TestResourceLoader {

	@Autowired
	@Qualifier(OpenmrsConstants.ATOMFEED_DATABASE_CONNECTOR)
	CouchDbConnector cdb;
	
	@Autowired
	ClientService clientService;
	
	@Autowired
	ClientsRepositoryImpl allClients;
	
	@Autowired
	EventService eventService;
	
	@Autowired
	EventsRepositoryImpl allEvents;

	@Autowired
	private AllMarkersCouchImpl allMarkers;

	@Autowired
	private AllFailedEventsCouchImpl allFailedEvents;
	
	
	@Before
	public void setup(){
		allClients.removeAll();
		allEvents.removeAll();
	}
	
	public AtomFeedIntegrationTest() throws IOException {
		super();
	}

	@Test
    public void shouldReadEventsCreatedEvents() throws URISyntaxException {
		PatientAtomfeed paf = new PatientAtomfeed(allMarkers, allFailedEvents, openmrsOpenmrsUrl, patientService, clientService, eventService);

		EncounterAtomfeed eaf = new EncounterAtomfeed(allMarkers, allFailedEvents, openmrsOpenmrsUrl, encounterService, eventService);
		if(pushToOpenmrsForTest){
			paf.processEvents();

			eaf.processEvents();
		}
    }
}
