package org.opensrp.repository.couch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.ektorp.util.Assert;
import org.ektorp.util.Documents;
import org.joda.time.DateTime;
import org.motechproject.dao.MotechBaseRepository;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.Client;
import org.opensrp.domain.LocationDTO;
import org.opensrp.domain.Migration;
import org.opensrp.domain.UserLocationTableName;
import org.opensrp.domain.postgres.CustomQuery;
import org.opensrp.domain.postgres.StockInfo;
import org.opensrp.domain.postgres.TargetDetails;
import org.opensrp.domain.postgres.WebNotification;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.repository.lucene.LuceneClientRepository;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.mysql.jdbc.StringUtils;

@Repository("couchClientsRepository")
@Primary
public class AllClients extends MotechBaseRepository<Client> implements ClientsRepository {
	
	private LuceneClientRepository lcr;
	
	@Autowired
	protected AllClients(@Qualifier(AllConstants.OPENSRP_DATABASE_CONNECTOR) CouchDbConnector db, LuceneClientRepository lcr) {
		super(Client.class, db);
		this.lcr = lcr;
	}
	
	@GenerateView
	public Client findByBaseEntityId(String baseEntityId) {
		if (StringUtils.isEmptyOrWhitespaceOnly(baseEntityId))
			return null;
		List<Client> clients = queryView("by_baseEntityId", baseEntityId);
		if (clients == null || clients.isEmpty()) {
			return null;
		}
		return clients.get(0);
	}
	
	@GenerateView
	public Client findByBaseEntityId(CouchDbConnector targetDb, String baseEntityId) {
		if (StringUtils.isEmptyOrWhitespaceOnly(baseEntityId))
			return null;
		List<Client> clients = queryView(targetDb, "by_baseEntityId", baseEntityId);
		if (clients == null || clients.isEmpty()) {
			return null;
		}
		return clients.get(0);
	}
	
	@View(name = "all_clients", map = "function(doc) { if (doc.type === 'Client') { emit(doc.baseEntityId); } }")
	public List<Client> findAllClients() {
		return db.queryView(createQuery("all_clients").includeDocs(true), Client.class);
	}
	
	@View(name = "all_clients_by_identifier", map = "function(doc) {if (doc.type === 'Client') {for(var key in doc.identifiers) {emit(doc.identifiers[key]);}}}")
	public List<Client> findAllByIdentifier(String identifier) {
		return db.queryView(createQuery("all_clients_by_identifier").key(identifier).includeDocs(true), Client.class);
	}
	
	@View(name = "all_clients_by_identifier", map = "function(doc) {if (doc.type === 'Client') {for(var key in doc.identifiers) {emit(doc.identifiers[key]);}}}")
	public List<Client> findAllByIdentifier(CouchDbConnector targetDb, String identifier) {
		return targetDb.queryView(createQuery("all_clients_by_identifier").key(identifier).includeDocs(true), Client.class);
	}
	
	@View(name = "all_clients_by_identifier_of_type", map = "function(doc) {if (doc.type === 'Client') {for(var key in doc.identifiers) {emit([key, doc.identifiers[key]]);}}}")
	public List<Client> findAllByIdentifier(String identifierType, String identifier) {
		ComplexKey ckey = ComplexKey.of(identifierType, identifier);
		return db.queryView(createQuery("all_clients_by_identifier_of_type").key(ckey).includeDocs(true), Client.class);
	}
	
	@View(name = "all_clients_by_attribute_of_type", map = "function(doc) {if (doc.type === 'Client') {for(var key in doc.attributes) {emit([key, doc.attributes[key]]);}}}")
	public List<Client> findAllByAttribute(String attributeType, String attribute) {
		ComplexKey ckey = ComplexKey.of(attributeType, attribute);
		return db.queryView(createQuery("all_clients_by_attribute_of_type").key(ckey).includeDocs(true), Client.class);
	}
	
	@View(name = "all_clients_by_matching_name", map = "function(doc) {if(doc.type === 'Client'){emit(doc.firstName, doc);emit(doc.lastName, doc);}}")
	public List<Client> findAllByMatchingName(String nameMatches) {
		return db.queryView(createQuery("all_clients_by_matching_name").startKey(nameMatches).endKey(nameMatches + "z")
		        .includeDocs(true), Client.class);
	}
	
