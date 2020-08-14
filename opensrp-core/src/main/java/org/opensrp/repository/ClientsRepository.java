package org.opensrp.repository;

import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.opensrp.domain.Client;
import org.opensrp.domain.postgres.CustomQuery;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;

public interface ClientsRepository extends CustomBaseRepository<Client> {
	
	Client findByBaseEntityId(String baseEntityId, String table);
	
	List<Client> findAllClients(String table);
	
	List<Client> findAllByIdentifier(String identifier, String table);
	
	List<Client> findAllByIdentifier(String identifierType, String identifier, String table);
	
	List<Client> findAllByAttribute(String attributeType, String attribute, String table);
	
	List<Client> findAllByMatchingName(String nameMatches, String table);
	
	/**
	 * Find a client based on the relationship id and between a range of date created dates e.g
	 * given mother's id get children born at a given time
	 *
	 * @param relationalId
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
	List<Client> findByRelationshipIdAndDateCreated(String relationalId, String dateFrom, String dateTo, String table);
	
	List<Client> findByRelationshipId(String relationshipType, String entityId, String table);
	
	List<Client> findByCriteria(ClientSearchBean searchBean, AddressSearchBean addressSearchBean, String table);
	
	List<Client> findByDynamicQuery(String query);
	
	List<Client> findByCriteria(ClientSearchBean searchBean, String table);
	
	List<Client> findByCriteria(AddressSearchBean addressSearchBean, DateTime lastEditFrom, DateTime lastEditTo, String table);
	
	List<Client> findByRelationShip(String relationIndentier, String table);
	
	List<Client> findByEmptyServerVersion(String table);
	
	List<Client> findByServerVersion(long serverVersion, String table);
	
	List<Client> findByFieldValue(String field, List<String> ids, String table);
	
	List<Client> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar, String table);
	
	List<Client> findAllClientByUpazila(String name, String table);
	
	CustomQuery findTeamInfo(String username);
	
	List<CustomQuery> getProviderLocationTreeByChildRole(int memberId, int childRoleId);
	
	List<CustomQuery> getVillageByProviderId(int memberId, int childRoleId, int locationTagId);
	
	List<CustomQuery> getProviderLocationIdByChildRole(int memberId, int childRoleId, int locationTagId);
	
	CustomQuery findUserStatus(String username);
	
	CustomQuery findUserId(String username);
	
	CustomQuery getMaxHealthId(Integer locationId);
	
	void updateAppVersion(String username, String version);
	
	Integer findClientIdByBaseEntityId(String baseEntityId, String table);
	
	Client findClientByClientId(Integer clientId, String table);
	
	public List<CustomQuery> getDistrictAndUpazila(Integer parentLocationTag);
	
	CustomQuery imeiCheck(String imeiNumber);
	
	String getIsResync(String username);
}
