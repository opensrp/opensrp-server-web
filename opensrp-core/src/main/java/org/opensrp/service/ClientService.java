package org.opensrp.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.common.MigrationStatus;
import org.opensrp.domain.Address;
import org.opensrp.domain.Client;
import org.opensrp.domain.ErrorTrace;
import org.opensrp.domain.Migration;
import org.opensrp.domain.UserLocationTableName;
import org.opensrp.domain.postgres.CustomQuery;
import org.opensrp.domain.postgres.StockInfo;
import org.opensrp.domain.postgres.TargetDetails;
import org.opensrp.domain.postgres.WebNotification;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {
	
	private final ClientsRepository allClients;
	
	private static Logger logger = LoggerFactory.getLogger(ClientService.class.toString());
	
	@Autowired
	public ClientService(ClientsRepository allClients) {
		this.allClients = allClients;
	}
	
	@Autowired
	private ErrorTraceService errorTraceService;
	
	public Client getByBaseEntityId(String baseEntityId, String table) {
		return allClients.findByBaseEntityId(baseEntityId, table);
	}
	
	public List<Client> findAllClients(String table) {
		return allClients.findAllClients(table);
	}
	
	public List<Client> findAllByIdentifier(String identifier, String table) {
		return allClients.findAllByIdentifier(identifier, table);
	}
	
	public List<Client> findAllByIdentifier(String identifierType, String identifier, String table) {
		return allClients.findAllByIdentifier(identifierType, identifier, table);
	}
	
	public List<Client> findByRelationshipIdAndDateCreated(String relationalId, String dateFrom, String dateTo, String table) {
		return allClients.findByRelationshipIdAndDateCreated(relationalId, dateFrom, dateTo, table);
	}
	
	public List<Client> findByRelationship(String relationalId, String table) {
		return allClients.findByRelationShip(relationalId, table);
	}
	
	public List<Client> findAllByAttribute(String attributeType, String attribute, String table) {
		return allClients.findAllByAttribute(attributeType, attribute, table);
	}
	
	public List<Client> findAllByMatchingName(String nameMatches, String table) {
		return allClients.findAllByMatchingName(nameMatches, table);
	}
	
	public List<Client> findByCriteria(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean,
	                                   DateTime lastEditFrom, DateTime lastEditTo, String table) {
		clientSearchBean.setLastEditFrom(lastEditFrom);
		clientSearchBean.setLastEditTo(lastEditTo);
		return allClients.findByCriteria(clientSearchBean, addressSearchBean, table);//db.queryView(q.includeDocs(true), Client.class);
	}
	
	public List<Client> findByCriteria(ClientSearchBean clientSearchBean, Long serverVersion, String table) {
		return allClients.findByCriteria(clientSearchBean, new AddressSearchBean(), table);
	}
	
	public List<Client> findByCriteria(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean, String table) {
		return allClients.findByCriteria(clientSearchBean, addressSearchBean, table);
	}
	
	/*	public List<Client> findByCriteria(String addressType, String country, String stateProvince, String cityVillage, String countyDistrict, 
				String  subDistrict, String town, String subTown, DateTime lastEditFrom, DateTime lastEditTo) {
			return allClients.findByCriteria(null, null, null, null, null, null, null, null, addressType, country, stateProvince, cityVillage, countyDistrict, subDistrict, town, subTown, lastEditFrom, lastEditTo);
		}*/
	
	public List<Client> findByDynamicQuery(String query) {
		return allClients.findByDynamicQuery(query);
	}
	
	public Client addClient(Client client, String table, String district, String division, String branch, String village) {
		if (client.getBaseEntityId() == null) {
			throw new RuntimeException("No baseEntityId");
		}
		Client c = findClient(client, table);
		if (c != null) {
			try {
				updateClient(client, table, district, division, branch, village);
			}
			catch (JSONException e) {
				throw new IllegalArgumentException(
				        "A client already exists with given list of identifiers. Consider updating data.[" + c + "]");
			}
		}
		
		client.setDateCreated(DateTime.now());
		allClients.add(client, table, district, division, branch, village);
		
		return client;
	}
	
	public Client findClient(Client client, String table) {
		// find by auto assigned entity id
		Client c = null;
		try {
			c = allClients.findByBaseEntityId(client.getBaseEntityId(), table);
			if (c != null) {
				return c;
			}
			
			//still not found!! search by generic identifiers
			logger.info("\n\n Client in findClient : " + client.toString() + "\n\n");
			logger.info("\n\n Identifiers : " + client.getIdentifiers() + "\n\n");
			
			//			for (String idt : client.getIdentifiers().keySet()) {
			//				if(!idt.equalsIgnoreCase("serial_no")){
			//					List<Client> cl = allClients.findAllByIdentifier(client.getIdentifier(idt));
			//					if (cl.size() > 1) {
			//						throw new IllegalArgumentException("Multiple clients with identifier type " + idt + " and ID "
			//								+ client.getIdentifier(idt) + " exist.");
			//					} else if (cl.size() != 0) {
			//						return cl.get(0);
			//					}
			//				}
			//			}
			logger.info("\n\n Client after finding : " + client.toString() + "\n\n");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}
	
	public Client find(String uniqueId, String table) {
		// find by document id
		Client c = allClients.findByBaseEntityId(uniqueId, table);
		if (c != null) {
			return c;
		}
		
		// if not found find if it is in any identifiers TODO refactor it later
		List<Client> cl = allClients.findAllByIdentifier(uniqueId, table);
		if (cl.size() > 1) {
			throw new IllegalArgumentException("Multiple clients with identifier " + uniqueId + " exist.");
		} else if (cl.size() != 0) {
			return cl.get(0);
		}
		
		return c;
	}
	
	public void updateClient(Client updatedClient, String table, String district, String division, String branch,
	                         String village) throws JSONException {
		// If update is on original entity
		if (updatedClient.isNew()) {
			throw new IllegalArgumentException(
			        "Client to be updated is not an existing and persisting domain object. Update database object instead of new pojo");
		}
		
		if (findClient(updatedClient, table) == null) {
			throw new IllegalArgumentException("No client found with given list of identifiers. Consider adding new!");
		}
		
		updatedClient.setDateEdited(DateTime.now());
		allClients.update(updatedClient, table, district, division, branch, village);
	}
	
	public Client mergeClient(Client updatedClient, JSONObject relationship, String table, String district, String division,
	                          String branch, String village) {
		try {
			Client original = findClient(updatedClient, table);
			if (original == null) {
				throw new IllegalArgumentException("No client found with given list of identifiers. Consider adding new!");
			}
			
			original = (Client) Utils.getMergedJSON(original, updatedClient,
			    Arrays.asList(Client.class.getDeclaredFields()), Client.class);
			
			for (Address a : updatedClient.getAddresses()) {
				if (original.getAddress(a.getAddressType()) == null) {
					original.addAddress(a);
				} else {
					original.removeAddress(a.getAddressType());
					original.addAddress(a);
				}
			}
			for (String k : updatedClient.getIdentifiers().keySet()) {
				original.addIdentifier(k, updatedClient.getIdentifier(k));
			}
			for (String k : updatedClient.getAttributes().keySet()) {
				original.addAttribute(k, updatedClient.getAttribute(k));
			}
			if (relationship != null) {
				JSONObject personB = relationship.getJSONObject("personB");
				List<Client> clients = findAllByIdentifier("OPENMRS_UUID", personB.getString("uuid"));
				if (clients.size() != 0) {
					original.addRelationship("household", clients.get(0).getBaseEntityId());
				}
			}
			original.setDateEdited(DateTime.now());
			original.setServerVersion(System.currentTimeMillis());
			allClients.update(original, table, district, division, branch, village);
			return original;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<Client> findByServerVersion(long serverVersion, String table) {
		return allClients.findByServerVersion(serverVersion, table);
	}
	
	public List<Client> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar, String table) {
		return allClients.notInOpenMRSByServerVersion(serverVersion, calendar, table);
	}
	
	public List<Client> findByFieldValue(String field, List<String> ids, String table) {
		return allClients.findByFieldValue(field, ids, table);
	}
	
	public List<Client> findByFieldValue(String id, String table) {
		return allClients.findByRelationShip(id, table);
	}
	
	public Client findClientByBaseEntityId(String baseEntityId, String table) {
		Integer clientId = allClients.findClientIdByBaseEntityId(baseEntityId, table);
		if (clientId != null) {
			Client c = allClients.findClientByClientId(clientId, table);
			return c;
		}
		return null;
	}
	
	public Client addOrUpdate(Client client, String table, String district, String division, String branch, String village) {
		if (client.getBaseEntityId() == null) {
			if (client != null) {
				ErrorTrace errorTrace = new ErrorTrace();
				errorTrace.setRevision("missing base_entity_id");
				errorTrace.setDate(new DateTime());
				errorTrace.setDocumentType("Client");
				errorTrace.setStackTrace(client.toString());
				errorTrace.setRecordId(client.getBaseEntityId());
				errorTrace.setStatus("failed");
				errorTraceService.addError(errorTrace);
			}
			throw new RuntimeException("No baseEntityId");
		}
		//Client c = findClient(client);
		try {
			Integer clientId = allClients.findClientIdByBaseEntityId(client.getBaseEntityId(), table);
			if (clientId != null) {
				Client c = allClients.findClientByClientId(clientId, table);
				if (c != null) {
					client.setRevision(c.getRevision());
					client.setId(c.getId());
					client.setDateEdited(DateTime.now());
					client.setDateCreated(c.getDateCreated());
					client.setServerVersion(System.currentTimeMillis());
					client.addIdentifier("OPENMRS_UUID", c.getIdentifier("OPENMRS_UUID"));
					allClients.update(client, table, district, division, branch, village);
				}
				
			} else {
				client.setServerVersion(System.currentTimeMillis());
				client.setDateCreated(DateTime.now());
				logger.info("\n\n\n Client in addOrUpdate before add :" + client.toString() + "\n\n");
				allClients.add(client, table, district, division, branch, village);
			}
		}
		catch (Exception e) {
			if (client != null) {
				ErrorTrace errorTrace = new ErrorTrace();
				errorTrace.setDate(new DateTime());
				errorTrace.setDocumentType("Client");
				errorTrace.setStackTrace(client.toString());
				errorTrace.setRecordId("missing");
				errorTrace.setStatus("failed");
				errorTrace.setRevision(e.getMessage());
				errorTraceService.addError(errorTrace);
			}
			e.printStackTrace();
		}
		return client;
	}
	
	public Client addOrUpdate(Client client, boolean resetServerVersion, String table, String district, String division,
	                          String branch, String village) {
		if (client.getBaseEntityId() == null) {
			throw new RuntimeException("No baseEntityId");
		}
		try {
			Client c = findClient(client, table);
			if (c != null) {
				client.setRevision(c.getRevision());
				client.setId(c.getId());
				client.setDateEdited(DateTime.now());
				client.setDateCreated(c.getDateCreated());
				if (resetServerVersion) {
					client.setServerVersion(System.currentTimeMillis());
				}
				allClients.update(client, table, district, division, branch, village);
				
			} else {
				
				client.setDateCreated(DateTime.now());
				allClients.add(client, table, district, division, branch, village);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("update failed with caseId" + client.getBaseEntityId() + ", cause:" + e.getMessage());
		}
		return client;
	}
	
	public List<Client> findAllClientByUpazila(String name, String table) {
		return allClients.findAllClientByUpazila(name, table);
	}
	
	public CustomQuery findTeamInfo(String username) {
		return allClients.findTeamInfo(username);
	}
	
	public List<CustomQuery> getProviderLocationTreeByChildRole(int memberId, int childRoleId) {
		return allClients.getProviderLocationTreeByChildRole(memberId, childRoleId);
	}
	
	public List<CustomQuery> getPALocationTreeByChildRole(int memberId, int childRoleId) {
		return allClients.getPALocationTreeByChildRole(memberId, childRoleId);
	}
	
	public List<TargetDetails> getTargetDetails(String username, Long timestamp) {
		return allClients.getTargetDetails(username, timestamp);
	}
	
	public List<CustomQuery> getVillageByProviderId(int memberId, int childRoleId, int locationTagId) {
		return allClients.getVillageByProviderId(memberId, childRoleId, locationTagId);
	}
	
	public List<CustomQuery> getProviderLocationIdByChildRole(int memberId, int childRoleId, int locationTagId) {
		return allClients.getProviderLocationIdByChildRole(memberId, childRoleId, locationTagId);
	}
	
	public CustomQuery getUserStatus(String username) {
		// TODO Auto-generated method stub
		return allClients.findUserStatus(username);
	}
	
	public CustomQuery getUserId(String username) {
		// TODO Auto-generated method stub
		return allClients.findUserId(username);
	}
	
	public CustomQuery getMaxHealthId(Integer locationId) {
		// TODO Auto-generated method stub
		return allClients.getMaxHealthId(locationId);
	}
	
	public CustomQuery getGuestMaxHealthId(Integer locationId) {
		// TODO Auto-generated method stub
		return allClients.getGuestMaxHealthId(locationId);
	}
	
	public void updateAppVersion(String username, String version) {
		allClients.updateAppVersion(username, version);
	}
	
	public JSONArray getDistrictAndUpazila(Integer parentLocationTag) throws JSONException {
		List<CustomQuery> districtAndUpazila = allClients.getDistrictAndUpazila(parentLocationTag);
		JSONArray response = new JSONArray();
		for (CustomQuery o : districtAndUpazila) {
			JSONObject disAndUpa = new JSONObject();
			disAndUpa.put("name", o.getName());
			String[] upazilas = o.getCode().split(",");
			JSONArray upa = new JSONArray();
			for (String upazila : upazilas) {
				upa.put(upazila);
			}
			disAndUpa.put("upazilas", upa);
			response.put(disAndUpa);
		}
		return response;
	}
	
	public Integer findClientIdByBaseEntityId(String baseEntityId, String table) {
		return allClients.findClientIdByBaseEntityId(baseEntityId, table);
	}
	
	public CustomQuery imeiCheck(String imeiNumber) {
		return allClients.imeiCheck(imeiNumber);
	}
	
	public String getIsResync(String username) {
		return allClients.getIsResync(username);
	}
	
	public UserLocationTableName getUserLocationAndTable(String username, String district, String division, String branch,
	                                                     String village) {
		UserLocationTableName userLocation = new UserLocationTableName();
		String table = "";
		if (!StringUtils.isBlank(district)) {
			
			table = "_" + district;
			userLocation.setTableName(table);
			userLocation.setBranch(branch);
			userLocation.setDistrict(district);
			userLocation.setDivision(division);
			userLocation.setTableName(table);
		} else {
			userLocation = allClients.getUserLocation(username);
			
			if (userLocation != null) {
				table = "_" + userLocation.getDistrict();
				userLocation.setTableName(table);
			} else {
				userLocation = new UserLocationTableName();
				userLocation.setTableName("_others");
				userLocation.setBranch("");
				userLocation.setDistrict("");
				userLocation.setDivision("");
				userLocation.setTableName("");
			}
			
		}
		
		return userLocation;
	}
	
	public Client findClientByClientId(Integer clientId, String table) {
		return allClients.findClientByClientId(clientId, table);
	}
	
	public List<WebNotification> getWebNotifications(String username, Long timestamp) {
		
		return allClients.getWebNotifications(username, timestamp);
	}
	
	public List<StockInfo> getStockInfos(String username, Long timestamp) {
		return allClients.getStockInfos(username, timestamp);
	}
	
	public List<Client> findByRelationshipId(String relationshipId, String table) {
		return allClients.findByRelationshipId(relationshipId, table);
	}
	
	/***
	 * @param inClient means after migrated client
	 * @param outClient means before migrated client
	 **/
	public Migration setMigration(Client inClient, Client outClient, Client inHhousehold, Client outHhousehold,
	                              String inProvider, String outProvider, String inHHrelationalId, String outHHrelationalId,
	                              String branchIdIn, String branchIdOut, String type, UserLocationTableName oldUserLocation,
	                              UserLocationTableName newUserLocation) {
		
		Address inAddressa = inClient.getAddress("usual_residence");
		Migration migration = new Migration();
		migration.setDistrictIn(inAddressa.getCountyDistrict());
		migration.setDivisionIn(inAddressa.getStateProvince());
		migration.setVillageIn(inAddressa.getCityVillage());
		migration.setUpazilaIn(inAddressa.getAddressField("address2"));
		migration.setPourasavaIn(inAddressa.getAddressField("address3"));
		migration.setUnionIn(inAddressa.getAddressField("address1"));
		migration.setVillageIDIn(inAddressa.getAddressField("address8"));
		
		Address outAddressa = outClient.getAddress("usual_residence");
		
		migration.setDistrictOut(outAddressa.getCountyDistrict());
		migration.setDivisionOut(outAddressa.getStateProvince());
		migration.setVillageOut(outAddressa.getCityVillage());
		migration.setUpazilaOut(outAddressa.getAddressField("address2"));
		migration.setPourasavaOut(outAddressa.getAddressField("address3"));
		migration.setUnionOut(outAddressa.getAddressField("address1"));
		migration.setVillageIDOut(outAddressa.getAddressField("address8"));
		
		migration.setMemberName(outClient.getFirstName());
		
		migration.setMemberContact(outClient.getAttribute("Mobile_Number") + "");
		
		migration.setMemberIDIn(inClient.getIdentifier("opensrp_id"));
		migration.setMemberIDOut(outClient.getIdentifier("opensrp_id"));
		migration.setSKIn(inProvider);
		migration.setSKOut(outProvider);
		migration.setSSIn(inClient.getAttribute("SS_Name") + "");
		migration.setSSOut(outClient.getAttribute("SS_Name") + "");
		migration.setRelationalIdIn(inHHrelationalId);
		migration.setRelationalIdOut(outHHrelationalId);
		
		if (outHhousehold != null) {
			migration.setHHNameOut(outHhousehold.getFirstName());
			
			migration.setHHContactOut(outHhousehold.getAttribute("HOH_Phone_Number") + "");
			
			migration.setNumberOfMemberOut(outHhousehold.getAttribute("Number_of_HH_Member") + "");
			
			migration.setRelationWithHHOut(outHhousehold.getAttribute("Relation_with_HOH") + "");
		}
		if (inHhousehold != null) {
			migration.setHHNameIn(inHhousehold.getFirstName());
			migration.setHHContactIn(inHhousehold.getAttribute("HOH_Phone_Number") + "");
			migration.setNumberOfMemberIn(inHhousehold.getAttribute("Number_of_HH_Member") + "");
			migration.setRelationWithHHIn(inHhousehold.getAttribute("Relation_with_HOH") + "");
			
		}
		migration.setBranchIDIn(branchIdIn);
		migration.setBranchIDOut(branchIdOut);
		
		migration.setStatus(MigrationStatus.PENDING.name());
		
		if (outClient.getBirthdate() != null) {
			migration.setDob(outClient.getBirthdate().toDate());
		}
		migration.setMigrationDate(new DateTime().toDate());
		migration.setMemberType(type);
		migration.setBaseEntityId(outClient.getBaseEntityId());
		migration.setDistrictIdOut(oldUserLocation.getTableName());
		migration.setDistrictIdIn(newUserLocation.getTableName());
		
		migration.setDivisionIdOut(oldUserLocation.getDivision());
		migration.setDivisionIdIn(newUserLocation.getDivision());
		migration.setTimestamp(System.currentTimeMillis());
		
		Map<String, List<String>> motherShips = outClient.getRelationships();
		String motherId = "";
		if (motherShips.containsKey("mother")) {
			motherId = motherShips.get("mother").get(0);
		}
		migration.setMotherId(motherId);
		return migration;
		
	}
	
	public String findBranchId(String baseEntityId, String table) {
		return allClients.findBranchId(baseEntityId, table);
	}
	
	public Integer addMigration(Migration migration) {
		return allClients.addMigration(migration);
		
	}
	
	public Migration findMigrationById(Long id) {
		
		return allClients.findMigrationById(id);
	}
	
	public List<Migration> findMigrationByIdRelationId(String relationalId, String status) {
		
		return allClients.findMigrationByIdRelationId(relationalId, status);
	}
	
	public Address setAddress(Client c, Migration migration) {
		Map<String, String> addressFields = new HashMap<String, String>();
		addressFields.put("address1", migration.getUnionOut());
		addressFields.put("address2", migration.getUpazilaOut());
		addressFields.put("address3", migration.getPourasavaOut());
		addressFields.put("address8", migration.getVillageIDOut());
		Address address = new Address();
		address.setCityVillage(migration.getVillageOut());
		address.setCountry("BANGLADESH");
		address.setCountyDistrict(migration.getDistrictOut());
		address.setAddressType("usual_residence");
		address.setStateProvince(migration.getDivisionOut());
		address.setAddressFields(addressFields);
		return address;
	}
	
	public Integer updateMigration(Migration migration, String baseEntityId) {
		
		return allClients.updateMigration(migration, baseEntityId);
	}
	
	public Integer updateMigrationStatusById(Long id, String status) {
		
		return allClients.updateMigrationStatusById(id, status);
	}
	
	public Integer updateMigrationStatusByRelationalId(String relationalId, String status) {
		
		return allClients.updateMigrationStatusByRelationalId(relationalId, status);
	}
}
