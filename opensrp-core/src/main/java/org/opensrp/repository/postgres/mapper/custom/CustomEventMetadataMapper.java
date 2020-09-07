package org.opensrp.repository.postgres.mapper.custom;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.opensrp.domain.postgres.Event;
import org.opensrp.domain.postgres.EventMetadataExample;
import org.opensrp.repository.postgres.mapper.EventMetadataMapper;
import org.opensrp.search.AddressSearchBean;

public interface CustomEventMetadataMapper extends EventMetadataMapper {
	
	Event selectByDocumentId(@Param("documentId") String documentId, @Param("table") String table);
	
	List<Event> selectMany(@Param("example") EventMetadataExample eventMetadataExample, @Param("table") String table);
	
	List<Event> selectManyWithRowBounds(@Param("example") EventMetadataExample example, @Param("offset") int offset,
	                                    @Param("limit") int limit, @Param("table") String table);
	
	List<Event> selectNotInOpenMRSByServerVersion(@Param("from") long serverVersion, @Param("to") long calendar,
	                                              @Param("limit") int limit, @Param("table") String table);
	
	List<Event> selectNotInOpenMRSByServerVersionAndType(@Param("eventType") String type, @Param("from") long serverVersion,
	                                                     @Param("to") long calendar, @Param("limit") int limit,
	                                                     @Param("table") String table);
	
	List<Event> selectBySearchBean(@Param("addressBean") AddressSearchBean addressSearchBean,
	                               @Param("serverVersion") long serverVersion, @Param("providerId") String providerId,
	                               @Param("limit") int limit, @Param("table") String table);
	
	Integer findEventIdByFormSubmissionId(@Param("formSubmissionId") String formSubmissionId, @Param("table") String table);
	
	Event findEventByEventId(@Param("eventId") Integer eventId, @Param("table") String table);
	
	List<Event> selectByProvider(@Param("serverVersion") long serverVersion, @Param("providerId") String providerId,
	                             @Param("limit") int limit, @Param("table") String table);
	
	List<String> getHouseholdId(@Param("maxId") Integer maxId, @Param("maxIdPlus") Integer maxIdPlus);
	
	List<Event> selectBySearchBeanFromFunction(@Param("userId") int userId, @Param("serverVersion") long serverVersion,
	                                           @Param("providerId") String providerId, @Param("limit") int limit,
	                                           @Param("table") String table);
}
