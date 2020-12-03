package org.opensrp.repository.postgres.mapper.custom;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.opensrp.domain.postgres.Client;
import org.opensrp.domain.postgres.ClientMetadataExample;
import org.opensrp.domain.postgres.StockInfo;
import org.opensrp.domain.postgres.WebNotification;
import org.opensrp.repository.postgres.mapper.ClientMetadataMapper;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;

public interface CustomClientMetadataMapper extends ClientMetadataMapper {
	
	List<Client> selectMany(@Param("example") ClientMetadataExample example, @Param("offset") int offset,
	                        @Param("limit") int limit, @Param("table") String table);
	
	Client selectOne(@Param("baseEntityId") String baseEntityId, @Param("table") String table);
	
	Client selectByDocumentId(@Param("documentId") String documentId, @Param("table") String table);
	
	List<Client> selectBySearchBean(@Param("clientBean") ClientSearchBean searchBean,
	                                @Param("addressBean") AddressSearchBean addressSearchBean, @Param("offset") int offset,
	                                @Param("limit") int limit, @Param("table") String table);
	
	List<Client> selectByName(@Param("name") String nameMatches, @Param("offset") int offset, @Param("limit") int limit,
	                          @Param("table") String table);
	
	Integer findClientIdByBaseEntityId(@Param("baseEntityId") String baseEntityId, @Param("table") String table);
	
	Client findClientByClientId(@Param("clientId") Integer clientId, @Param("table") String table);
	
	String selectIsResync(String username);
	
	List<WebNotification> selectWebNotifications(@Param("username") String username, @Param("timestamp") Long timestamp);
	
	List<StockInfo> selectStockInfos(@Param("username") String username, @Param("timestamp") Long timestamp);
	
}
