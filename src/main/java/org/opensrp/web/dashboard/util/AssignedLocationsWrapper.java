package org.opensrp.web.dashboard.util;

import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.PhysicalLocation;

public class AssignedLocationsWrapper extends AssignedLocations{
	private PhysicalLocation physicalLocation;

	public AssignedLocationsWrapper() {}
	
	public AssignedLocationsWrapper(AssignedLocations assignedLocation, PhysicalLocation physicalLocation){
		setFromDate(assignedLocation.getFromDate());
		setToDate(assignedLocation.getToDate());
		setJurisdictionId(assignedLocation.getJurisdictionId());
		setPlanId(assignedLocation.getPlanId());
		setOrganizationId(assignedLocation.getOrganizationId());
		
		this.setPhysicalLocation(physicalLocation);
	}
	
	@Override
	public String toString() {
		return getPhysicalLocation() == null ? getJurisdictionId() : getPhysicalLocation().getProperties().getName();
	}

	public PhysicalLocation getPhysicalLocation() {
		return physicalLocation;
	}

	public void setPhysicalLocation(PhysicalLocation physicalLocation) {
		this.physicalLocation = physicalLocation;
	}
}
