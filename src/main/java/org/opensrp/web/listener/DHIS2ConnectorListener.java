package org.opensrp.web.listener;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.opensrp.connector.dhis2.DHIS2AggregateConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Profile("dhis2-sync-vaccine-tracker")
@Service
@EnableScheduling
public class DHIS2ConnectorListener {
	
	private static Logger logger = LogManager.getLogger(DHIS2ConnectorListener.class.toString());
	
	@Autowired
	private DHIS2AggregateConnector dHIS2AggregateConnector;
	
	public void vaccinationAggregatorDataCountForSendingToDHIS2() {
		logger.info("Listener called for SendingToDHIS2");
		Calendar c = Calendar.getInstance();
		if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
			JSONObject aggregatedDataSet = null;
			String message = "";
			try {
				aggregatedDataSet = dHIS2AggregateConnector.getAggregatedDataCount();
				dHIS2AggregateConnector.aggredateDataSendToDHIS2(aggregatedDataSet);
				message = aggregatedDataSet.toString();
				System.out.println("Aggregated data send to DHIS2..." + aggregatedDataSet.toString());
				
			}
			catch (Exception e) {
				System.out.println("Aggregate Data Count Error Message" + e.getMessage());
				
			}
		} else {
			System.out.println("This in not last day of the month");
		}
		
	}
	
}