	/**
	 * Find a client based on the relationship id and between a range of date created dates e.g
	 * given mother's id get children born at a given time Use Beans to search for methods with very
	 * many search params
	 * 
	 * @param relationalId
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
	@View(name = "client_by_relationship_id_and_date_created", map = "function(doc) { if (doc.type === 'Client' && doc.relationships) {for (var key in doc.relationships) { var entityid=doc.relationships[key][0]; emit([entityid, doc.dateCreated.substring(0,10)], null); }} }")
	public List<Client> findByRelationshipIdAndDateCreated(String relationalId, String dateFrom, String dateTo) {
		ComplexKey startKey = ComplexKey.of(relationalId, dateFrom);
		ComplexKey endKey = ComplexKey.of(relationalId, dateTo);
		List<Client> clients = db.queryView(createQuery("client_by_relationship_id_and_date_created").startKey(startKey)
		        .endKey(endKey).includeDocs(true), Client.class);
		return clients;
	}
	
	//	@View(name = "client_by_relationship", map = "function(doc) {if (doc.type === 'Client') {for(var key in doc.relationships) {emit([key, doc.relationships[key]]);}}}")
	//	@View(name = "client_by_relationship", map = "function(doc) { if(doc.type == 'Client' && doc.relationships.mother[0]) {emit(null, doc._id)} }")
	@View(name = "client_by_relationship", map = "function(doc) { if(doc.type === 'Client' && doc.relationships) { for (var key in doc.relationships) { var entityid = doc.relationships[key][0]; if (key === 'mother') {emit([key, entityid], doc);}}}}")
	public List<Client> findByRelationshipId(String relationshipType, String entityId) {
		return db.queryView(createQuery("client_by_relationship").startKey(entityId).endKey(entityId).includeDocs(true),
		    Client.class);
	}
	
	//	@View(name = "clients_by_relationship", map = "function(doc) {if (doc.type === 'Client' && doc.relationships.mother) {for(var key in doc.relationships) {emit(doc.relationships.mother[key]);}}}")
	//	public List<Client> findByRelationshipId(String identifier) {
	//		return db.queryView(createQuery("clients_by_relationship").key(identifier).includeDocs(true), Client.class);
	//	}
	
	public List<Client> findByCriteria(ClientSearchBean searchBean, AddressSearchBean addressSearchBean) {
		return lcr.getByCriteria(searchBean, addressSearchBean, null);//db.queryView(q.includeDocs(true), Client.class);
	}
	
	public List<Client> findByDynamicQuery(String query) {
		return lcr.getByCriteria(query);//db.queryView(q.includeDocs(true), Client.class);
	}
	
	public List<Client> findByCriteria(ClientSearchBean searchBean) {
		return lcr.getByCriteria(searchBean, new AddressSearchBean(), null);
	}
	
	public List<Client> findByCriteria(AddressSearchBean addressSearchBean, DateTime lastEditFrom, DateTime lastEditTo) {
		ClientSearchBean clientSearchBean = new ClientSearchBean();
		clientSearchBean.setLastEditFrom(lastEditFrom);
		return lcr.getByCriteria(clientSearchBean, addressSearchBean, null);
	}
	
	public List<Client> findByRelationShip(String motherIndentier) {
		return lcr.getByClientByMother("mother", motherIndentier);
	}
	
	/**
	 * Query view from the specified db
	 * 
	 * @param targetDb
	 * @param viewName
	 * @param key
	 * @return
	 */
	public List<Client> queryView(CouchDbConnector targetDb, String viewName, String key) {
		return targetDb.queryView(createQuery(viewName).includeDocs(true).key(key), Client.class);
	}
	
	/**
	 * Save client to the specified db
	 * 
	 * @param targetDb
	 * @param client
	 */
	public void add(CouchDbConnector targetDb, Client client) {
		Assert.isTrue(Documents.isNew(client), "entity must be new");
		targetDb.create(client);
	}
	
	/**
	 * Get all clients without a server version
	 * 
	 * @return
	 */
	@View(name = "clients_by_empty_server_version", map = "function(doc) { if ( doc.type == 'Client' && !doc.serverVersion) { emit(doc._id, doc); } }")
	public List<Client> findByEmptyServerVersion() {
		return db.queryView(createQuery("clients_by_empty_server_version").limit(200).includeDocs(true), Client.class);
	}
	
	@View(name = "clients_by__server_version", map = "function(doc) { if (doc.type === 'Client') { emit([doc.serverVersion], null); } }")
	public List<Client> findByServerVersion(long serverVersion) {
		ComplexKey startKey = ComplexKey.of(serverVersion + 1);
		ComplexKey endKey = ComplexKey.of(System.currentTimeMillis());
		return db.queryView(createQuery("clients_by__server_version").startKey(startKey).endKey(endKey).includeDocs(true),
		    Client.class);
	}
	
	public List<Client> findByFieldValue(String field, List<String> ids) {
		return lcr.getByFieldValue(field, ids);
	}
	
