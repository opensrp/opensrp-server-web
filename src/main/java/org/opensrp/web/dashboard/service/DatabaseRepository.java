package org.opensrp.web.dashboard.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.domain.postgres.Report;
import org.opensrp.web.dashboard.util.SearchBuilder;

/**
 * <p>
 * This is the central API class abstracting the notion of a persistence service.<br>
 * </p>
 * <br>
 * The main function of the <tt>DatabaseRepository</tt> is to offer create, read and delete
 * operations for instances of mapped entity classes.<br>
 * Transient instances may be made persistent by calling <tt>save()</tt>, <br>
 * Transient instances may be made update by calling <tt>update()</tt>, <br>
 * Transient instances may be removed from persistent by calling <tt>delete()</tt>, <br>
 * <h1>Some Query API for Entity class:<h1><br />
 * <ul>
 * <li>{@link #findById(int, String, Class)}</li>
 * <li>{@link #findAllByKey(String, String, Class)}</li>
 * <li>{@link #findByKey(String, String, Class)}</li>
 * <li>{@link #findByKeys(Map, Class)}</li>
 * <li>{@link #findLastByKey(Map, String, Class)}</li>
 * <li>{@link #findLastByKeyLessThanDateConditionOneField(Map, Date, String, String, Class)}</li>
 * <li>{@link #findAllByKeys(Map, Class)}</li>
 * <li>{@link #findAllByKeysWithALlMatches(boolean, Map, Class)}</li>
 * <li>{@link #isExists(String, String, Class)}</li>
 * <li>{@link #entityExistsNotEualThisId(int, Object, String, Class)}</li>
 * <li>{@link #findAllByKey(String, String, Class)}</li>
 * <li>{@link #search(SearchBuilder, int, int, Class)}</li>
 * <li>{@link #countBySearch(SearchBuilder, Class)}</li>
 * <li>{@link #countBySearch(SearchBuilder, Class)}</li>
 * </ul>
 * <p>
 * <br />
 * <h1>Some Query API for View:
 * <h1><br />
 * <ul>
 * <li>{@link #getDataFromViewByBEId(String, String, String)}</li>
 * <li>{@link #getDataFromView(SearchBuilder, int, int, String, String, String)}</li>
 * <li>{@link #getViewDataSize(SearchBuilder, String, String)}</li>
 * </ul>
 * </p>
 * <p>
 * <br />
 * <h1>Some Query API for RAW :
 * <h1><br />
 * <ul>
 * <li>{@link #findAll(String)}</li>
 * <li>{@link #executeSelectQuery(String, Map)}</li>
 * <li>{@link #executeSelectQuery(String)}</li>
 * <li>{@link #getDataFromSQLFunction(SearchBuilder, Query, Session)}</li>
 * </ul>
 * </p>
 * 
 * @author proshanto
 * @author nursat
 * @author prince
 * @version 0.1.0
 * @since 2018-05-30
 */
public interface DatabaseRepository {
	
	public <T> long save(T t) throws Exception;

	public <T> long saveAll(List<T> t) throws Exception;

	public <T> int update(T t);
	
	public <T> boolean delete(T t);
	
	public <T> T findById(int id, String fieldName, Class<?> className);

	public <T> T findByForeignKey(int id, String fieldName, String className);

	public <T> List<T> findAllByForeignKey(int id, String fieldName, String className);

	public <T> T findByKey(String value, String fieldName, Class<?> className);
	
	public <T> List<T> findAll(String tableClass);
	
	public <T> T findByKeys(Map<String, Object> fielaValues, Class<?> className);
	
	public <T> T findLastByKey(Map<String, Object> fielaValues, String orderByFieldName, Class<?> className);
	
	public <T> T findLastByKeyLessThanDateConditionOneField(Map<String, Object> fielaValues, Date fieldvalue, String field,
	                                                        String orderByFieldName, Class<?> className);
	
	public <T> List<T> findAllByKeys(Map<String, Object> fielaValues, Class<?> className);
	
	public <T> List<T> findAllByKeysWithALlMatches(boolean isProvider, Map<String, String> fielaValues, Class<?> className);
	
	public boolean isExists(Map<String, Object> fielaValues, Class<?> className);
	
	public <T> boolean entityExistsNotEualThisId(int id, T value, String fieldName, Class<?> className);
	
	public <T> List<T> findAllByKey(String value, String fieldName, Class<?> className);
	
	public List<Object[]> executeSelectQuery(String sqlQuery, Map<String, Object> params);
	
	public <T> List<T> executeSelectQuery(String sqlQuery);
	
	public <T> List<T> search(SearchBuilder searchBuilder, int result, int offsetreal, Class<?> entityClassName);;
	
	public int countBySearch(SearchBuilder searchBuilder, Class<?> entityClassName);
	
	public <T> List<T> getDataFromViewByBEId(String viewName, String entityType, String baseEntityId);
	
	public <T> List<T> getDataFromView(SearchBuilder searchBuilder, int maxRange, int offsetreal, String viewName,
	                                   String entityType, String orderingBy);
	
	public int getViewDataSize(SearchBuilder searchBuilder, String viewName, String entityType);
	
	public <T> List<T> getDataFromSQLFunction(Query query, Session session);

	public <T> List<T> getDataByMHV(String username);

	public <T> List<T> getMemberListByHousehold(String householdBaseId, String mhvId);

	public <T> T getMemberByHealthId(String healthId);

	public <T> T getMemberByBaseEntityId(String baseEntityId);

	public <T> List<T> getMemberListByCC(String ccName);

	public <T> List<T> getUpazilaList();

	public <T> List<T> getCCListByUpazila(SearchBuilder searchBuilder);

	public List<Report> getMHVListFilterWise(String filterString);

	public <T> List<T> getCatchmentArea(int userId);

	public <T> List<T> getVillageIdByProvider(int memberId, int childRoleId, int locationTagId);

	public <T> T countByField(int id, String fieldName, String className);

	public List<PhysicalLocation> getProviderLocationTreeByChildRole(int memberId, int childRoleId);
	
	public List<Object[]> getHouseHoldReports(String startDate, String endDate, String filterString,String searched_value,List<Object[]> allSKs);

	public List<Object[]> getAllSK(List<Object[]> branches);

	public List<Object[]> getSKByBranch(Integer branchId);

	public <T> List<T> getUniqueLocation(String village, String ward);

	public List<Object[]> getClientInformation();

	public List<Object[]> getClientInfoFilter(String startTime, String endTime, String formName, String sk, List<Object[]> allSKs, Integer pageNumber);

	public Integer getClientInfoFilterCount(String startTime, String endTime, String formName, String sk, List<Object[]> allSKs);

	List<Object[]> getExportByCreator(String username);

	public List<Object[]> getUserListByFilterString(int locationId, int locationTagId, int roleId, int branchId);

	public List<Object[]> getUserListWithoutCatchmentArea(int roleId, int branchId);
}
