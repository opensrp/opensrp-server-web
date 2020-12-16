package org.opensrp.repository;

import java.util.Calendar;
import java.util.List;

import org.opensrp.domain.CustomEventMeta;
import org.opensrp.domain.Event;
import org.opensrp.domain.postgres.CustomQuery;
import org.opensrp.domain.postgres.HealthId;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.EventSearchBean;

public interface EventsRepository extends CustomBaseRepository<Event> {
	
	List<Event> findAllByIdentifier(String identifier, String table);
	
	List<Event> findAllByIdentifier(String identifierType, String identifier, String table);
	
	Event findById(String id, String table);
	
	Event findByFormSubmissionId(String formSubmissionId, String table);
	
	List<Event> findByBaseEntityId(String baseEntityId, String table);
	
	Event findByBaseEntityAndFormSubmissionId(String baseEntityId, String formSubmissionId, String table);
	
	List<Event> findByBaseEntityAndType(String baseEntityId, String eventType, String table);
	
	List<Event> findByBaseEntityAndEventTypeContaining(String baseEntityId, String eventType, String table);
	
	List<Event> findEvents(EventSearchBean eventSearchBean, String table);
	
	List<Event> findEventsByDynamicQuery(String query, String table);
	
	List<Event> findByServerVersion(long serverVersion, String table);
	
	List<Event> notInOpenMRSByServerVersion(long serverVersion, Calendar calendar, String table);
	
	List<Event> notInOpenMRSByServerVersionAndType(String type, long serverVersion, Calendar calendar, String table);
	
	List<Event> findByClientAndConceptAndDate(String baseEntityId, String concept, String conceptValue, String dateFrom,
	                                          String dateTo, String table);
	
	List<Event> findByBaseEntityIdAndConceptParentCode(String baseEntityId, String concept, String parentCode, String table);
	
	List<Event> findByConceptAndValue(String concept, String conceptValue, String table);
	
	List<Event> findByEmptyServerVersion(String table);
	
	List<Event> findEvents(EventSearchBean eventSearchBean, String sortBy, String sortOrder, int limit, String table);
	
	List<Event> findEventByEventTypeBetweenTwoDates(String eventType, String table);
	
	List<Event> findByProvider(String provider, String table);
	
	List<Event> findByFieldValue(String field, List<String> ids, long serverVersion, String table);
	
	void deleteByPrimaryKey(Event event, String table);
	
	List<CustomQuery> getLocations(int userId);
	
	CustomQuery getUser(String userName);
	
	CustomQuery getTeamMemberId(int userId);
	
	int updateHealthId(HealthId healthId);
	
	List<HealthId> gethealthIds(boolean status, String type);
	
	List<Event> selectBySearchBean(AddressSearchBean addressSearchBean, long serverVersion, String providerId, int limit,
	                               String table);
	
	List<Event> selectBySearchBean(int userId, long serverVersion, String providerId, int limit, String table);
	
	Integer findEventIdByFormSubmissionId(String formSubmissionId, String table);
	
	Event findEventByEventId(Integer eventId, String table);
	
	List<Event> selectByProvider(long serverVersion, String providerId, int limit, String table);
	
	int insertHealthId(HealthId healthId);
	
	List<String> getHouseholdId(Integer maxid);
	
	List<CustomQuery> getRoles(int userId);
	
	List<String> getGuestHouseholdId(Integer maxid);
	
	int insertGuestHealthId(HealthId healthId);
	
	CustomEventMeta findFirstEventMeta(String baseEntityId, String table);
	
	List<Event> findEventByBaseEntityId(String baseEntityId, String table);
}
