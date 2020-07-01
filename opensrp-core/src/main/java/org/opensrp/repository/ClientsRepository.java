package org.opensrp.repository;

import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.opensrp.domain.Client;
import org.opensrp.domain.postgres.CustomQuery;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;

public interface ClientsRepository extends BaseRepository<Client> {
	
	Client findByBaseEntityId(String baseEntityId);
	
	List<Client> findAllClients();
	
	List<Client> findAllByIdentifier(String identifier);
	
	List<Client> findAllByIdentifier(String identifierType, String identifier);
	
	List<Client> findAllByAttribute(String attributeType, String attribute);
	
	List<Client> findAllByMatchingName(String nameMatches);
	
	/**
	 * Find a client based on the relationship id and between a range of date created dates e.g
	 * given mother's id get children born at a given time
	 *
	 * @param relationalId
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 */
	List<Client> findByRelationshipIdAndDateCreated(String relationalId, String dateFrom, String dateTo);
	
	List<Client> findByRelationshipId(String relationshipType, String entityId);
	
	List<Client> findByCriteria(ClientSearchBean searchBean, AddressSearchBean addressSearchBean);
	
	List<Client> findByDynamicQuery(String query);
	
	List<Client> findByCriteria(ClientSearchBean searchBean);
	
	List<Client> findByCriteria(AddressSearchBean addressSearchBean, DateTime lastEditFrom, DateTime lastEditTo);
	
	List<Client> findByRelationShip(String relationIndentier);
	
	List<Client> findByEmptyServerVersion();
	
	List<Client> findByServerVersion(long serverVersion);
	
	List<Client> findByFieldValue(String field, List<String> ids);
	
	List<Client> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar);
	
	List<Client> findAllClientByUpazila(String name);
	
	CustomQuery findTeamInfo(String username);
	
	List<CustomQuery> getProviderLocationTreeByChildRole(int memberId, int childRoleId);
	
	List<CustomQuery> getVillageByProviderId(int memberId, int childRoleId, int locationTagId);
	
	List<CustomQuery> getProviderLocationIdByChildRole(int memberId, int childRoleId, int locationTagId);
	
	CustomQuery findUserStatus(String username);
	
	CustomQuery findUserId(String username);
	
	CustomQuery getMaxHealthId(Integer locationId);
	
	void updateAppVersion(String username, String version);
	
	Integer findClientIdByBaseEntityId(String baseEntityId);
	
	Client findClientByClientId(Integer clientId);
	
	public List<CustomQuery> getDistrictAndUpazila(Integer parentLocationTag);
	
	CustomQuery imeiCheck(String imeiNumber);
	
	String getIsResync(String username);
}
