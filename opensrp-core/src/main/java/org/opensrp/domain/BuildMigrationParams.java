package org.opensrp.domain;

import java.util.List;
import java.util.Map;

public class BuildMigrationParams {
	
	private Client inClient;
	
	private Address outAddress;
	
	private Map<String, Object> outClientAttributes;
	
	private String outMemberId;
	
	private Map<String, List<String>> outClientRelationship;
	
	private Client inHHousehold;
	
	private Client outHHousehold;
	
	private String inProvider;
	
	private String outProvider;
	
	private String inHHRelationalId;
	
	private String outHHRelationalId;
	
	private String branchIdIn;
	
	private String branchIdOut;
	
	private String type;
	
	private UserLocationTableName oldUserLocation;
	
	private UserLocationTableName newUserLocation;
	
	private String isMember;
	
	public Client getInClient() {
		return inClient;
	}
	
	public void setInClient(Client inClient) {
		this.inClient = inClient;
	}
	
	public Address getOutAddress() {
		return outAddress;
	}
	
	public void setOutAddress(Address outAddress) {
		this.outAddress = outAddress;
	}
	
	public Map<String, Object> getOutClientAttributes() {
		return outClientAttributes;
	}
	
	public void setOutClientAttributes(Map<String, Object> outClientAttributes) {
		this.outClientAttributes = outClientAttributes;
	}
	
	public String getOutMemberId() {
		return outMemberId;
	}
	
	public void setOutMemberId(String outMemberId) {
		this.outMemberId = outMemberId;
	}
	
	public Map<String, List<String>> getOutClientRelationship() {
		return outClientRelationship;
	}
	
	public void setOutClientRelationship(Map<String, List<String>> outClientRelationship) {
		this.outClientRelationship = outClientRelationship;
	}
	
	public Client getInHHousehold() {
		return inHHousehold;
	}
	
	public void setInHHousehold(Client inHHousehold) {
		this.inHHousehold = inHHousehold;
	}
	
	public Client getOutHHousehold() {
		return outHHousehold;
	}
	
	public void setOutHHousehold(Client outHHousehold) {
		this.outHHousehold = outHHousehold;
	}
	
	public String getInProvider() {
		return inProvider;
	}
	
	public void setInProvider(String inProvider) {
		this.inProvider = inProvider;
	}
	
	public String getOutProvider() {
		return outProvider;
	}
	
	public void setOutProvider(String outProvider) {
		this.outProvider = outProvider;
	}
	
	public String getInHHRelationalId() {
		return inHHRelationalId;
	}
	
	public void setInHHRelationalId(String inHHRelationalId) {
		this.inHHRelationalId = inHHRelationalId;
	}
	
	public String getOutHHRelationalId() {
		return outHHRelationalId;
	}
	
	public void setOutHHRelationalId(String outHHRelationalId) {
		this.outHHRelationalId = outHHRelationalId;
	}
	
	public String getBranchIdIn() {
		return branchIdIn;
	}
	
	public void setBranchIdIn(String branchIdIn) {
		this.branchIdIn = branchIdIn;
	}
	
	public String getBranchIdOut() {
		return branchIdOut;
	}
	
	public void setBranchIdOut(String branchIdOut) {
		this.branchIdOut = branchIdOut;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public UserLocationTableName getOldUserLocation() {
		return oldUserLocation;
	}
	
	public void setOldUserLocation(UserLocationTableName oldUserLocation) {
		this.oldUserLocation = oldUserLocation;
	}
	
	public UserLocationTableName getNewUserLocation() {
		return newUserLocation;
	}
	
	public void setNewUserLocation(UserLocationTableName newUserLocation) {
		this.newUserLocation = newUserLocation;
	}
	
	public String getIsMember() {
		return isMember;
	}
	
	public void setIsMember(String isMember) {
		this.isMember = isMember;
	}
	
}
