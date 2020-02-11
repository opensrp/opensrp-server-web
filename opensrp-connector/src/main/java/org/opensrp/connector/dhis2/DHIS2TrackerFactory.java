package org.opensrp.connector.dhis2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DHIS2TrackerFactory {
	
	@Autowired
	private HouseholdTracker householdTracker;
	
	@Autowired
	private MotherTracker motherTracker;
	
	@Autowired
	private ChildTracker childTracker;
	
	private DHIS2Tracker dhis2Tracker;
	
	public DHIS2TrackerFactory() {
		
	}
	
	public DHIS2Tracker getTracker(TrackerType tracker) {
		
		if (tracker == TrackerType.HOUSEHOLD) {
			
			dhis2Tracker = householdTracker;
			System.err.println("tracker:" + tracker + ":" + householdTracker);
		} else if (tracker == TrackerType.MOTHER) {
			dhis2Tracker = motherTracker;
			System.err.println("tracker:" + tracker + ":" + motherTracker);
		} else if (tracker == TrackerType.CHILD) {
			dhis2Tracker = childTracker;
			System.err.println("tracker:" + tracker + ":" + childTracker);
		} else {
			
		}
		
		return dhis2Tracker;
		
	}
	
}
