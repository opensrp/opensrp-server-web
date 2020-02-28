/**
 * 
 */
package org.opensrp.web.it.listener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.domain.Camp;
import org.opensrp.repository.couch.AllCamp;
import org.opensrp.repository.couch.AllClients;
import org.opensrp.scheduler.repository.couch.AllActions;
import org.opensrp.web.listener.RapidproMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author proshanto
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/applicationContext-opensrp-web.xml" })
public class ClientListenerTest {
	
	@Autowired
	private AllClients allClients;
	
	@Autowired
	private AllActions allActions;
	
	@Autowired
	private RapidproMessageListener rapidProMessageListener;
	
	@Autowired
	private AllCamp allCamp;
	
	@Before
	public void setup() throws IOException {
		//allClients.removeAll();
		//	allActions.removeAll();
	}
	
	@Ignore
	@Test
	public void testFetchClient() throws JSONException {
		
		/*		Client child = (Client) new Client("127").withFirstName("foomm").withGender("female").withLastName("bae ff")
				        .withBirthdate(new DateTime(), false).withDateCreated(new DateTime());
				
				List<String> motherRelationshipsList = new ArrayList<>();
				motherRelationshipsList.add("130");
				Map<String, List<String>> motherRelationships = new HashMap<>();
				motherRelationships.put("mother", motherRelationshipsList);
				child.setRelationships(motherRelationships);
				
				allClients.add(child);
				
				Client mother = (Client) new Client("130").withFirstName("foorrr").withGender("female").withLastName("bae ff")
				        .withBirthdate(new DateTime(), false).withDateCreated(new DateTime());
				
				Map<String, Object> motherAttributes = new HashMap<>();
				
				motherAttributes.put("phoneNumber", "01711082537");
				motherAttributes.put("nationalId", "76543222349775");
				motherAttributes.put("spouseName", "Dion");
				mother.setAttributes(motherAttributes);
				allClients.add(mother);
				Action normalAction = new Action("127", "ANM 1", ActionData.createAlert("child", "opv", "opv0", normal,
				    DateTime.now(), DateTime.now().plusDays(3)));
				Action upcominglAction = new Action("127", "ANM 1", ActionData.createAlert("child", "opv", "opv0", upcoming,
				    DateTime.now(), DateTime.now().plusDays(3)));
				allActions.add(normalAction);
				allActions.add(upcominglAction);*/
		//rapidProMessageListener.fetchClient();
		
		rapidProMessageListener.campAnnouncementListener("raihan");
		
	}
	
	@Test
	public void createCamp() throws FileNotFoundException {
		String csvFile = "/home/sohel/workspace-3.6.1/opensrp-server/CreateNewCamp.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		int lineNumber = 0;
		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				if (lineNumber == 0) {
					lineNumber++;
					continue;
				}
				String[] updateObject = line.split(cvsSplitBy);
				String providerName = updateObject[0].trim();
				String date = updateObject[1].trim();
				String campName = updateObject[2].trim();
				String centerName = updateObject[3].trim();
				boolean status = Boolean.parseBoolean(updateObject[4].trim());
				Camp camp = new Camp();
				camp.setProviderName(providerName);
				camp.setDate(date);
				camp.setCampName(campName);
				camp.setCenterName(centerName);
				camp.setStatus(status);
				allCamp.add(camp);
				System.err.println("successfully defined camp: " + campName);
				lineNumber++;
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
