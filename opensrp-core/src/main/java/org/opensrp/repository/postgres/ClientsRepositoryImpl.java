package org.opensrp.repository.postgres;

import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.Address;
import org.opensrp.domain.Client;
import org.opensrp.domain.Migration;
import org.opensrp.domain.UserLocationTableName;
import org.opensrp.domain.postgres.ClientMetadata;
import org.opensrp.domain.postgres.ClientMetadataExample;
import org.opensrp.domain.postgres.CustomQuery;
import org.opensrp.domain.postgres.StockInfo;
import org.opensrp.domain.postgres.TargetDetails;
import org.opensrp.domain.postgres.WebNotification;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.repository.postgres.mapper.custom.CustomClientMapper;
import org.opensrp.repository.postgres.mapper.custom.CustomClientMetadataMapper;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("clientsRepositoryPostgres")
public class ClientsRepositoryImpl extends CustomBaseRepositoryImpl<Client> implements ClientsRepository {
	
	private static Logger logger = LoggerFactory.getLogger(ClientsRepository.class.toString());
	
	@Value("#{opensrp['address.type']}")
	private String addressType;
	
	@Value("#{opensrp['use.address.client.in.metadata']}")
	private Boolean userAddressInClientMetadata;
	
	@Autowired
	private CustomClientMetadataMapper clientMetadataMapper;
	
	@Autowired
	private CustomClientMapper clientMapper;
	
