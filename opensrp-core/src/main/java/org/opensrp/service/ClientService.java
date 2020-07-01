package org.opensrp.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opensrp.domain.Address;
import org.opensrp.domain.Client;
import org.opensrp.domain.ErrorTrace;
import org.opensrp.domain.postgres.CustomQuery;
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
	
	public Client getByBaseEntityId(String baseEntityId) {
		return allClients.findByBaseEntityId(baseEntityId);
	}
	
	public List<Client> findAllClients() {
		return allClients.findAllClients();
	}
	
	public List<Client> findAllByIdentifier(String identifier) {
		return allClients.findAllByIdentifier(identifier);
	}
	
	public List<Client> findAllByIdentifier(String identifierType, String identifier) {
		return allClients.findAllByIdentifier(identifierType, identifier);
	}
	
	public List<Client> findByRelationshipIdAndDateCreated(String relationalId, String dateFrom, String dateTo) {
		return allClients.findByRelationshipIdAndDateCreated(relationalId, dateFrom, dateTo);
	}
	
	public List<Client> findByRelationship(String relationalId) {
		return allClients.findByRelationShip(relationalId);
	}
	
	public List<Client> findAllByAttribute(String attributeType, String attribute) {
		return allClients.findAllByAttribute(attributeType, attribute);
	}
	
	public List<Client> findAllByMatchingName(String nameMatches) {
		return allClients.findAllByMatchingName(nameMatches);
	}
	
	public List<Client> findByCriteria(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean,
	                                   DateTime lastEditFrom, DateTime lastEditTo) {
		clientSearchBean.setLastEditFrom(lastEditFrom);
		clientSearchBean.setLastEditTo(lastEditTo);
		return allClients.findByCriteria(clientSearchBean, addressSearchBean);//db.queryView(q.includeDocs(true), Client.class);
	}
	
	public List<Client> findByCriteria(ClientSearchBean clientSearchBean, Long serverVersion) {
		return allClients.findByCriteria(clientSearchBean, new AddressSearchBean());
	}
	
	public List<Client> findByCriteria(ClientSearchBean clientSearchBean, AddressSearchBean addressSearchBean) {
		return allClients.findByCriteria(clientSearchBean, addressSearchBean);
	}
	
	/*	public List<Client> findByCriteria(String addressType, String country, String stateProvince, String cityVillage, String countyDistrict, 
				String  subDistrict, String town, String subTown, DateTime lastEditFrom, DateTime lastEditTo) {
			return allClients.findByCriteria(null, null, null, null, null, null, null, null, addressType, country, stateProvince, cityVillage, countyDistrict, subDistrict, town, subTown, lastEditFrom, lastEditTo);
		}*/
	
	public List<Client> findByDynamicQuery(String query) {
		return allClients.findByDynamicQuery(query);
	}
	
	public Client addClient(Client client) {
		if (client.getBaseEntityId() == null) {
			throw new RuntimeException("No baseEntityId");
		}
		Client c = findClient(client);
		if (c != null) {
			try {
				updateClient(client);
			}
			catch (JSONException e) {
				throw new IllegalArgumentException(
				        "A client already exists with given list of identifiers. Consider updating data.[" + c + "]");
			}
		}
		
		client.setDateCreated(DateTime.now());
		allClients.add(client);
		return client;
	}
	
	public Client findClient(Client client) {
		// find by auto assigned entity id
		Client c = null;
		try {
			c = allClients.findByBaseEntityId(client.getBaseEntityId());
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
	
	public Client find(String uniqueId) {
		// find by document id
		Client c = allClients.findByBaseEntityId(uniqueId);
		if (c != null) {
			return c;
		}
		
		// if not found find if it is in any identifiers TODO refactor it later
		List<Client> cl = allClients.findAllByIdentifier(uniqueId);
		if (cl.size() > 1) {
			throw new IllegalArgumentException("Multiple clients with identifier " + uniqueId + " exist.");
		} else if (cl.size() != 0) {
			return cl.get(0);
		}
		
		return c;
	}
	
	public void updateClient(Client updatedClient) throws JSONException {
		// If update is on original entity
		if (updatedClient.isNew()) {
			throw new IllegalArgumentException(
			        "Client to be updated is not an existing and persisting domain object. Update database object instead of new pojo");
		}
		
		if (findClient(updatedClient) == null) {
			throw new IllegalArgumentException("No client found with given list of identifiers. Consider adding new!");
		}
		
		updatedClient.setDateEdited(DateTime.now());
		allClients.update(updatedClient);
	}
	
	public Client mergeClient(Client updatedClient, JSONObject relationship) {
		try {
			Client original = findClient(updatedClient);
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
			allClients.update(original);
			return original;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<Client> findByServerVersion(long serverVersion) {
		return allClients.findByServerVersion(serverVersion);
	}
	
	public List<Client> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar) {
		return allClients.notInOpenMRSByServerVersion(serverVersion, calendar);
	}
	
	public List<Client> findByFieldValue(String field, List<String> ids) {
		return allClients.findByFieldValue(field, ids);
	}
	
	public List<Client> findByFieldValue(String id) {
		return allClients.findByRelationShip(id);
	}
	
	public Client addOrUpdate(Client client) {
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
			Integer clientId = allClients.findClientIdByBaseEntityId(client.getBaseEntityId());
			if (clientId != null) {
				Client c = allClients.findClientByClientId(clientId);
				if (c != null) {
					client.setRevision(c.getRevision());
					client.setId(c.getId());
					client.setDateEdited(DateTime.now());
					client.setDateCreated(c.getDateCreated());
					client.setServerVersion(System.currentTimeMillis());
					client.addIdentifier("OPENMRS_UUID", c.getIdentifier("OPENMRS_UUID"));
					allClients.update(client);
				}
				
			} else {
				client.setServerVersion(System.currentTimeMillis());
				client.setDateCreated(DateTime.now());
				logger.info("\n\n\n Client in addOrUpdate before add :" + client.toString() + "\n\n");
				allClients.add(client);
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
	
	public Client addOrUpdate(Client client, boolean resetServerVersion) {
		if (client.getBaseEntityId() == null) {
			throw new RuntimeException("No baseEntityId");
		}
		try {
			Client c = findClient(client);
			if (c != null) {
				client.setRevision(c.getRevision());
				client.setId(c.getId());
				client.setDateEdited(DateTime.now());
				client.setDateCreated(c.getDateCreated());
				if (resetServerVersion) {
					client.setServerVersion(System.currentTimeMillis());
				}
				allClients.update(client);
				
			} else {
				
				client.setDateCreated(DateTime.now());
				allClients.add(client);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("update failed with caseId" + client.getBaseEntityId() + ", cause:" + e.getMessage());
		}
		return client;
	}
	
	public List<Client> findAllClientByUpazila(String name) {
		return allClients.findAllClientByUpazila(name);
	}
	
	public CustomQuery findTeamInfo(String username) {
		return allClients.findTeamInfo(username);
	}
	
	public List<CustomQuery> getProviderLocationTreeByChildRole(int memberId, int childRoleId) {
		return allClients.getProviderLocationTreeByChildRole(memberId, childRoleId);
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
	
	public Integer findClientIdByBaseEntityId(String baseEntityId) {
		return allClients.findClientIdByBaseEntityId(baseEntityId);
	}
	
	public CustomQuery imeiCheck(String imeiNumber) {
		return allClients.imeiCheck(imeiNumber);
	}
	
	public String getIsResync(String username) {
		return allClients.getIsResync(username);
	}
}
