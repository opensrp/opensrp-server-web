package org.opensrp.web.it.listener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.impl.StdObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.opensrp.connector.domain.Camp;
import org.opensrp.connector.repository.couch.AllCamp;

public class CreateNewCamp {
	
	private CouchDbInstance dbInstance;
	
	private StdCouchDbConnector stdCouchDbConnector;
	
	private AllCamp allCamp;
	
	@Before
	public void setUp() throws Exception {
		HttpClient httpClient = new StdHttpClient.Builder().host("localhost").port(5984).username("rootuser")
		        .password("adminpass").socketTimeout(10000000).build();
		dbInstance = new StdCouchDbInstance(httpClient);
		stdCouchDbConnector = new StdCouchDbConnector("opensrp", dbInstance, new StdObjectMapperFactory());
		stdCouchDbConnector.createDatabaseIfNotExists();
		allCamp = new AllCamp(stdCouchDbConnector);
		
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