	@Override
	public Client get(String id, String table) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		
		org.opensrp.domain.postgres.Client pgClient = clientMetadataMapper.selectByDocumentId(id, table);
		if (pgClient == null) {
			return null;
		}
		return convert(pgClient);
	}
	
	@Override
	public void add(Client entity, String table, String district, String division, String branch, String village) {
		if (entity == null || entity.getBaseEntityId() == null) {
			return;
		}
		
		if (retrievePrimaryKey(entity, table) != null) { //Client already added
			return;
		}
		
		if (entity.getId() == null)
			entity.setId(UUID.randomUUID().toString());
		
		setRevision(entity);
		
		org.opensrp.domain.postgres.Client pgClient = convert(entity, null);
		if (pgClient == null) {
			return;
		}
		pgClient.setDistrict(district);
		pgClient.setDivision(division);
		pgClient.setBranch(branch);
		pgClient.setBaseEntityId(entity.getBaseEntityId());
		pgClient.setServerVersion(entity.getServerVersion());
		Map<String, String> addressFields = entity.getAddresses().get(0).getAddressFields();
		if (addressFields != null) {
			if (addressFields.containsKey("address8")) {
				pgClient.setVillage(addressFields.get("address8"));
			}
		}
		
		int rowsAffected = clientMapper.insertSelectiveAndSetId(pgClient);
		if (rowsAffected < 1 || pgClient.getId() == null) {
			return;
		}
		
		ClientMetadata clientMetadata = createMetadata(entity, pgClient.getId());
		if (clientMetadata != null) {
			
			clientMetadata.setDistrict(district);
			clientMetadata.setDivision(division);
			clientMetadata.setBranch(branch);
			
			clientMetadataMapper.insertSelective(clientMetadata);
		}
	}
	
	@Override
	public void update(Client entity, String table, String district, String division, String branch, String village) {
		
		if (entity == null || entity.getBaseEntityId() == null) {
			return;
		}
		
		Long id = retrievePrimaryKey(entity, table);
		if (id == null) { // Client not added
			return;
		}
		
		setRevision(entity);
		
		org.opensrp.domain.postgres.Client pgClient = convert(entity, id);
		if (pgClient == null) {
			return;
		}
		
		ClientMetadata clientMetadata = createMetadata(entity, id);
		if (clientMetadata == null) {
			return;
		}
		pgClient.setDistrict(district);
		pgClient.setDivision(division);
		pgClient.setBranch(branch);
		pgClient.setBaseEntityId(entity.getBaseEntityId());
		pgClient.setServerVersion(entity.getServerVersion());
		Map<String, String> addressFields = entity.getAddresses().get(0).getAddressFields();
		if (addressFields != null) {
			if (addressFields.containsKey("address8")) {
				pgClient.setVillage(addressFields.get("address8"));
			}
		}
		int rowsAffected = clientMapper.updateByPrimaryKey(pgClient);
		if (rowsAffected < 1) {
			return;
		}
		
		ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
		clientMetadataExample.createCriteria().andClientIdEqualTo(id).andDateDeletedIsNull();
		clientMetadata.setId(clientMetadataMapper.selectByExample(clientMetadataExample).get(0).getId());
		clientMetadata.setDistrict(district);
		clientMetadata.setDivision(division);
		clientMetadata.setBranch(branch);
		
		clientMetadataMapper.updateByPrimaryKey(clientMetadata);
	}
	
	@Override
	public List<Client> getAll(String table) {
		ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
		clientMetadataExample.createCriteria().andDateDeletedIsNull();
		List<org.opensrp.domain.postgres.Client> clients = clientMetadataMapper.selectMany(clientMetadataExample, 0,
		    DEFAULT_FETCH_SIZE, table);
		return convert(clients);
	}
	
	@Override
	public void safeRemove(Client entity, String table) {
		if (entity == null || entity.getBaseEntityId() == null) {
			return;
		}
		
		Long id = retrievePrimaryKey(entity, table);
		if (id == null) {
			return;
		}
		
		Date dateDeleted = entity.getDateVoided() == null ? new Date() : entity.getDateVoided().toDate();
		ClientMetadata clientMetadata = new ClientMetadata();
		clientMetadata.setDateDeleted(dateDeleted);
		
		ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
		clientMetadataExample.createCriteria().andClientIdEqualTo(id).andDateDeletedIsNull();
		
		int rowsAffected = clientMetadataMapper.updateByExampleSelective(clientMetadata, clientMetadataExample);
		if (rowsAffected < 1) {
			return;
		}
		
		org.opensrp.domain.postgres.Client pgClient = new org.opensrp.domain.postgres.Client();
		pgClient.setId(id);
		pgClient.setDateDeleted(dateDeleted);
		clientMapper.updateByPrimaryKeySelective(pgClient);
	}
	
	@Override
	public Client findByBaseEntityId(String baseEntityId, String table) {
		if (StringUtils.isBlank(baseEntityId)) {
			return null;
		}
		org.opensrp.domain.postgres.Client pgClient = clientMetadataMapper.selectOne(baseEntityId, table);
		return convert(pgClient);
	}
	
	@Override
	public List<Client> findAllClients(String table) {
		return getAll(table);
	}
	
	@Override
	public List<Client> findAllByIdentifier(String identifier, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMapper.selectByIdentifier(identifier, table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findAllByIdentifier(String identifierType, String identifier, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMapper.selectByIdentifierOfType(identifierType, identifier,
		    table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findAllByAttribute(String attributeType, String attribute, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMapper.selectByAttributeOfType(attributeType, attribute,
		    table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findAllByMatchingName(String nameMatches, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMetadataMapper.selectByName(nameMatches, 0,
		    DEFAULT_FETCH_SIZE, table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findByRelationshipIdAndDateCreated(String relationalId, String dateFrom, String dateTo, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMapper.selectByRelationshipIdAndDateCreated(relationalId,
		    new DateTime(dateFrom).toDate(), new DateTime(dateTo).toDate(), table);
		return convert(clients);
	}
	
	public List<Client> findByRelationshipId(String relationshipType, String entityId, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMapper.selectByRelationshipIdOfType(relationshipType,
		    entityId, table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findByCriteria(ClientSearchBean searchBean, AddressSearchBean addressSearchBean, String table) {
		return convert(clientMetadataMapper.selectBySearchBean(searchBean, addressSearchBean, 0, DEFAULT_FETCH_SIZE, table));
	}
	
	@Override
	public List<Client> findByDynamicQuery(String query) {
		throw new IllegalArgumentException("Method not supported");
	}
	
	@Override
	public List<Client> findByCriteria(ClientSearchBean searchBean, String table) {
		return findByCriteria(searchBean, new AddressSearchBean(), table);
	}
	
	@Override
	public List<Client> findByCriteria(AddressSearchBean addressSearchBean, DateTime lastEditFrom, DateTime lastEditTo,
	                                   String table) {
		ClientSearchBean clientSearchBean = new ClientSearchBean();
		clientSearchBean.setLastEditFrom(lastEditFrom);
		clientSearchBean.setLastEditTo(lastEditTo);
		return findByCriteria(clientSearchBean, addressSearchBean, table);
	}
	
	@Override
	public List<Client> findByRelationShip(String relationIndentier, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMapper.selectByRelationShip(relationIndentier, table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findByEmptyServerVersion(String table) {
		ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
		clientMetadataExample.createCriteria().andServerVersionIsNull().andDateDeletedIsNull();
		clientMetadataExample.setOrderByClause("client_id ASC");
		
		List<org.opensrp.domain.postgres.Client> clients = clientMetadataMapper.selectMany(clientMetadataExample, 0,
		    DEFAULT_FETCH_SIZE, table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findByServerVersion(long serverVersion, String table) {
		ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
		clientMetadataExample.createCriteria().andServerVersionGreaterThanOrEqualTo(serverVersion + 1)
		        .andDateDeletedIsNull();
		clientMetadataExample.setOrderByClause("server_version ASC");
		
		List<org.opensrp.domain.postgres.Client> clients = clientMetadataMapper.selectMany(clientMetadataExample, 0,
		    DEFAULT_FETCH_SIZE, table);
		return convert(clients);
	}
	
	@Override
	public List<Client> findByFieldValue(String field, List<String> ids, String table) {
		if (field.equals(BASE_ENTITY_ID) && ids != null && !ids.isEmpty()) {
			ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
			clientMetadataExample.createCriteria().andBaseEntityIdIn(ids).andDateDeletedIsNull();
			List<org.opensrp.domain.postgres.Client> clients = clientMetadataMapper.selectMany(clientMetadataExample, 0,
			    DEFAULT_FETCH_SIZE, table);
			return convert(clients);
		}
		return new ArrayList<>();
	}
	
	@Override
	public List<Client> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar, String table) {
		long serverStartKey = serverVersion + 1;
		long serverEndKey = calendar.getTimeInMillis();
		if (serverStartKey < serverEndKey) {
			ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
			clientMetadataExample.createCriteria().andOpenmrsUuidIsNull()
			        .andServerVersionBetween(serverStartKey, serverEndKey).andDateDeletedIsNull();
			
			List<org.opensrp.domain.postgres.Client> clients = clientMetadataMapper.selectMany(clientMetadataExample, 0,
			    DEFAULT_FETCH_SIZE, table);
			return convert(clients);
		}
		return new ArrayList<>();
	}
	
	@Override
	public List<Client> findAllClientByUpazila(String name, String table) {
		return convert(clientMapper.getClientByUpazila(name, table));
	}
	
	@Override
	public CustomQuery findTeamInfo(String username) {
		return clientMapper.getTeamInfo(username);
	}
	
	@Override
	public List<CustomQuery> getProviderLocationTreeByChildRole(int memberId, int childRoleId) {
		return clientMapper.getProviderLocationTreeByChildRole(memberId, childRoleId);
	}
	
	@Override
	public List<CustomQuery> getPALocationTreeByChildRole(int memberId, int childRoleId) {
		return clientMapper.getPALocationTreeByChildRole(memberId, childRoleId);
	}
	
	@Override
	public List<TargetDetails> getTargetDetails(String username, Long timestamp) {
		return clientMapper.selectTargetDetails(username, timestamp);
	}
	
	@Override
	public List<CustomQuery> getVillageByProviderId(int memberId, int childRoleId, int locationTagId) {
		return clientMapper.getVillageByProviderId(memberId, childRoleId, locationTagId);
	}
	
	@Override
	public List<CustomQuery> getProviderLocationIdByChildRole(int memberId, int childRoleId, int locationTagId) {
		return clientMapper.getProviderLocationIdByChildRole(memberId, childRoleId, locationTagId);
	}
	
	// Private Methods
	protected List<Client> convert(List<org.opensrp.domain.postgres.Client> clients) {
		if (clients == null || clients.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<Client> convertedClients = new ArrayList<>();
		for (org.opensrp.domain.postgres.Client client : clients) {
			Client convertedClient = convert(client);
			if (convertedClient != null) {
				convertedClients.add(convertedClient);
			}
		}
		
		return convertedClients;
	}
	
	private Client convert(org.opensrp.domain.postgres.Client client) {
		if (client == null || client.getJson() == null || !(client.getJson() instanceof Client)) {
			return null;
		}
		
		return (Client) client.getJson();
	}
	
	private org.opensrp.domain.postgres.Client convert(Client client, Long primaryKey) {
		if (client == null) {
			return null;
		}
		
		org.opensrp.domain.postgres.Client pgClient = new org.opensrp.domain.postgres.Client();
		pgClient.setId(primaryKey);
		pgClient.setJson(client);
		
		return pgClient;
	}
	
	private ClientMetadata createMetadata(Client client, Long clientId) {
		try {
			
			ClientMetadata clientMetadata = new ClientMetadata();
			clientMetadata.setDocumentId(client.getId());
			clientMetadata.setBaseEntityId(client.getBaseEntityId());
			if (client.getBirthdate() != null) {
				clientMetadata.setBirthDate(client.getBirthdate().toDate());
			}
			clientMetadata.setClientId(clientId);
			clientMetadata.setFirstName(client.getFirstName());
			clientMetadata.setMiddleName(client.getMiddleName());
			clientMetadata.setLastName(client.getLastName());
			Map<String, String> addressFields = client.getAddresses().get(0).getAddressFields();
			
			if (userAddressInClientMetadata) {
				Address requiredAddress = new Address();
				
				for (Address address : client.getAddresses()) {
					if (address.getAddressType().equalsIgnoreCase(this.addressType)) {
						requiredAddress = address;
						break;
					}
				}
				
				if (requiredAddress != null) {
					clientMetadata.setAddress1(requiredAddress.getAddressField("address1"));
					clientMetadata.setAddress2(requiredAddress.getAddressField("address2"));
					clientMetadata.setAddress3(requiredAddress.getCityVillage());
					if (requiredAddress.getAddressField("address8") != null) {
						clientMetadata.setVillageId(Long.valueOf(requiredAddress.getAddressField("address8")));
						clientMetadata.setVillage(requiredAddress.getAddressField("address8"));
					}
				}
			}
			
			String relationalId = null;
			
			Map<String, List<String>> relationShips = client.getRelationships();
			
			if (relationShips.containsKey("family")) {
				relationalId = relationShips.get("family").get(0);
			} else if (relationShips.containsKey("family_head")) {
				relationalId = relationShips.get("family_head").get(0);
			}
			
			/*if (relationShips != null && !relationShips.isEmpty()) {
				for (Map.Entry<String, List<String>> maEntry : relationShips.entrySet()) {
					List<String> values = maEntry.getValue();
					if (values != null && !values.isEmpty()) {
						relationalId = values.get(0);
						break;
					}
				}
			}*/
			clientMetadata.setRelationalId(relationalId);
			
			String uniqueId = null;
			String openmrsUUID = null;
			Map<String, String> identifiers = client.getIdentifiers();
			if (identifiers != null && !identifiers.isEmpty()) {
				for (Map.Entry<String, String> entry : identifiers.entrySet()) {
					String value = entry.getValue();
					if (StringUtils.isNotBlank(value)) {
						if (AllConstants.Client.OPENMRS_UUID_IDENTIFIER_TYPE.equalsIgnoreCase(entry.getKey())) {
							openmrsUUID = value;
						} else {
							uniqueId = value;
						}
					}
				}
			}
			
			clientMetadata.setUniqueId(uniqueId);
			clientMetadata.setOpenmrsUuid(openmrsUUID);
			clientMetadata.setServerVersion(client.getServerVersion());
			if (client.getDateVoided() != null)
				clientMetadata.setDateDeleted(client.getDateVoided().toDate());
			return clientMetadata;
		}
		catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected Long retrievePrimaryKey(Client t, String table) {
		Object uniqueId = getUniqueField(t);
		if (uniqueId == null) {
			return null;
		}
		
		String baseEntityId = uniqueId.toString();
		
		ClientMetadataExample clientMetadataExample = new ClientMetadataExample();
		clientMetadataExample.createCriteria().andBaseEntityIdEqualTo(baseEntityId).andDateDeletedIsNull();
		
		org.opensrp.domain.postgres.Client pgClient = clientMetadataMapper.selectOne(baseEntityId, table);
		if (pgClient == null) {
			return null;
		}
		return pgClient.getId();
	}
	
	@Override
	protected Object getUniqueField(Client t) {
		if (t == null) {
			return null;
		}
		return t.getBaseEntityId();
	}
	
	@Override
	public CustomQuery findUserStatus(String username) {
		// TODO Auto-generated method stub
		return clientMapper.selectUserStatus(username);
	}
	
	@Override
	public CustomQuery findUserId(String username) {
		// TODO Auto-generated method stub
		return clientMapper.findUserId(username);
	}
	
	@Override
	public CustomQuery getMaxHealthId(Integer locationId) {
		// TODO Auto-generated method stub
		return clientMapper.getMaxHealthId(locationId);
	}
	
	@Override
	public void updateAppVersion(String username, String version) {
		clientMapper.updateAppVersion(username, version);
		
	}
	
	@Override
	public Integer findClientIdByBaseEntityId(String baseEntityId, String table) {
		// TODO Auto-generated method stub
		return clientMetadataMapper.findClientIdByBaseEntityId(baseEntityId, table);
	}
	
	@Override
	public Client findClientByClientId(Integer clientId, String table) {
		if (clientId == null) {
			return null;
		}
		
		org.opensrp.domain.postgres.Client pgClient = clientMetadataMapper.findClientByClientId(clientId, table);
		if (pgClient != null) {
			return convert(pgClient);
		}
		return null;
	}
	
	@Override
	public List<CustomQuery> getDistrictAndUpazila(Integer parentLocationTag) {
		return clientMapper.getDistrictAndUpazila(parentLocationTag);
	}
	
	@Override
	public CustomQuery imeiCheck(String imeiNumber) {
		return clientMapper.imeiCheck(imeiNumber);
	}
	
	@Override
	public String getIsResync(String username) {
		
		return clientMetadataMapper.selectIsResync(username);
	}
	
	@Override
	public UserLocationTableName getUserLocation(String username) {
		
		return clientMapper.selectUserLocation(username);
	}
	
	@Override
	public List<WebNotification> getWebNotifications(String username, Long timestamp) {
		
		return clientMetadataMapper.selectWebNotifications(username, timestamp);
	}
	
	@Override
	public List<StockInfo> getStockInfos(String username, Long timestamp) {
		return clientMetadataMapper.selectStockInfos(username, timestamp);
	}
	
	@Override
	public CustomQuery getGuestMaxHealthId(Integer locationId) {
		
		return clientMapper.selectGuestMaxHealthId(locationId);
	}
	
	@Override
	public List<Client> findByRelationshipId(String relationshipId, String table) {
		List<org.opensrp.domain.postgres.Client> clients = clientMapper.selectByRelationshipId(relationshipId, table);
		return convert(clients);
		
	}
	
	@Override
	public String findBranchId(String baseEntityId, String table) {
		
		return clientMetadataMapper.selectBranchId(baseEntityId, table);
	}
	
	@Override
	public Integer addMigration(Migration migration) {
		return clientMapper.insertMigration(migration);
		
	}
	
	@Override
	public Migration findMigrationById(Long id) {
		
		return clientMapper.selectMigrationById(id);
	}
	
	@Override
	public List<Migration> findMigrationByIdRelationId(String relationalId) {
		
		return clientMapper.selectMigrationByIdRelationId(relationalId);
	}
	
}