	@View(name = "clients_not_in_OpenMRS", map = "function(doc) { if (doc.type === 'Client' && doc.serverVersion) { var noId = true; for(var key in doc.identifiers) {if(key == 'OPENMRS_UUID') {noId = false;}}if(noId){emit([doc.serverVersion],  null); }} }")
	public List<Client> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar) {
		long serverStartKey = serverVersion + 1;
		long serverEndKey = calendar.getTimeInMillis();
		if (serverStartKey < serverEndKey) {
			ComplexKey startKey = ComplexKey.of(serverStartKey);
			ComplexKey endKey = ComplexKey.of(serverEndKey);
			return db.queryView(createQuery("clients_not_in_OpenMRS").startKey(startKey).endKey(endKey).limit(1000)
			        .includeDocs(true), Client.class);
		}
		return new ArrayList<>();
	}
	
	@Override
	public List<Client> findAllClientByUpazila(String name, String table) {
		return null;
	}
	
	@Override
	public CustomQuery findTeamInfo(String username) {
		return null;
	}
	
	@Override
	public List<CustomQuery> getProviderLocationTreeByChildRole(int memberId, int childRoleId) {
		return null;
	}
	
	@Override
	public List<CustomQuery> getPALocationTreeByChildRole(int memberId, int childRoleId) {
		return null;
	}
	
	@Override
	public List<TargetDetails> getTargetDetails(String username, Long timestamp) {
		return null;
	}
	
	@Override
	public List<CustomQuery> getVillageByProviderId(int memberId, int childRoleId, int locationTagId) {
		return null;
	}
	
	@Override
	public List<CustomQuery> getProviderLocationIdByChildRole(int memberId, int childRoleId, int locationTagId) {
		return null;
	}
	
	public Client addClient(CouchDbConnector targetDb, Client client) {
		if (client.getBaseEntityId() == null) {
			throw new RuntimeException("No baseEntityId");
		}
		Client c = findClient(targetDb, client);
		if (c != null) {
			throw new IllegalArgumentException(
			        "A client already exists with given list of identifiers. Consider updating data.[" + c + "]");
		}
		
		client.setDateCreated(new DateTime());
		add(targetDb, client);
		return client;
	}
	
	/**
	 * Find a client from the specified db
	 *
	 * @param targetDb
	 * @param client
	 * @return
	 */
	public Client findClient(CouchDbConnector targetDb, Client client) {
		// find by auto assigned entity id
		try {
			Client c = findByBaseEntityId(client.getBaseEntityId());
			if (c != null) {
				return c;
			}
			
			//still not found!! search by generic identifiers
			
			for (String idt : client.getIdentifiers().keySet()) {
				List<Client> cl = findAllByIdentifier(targetDb, client.getIdentifier(idt));
				if (cl.size() > 1) {
					throw new IllegalArgumentException("Multiple clients with identifier type " + idt + " and ID "
					        + client.getIdentifier(idt) + " exist.");
				} else if (cl.size() != 0) {
					return cl.get(0);
				}
			}
			return c;
		}
		catch (Exception e) {
			
			return null;
		}
	}
	
	@Override
	public CustomQuery findUserStatus(String username) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public CustomQuery findUserId(String username) {
		return null;
	}
	
	@Override
	public CustomQuery getMaxHealthId(Integer locationId) {
		return null;
	}
	
	@Override
	public void updateAppVersion(String username, String version) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Integer findClientIdByBaseEntityId(String baseEntityId, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Client findClientByClientId(Integer clientId, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<CustomQuery> getDistrictAndUpazila(Integer parentLocationTag) {
		return null;
	}
	
	@Override
	public CustomQuery imeiCheck(String imeiNumber) {
		return null;
	}
	
	@Override
	public String getIsResync(String username) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void add(Client entity, String table, String dist, String div, String branch, String vill) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update(Client entity, String table, String dist, String div, String branch, String vill) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<Client> getAll(String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void safeRemove(Client entity, String table) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Client findByBaseEntityId(String baseEntityId, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findAllClients(String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findAllByIdentifier(String identifierType, String identifier, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findAllByAttribute(String attributeType, String attribute, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findAllByMatchingName(String nameMatches, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByRelationshipIdAndDateCreated(String relationalId, String dateFrom, String dateTo, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByRelationshipId(String relationshipType, String entityId, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByCriteria(ClientSearchBean searchBean, AddressSearchBean addressSearchBean, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByCriteria(ClientSearchBean searchBean, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByCriteria(AddressSearchBean addressSearchBean, DateTime lastEditFrom, DateTime lastEditTo,
	                                   String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByRelationShip(String relationIndentier, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByEmptyServerVersion(String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByServerVersion(long serverVersion, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> findByFieldValue(String field, List<String> ids, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public UserLocationTableName getUserLocation(String username) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<WebNotification> getWebNotifications(String username, Long timestamp) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<StockInfo> getStockInfos(String username, Long timestamp) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public CustomQuery getGuestMaxHealthId(Integer locationId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String findBranchId(String baseEntityId, String table) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Integer addMigration(Migration migration) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Migration findMigrationById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Migration> findMigrationByIdRelationId(String relationalId, String status) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Integer updateMigration(Migration migration, String baseEntityId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Integer updateMigrationStatusById(Long id, String status) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Integer updateMigrationStatusByRelationalId(String relationalId, String status) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<LocationDTO> getLocationByTagId(Integer tagId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<LocationDTO> getLocationByparentId(Integer parentId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Client> searchClient(Integer vilageId, String gender, Integer startAge, Integer endAge, String type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> getMigratedList(String provider, String type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> getRejectedList(String provider, String type) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
