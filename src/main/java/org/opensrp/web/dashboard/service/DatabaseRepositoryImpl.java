package org.opensrp.web.dashboard.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.domain.postgres.Report;
import org.opensrp.web.dashboard.util.SearchBuilder;
import org.opensrp.web.dashboard.util.SearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Concrete implementation of a DatabaseRepository.<br/>
 * The main contract here is the communication with hibernate repository through
 * {@link #sessionFactory}.currently Various types of query and database operation are supported
 * both entity class and view, but has a lot of chance to improve it and its gets maturity day by
 * day.So its only perform Database operation related action.<br/>
 * </p>
 * <b>Exposes One interface:</b>
 * <ul>
 * <li>{@link DatabaseRepository} to the application</li>
 * </ul>
 * <br/>
 * This class is not thread-safe.
 * 
 * @author proshanto
 * @author nursat
 * @author prince
 * @version 0.1.0
 * @since 2018-05-30
 */
@Repository
public class DatabaseRepositoryImpl implements DatabaseRepository {
	
	private static final Logger logger = Logger.getLogger(DatabaseRepositoryImpl.class);
	private static final int SK_ID = 28;
	private static final int VILLAGE_ID = 33;
	private static final int UNION_ID = 32;

	@Autowired
	private SessionFactory sessionFactory;

	private org.hibernate.Session getCurrentSession(){
	    return sessionFactory.openSession();
	}
	
	public DatabaseRepositoryImpl() {
		
	}
	
	/**
	 * Save data object to persistent through Hibernate {@link #sessionFactory}
	 * 
	 * @param t is data object.
	 * @exception Exception
	 * @return 1 for success and -1 for failure.
	 */
	@Override
	public <T> long save(T t) throws Exception {
		Session session = getCurrentSession();
		Transaction tx = null;
		long returnValue = -1;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(t);
			logger.info("saved successfully: " + t.getClass().getName());
			returnValue = 1;
			if (!tx.wasCommitted())
				tx.commit();
		}
		catch (HibernateException e) {
			returnValue = -1;
			tx.rollback();
			logger.error(e);
			throw new Exception(e.getMessage());
		}
		finally {
			session.close();
			
		}
		return returnValue;
	}

	@Override
	public <T> long saveAll(List<T> t) throws Exception {
		System.out.println("SAVE ALL");
		Session session = getCurrentSession();
		Transaction tx = null;
		long returnValue = -1;
		try {
			tx = session.beginTransaction();
			System.out.println("Save.... "+ t.size());
			for (int i = 0; i < t.size(); i++) {
				System.out.println(""+t.toString());
				session.saveOrUpdate(t.get(i));
			}
			logger.info("saved successfully: " + t.getClass().getName());
			returnValue = 1;
			if (!tx.wasCommitted())
				tx.commit();
		}
		catch (HibernateException e) {
			returnValue = -1;
			tx.rollback();
			logger.error(e);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		finally {
			session.close();

		}
		return returnValue;
	}
	
	/**
	 * Update data object to persistent through Hibernate {@link #sessionFactory}
	 * 
	 * @param t is data object.
	 * @exception Exception
	 * @return 1 for success and -1 for failure.
	 */
	@Override
	public <T> int update(T t) {
		Session session = getCurrentSession();
		Transaction tx = null;
		int returnValue = -1;
		try {
			tx = session.beginTransaction();
			session.saveOrUpdate(t);
			logger.info("updated successfully");
			if (!tx.wasCommitted())
				tx.commit();
			returnValue = 1;
		}
		catch (HibernateException e) {
			returnValue = -1;
			tx.rollback();
			logger.error(e);
		}
		finally {
			session.close();
		}
		return returnValue;
	}
	
	/**
	 * Delete data object from persistent through Hibernate {@link #sessionFactory}
	 * 
	 * @param t is data object.
	 * @exception Exception
	 * @return true for success and false for failure.
	 */
	
	@Override
	public <T> boolean delete(T t) {
		Session session = getCurrentSession();
		Transaction tx = null;
		System.out.println("DELETE METHOD A ASHCHHE");
		boolean returnValue = false;
		try {
			tx = session.beginTransaction();
			logger.info("deleting: " + t.getClass().getName());
			session.delete(t);
			if (!tx.wasCommitted())
				tx.commit();
			returnValue = true;
		}
		catch (HibernateException e) {
			returnValue = false;
			tx.rollback();
			logger.error(e);
		}
		finally {
			session.close();
		}
		return returnValue;
	}
	
	/**
	 * <p>
	 * {@link #findById(int, String, Class)} fetch entity by {@link #sessionFactory}. This is a
	 * common method for all Entity class, so it works for any entity class.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> findById(12,id,User.class).
	 * 
	 * @param id is unique id of a entity (primary key of a table and type should be int).
	 * @param fieldName is name of primary key of a entity class.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return Entity object or null.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T findById(int id, String fieldName, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		criteria.add(Restrictions.eq(fieldName, id));
		List<T> result = criteria.list();
		session.close();
		return (T) (result.size() > 0 ? (T) result.get(0) : null);
	}

	public <T> T findByForeignKey(int id, String fieldName, String className) {
		Session session = getCurrentSession();
		String hql = "from "+className+" where " + fieldName + " = :id";
		List<T> result = session.createQuery(hql).setInteger("id", id).list();
		session.close();
		return (T) (result.size() > 0 ? (T) result.get(0) : null);
	}

	public <T> List<T> findAllByForeignKey(int id, String fieldName, String className) {
		Session session = getCurrentSession();
		String hql = "from "+className+" where " + fieldName + " = :id";
		List<T> result = session.createQuery(hql).setInteger("id", id).list();
		session.close();
		return (result.size() > 0 ? result : null);
	}
	
	/**
	 * <p>
	 * {@link #findByKey(String, String, Class)} fetch entity by {@link #sessionFactory}. This is a
	 * common method for all Entity class, so it works for any entity class.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> findByKey("john", "name", User.class).
	 * 
	 * @param fieldName is field or property name of Entity class and type should be String.
	 * @param value is given value type String.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return Entity object or null.
	 */
	
	@Override
	public <T> T findByKey(String value, String fieldName, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		criteria.add(Restrictions.eq(fieldName, value));
		@SuppressWarnings("unchecked")
		List<T> result = criteria.list();
		session.close();
		return (T) (result.size() > 0 ? (T) result.get(0) : null);
	}
	
	/**
	 * <p>
	 * {@link #findByKeys(Map, Class)} fetch entity by {@link #sessionFactory}. This is a common
	 * method for all Entity class, so it works for any entity class.its returns first record of the
	 * Record set.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> Map<String, Object> params = new HashMap<String, Object>();
	 * params.put("parentId", parentId); findByKeys(params, User.class).
	 * 
	 * @param fielaValues is map of field and corresponding value.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return Entity object or null.
	 */
	
	@Override
	public <T> T findByKeys(Map<String, Object> fielaValues, Class<?> className) {
		Session session = getCurrentSession();
		@SuppressWarnings("unchecked")
		List<T> result = null;
		try {
			Criteria criteria = session.createCriteria(className);
			for (Map.Entry<String, Object> entry : fielaValues.entrySet()) {
				criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
			}
			result = criteria.list();
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return (T) (result.size() > 0 ? (T) result.get(0) : null);
	}
	
	/**
	 * <p>
	 * {@link #findLastByKey(Map, String, Class)} fetch entity by {@link #sessionFactory}. This is a
	 * common method for all Entity class, so it works for any entity class.its returns last record
	 * of the Record set.its only support descending order.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> Map<String, Object> params = new HashMap<String, Object>();
	 * params.put("parentId", parentId); findLastByKey(params,"id", User.class).
	 * 
	 * @param fielaValues is map of field and corresponding value.
	 * @param orderByFieldName is name of field where ordering is applied.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return Entity object or null.
	 */
	@Override
	public <T> T findLastByKey(Map<String, Object> fielaValues, String orderByFieldName, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		for (Map.Entry<String, Object> entry : fielaValues.entrySet()) {
			criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
		}
		criteria.addOrder(Order.desc(orderByFieldName));
		@SuppressWarnings("unchecked")
		List<T> result = criteria.list();
		session.close();
		return (T) (result.size() > 0 ? (T) result.get(0) : null);
	}
	
	/**
	 * <p>
	 * {@link #findLastByKeyLessThanDateConditionOneField(Map, Date, String, String, Class)} fetch
	 * entity by {@link #sessionFactory}. This is a common method for all Entity class, so it works
	 * for any entity class.its returns last record of the Record set.its only support descending
	 * order.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> Map<String, Object> params = new HashMap<String, Object>();
	 * params.put("parentId", parentId); findLastByKeyLessThanDateConditionOneField(params,
	 * 2018-10-15,"created","id,User,class).
	 * 
	 * @param fielaValues is map of field and corresponding value.
	 * @param fieldDateValue is condition date value.
	 * @param field is name of fieldDate where fieldDateValue is imposed.
	 * @param orderByFieldName is name of field where ordering is applied.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return Entity object or null.
	 */
	@Override
	public <T> T findLastByKeyLessThanDateConditionOneField(Map<String, Object> fielaValues, Date fieldDateValue,
	                                                        String field, String orderByFieldName, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		for (Map.Entry<String, Object> entry : fielaValues.entrySet()) {
			criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
		}
		criteria.add(Restrictions.lt(field, fieldDateValue));
		criteria.addOrder(Order.desc(orderByFieldName));
		@SuppressWarnings("unchecked")
		List<T> result = criteria.list();
		session.close();
		return (T) (result.size() > 0 ? (T) result.get(0) : null);
	}
	
	/**
	 * <p>
	 * {@link #findAllByKeys(Map, Class)} fetch entity by {@link #sessionFactory}. This is a common
	 * method for all Entity class, so it works for any entity class.its returns all records of the
	 * result.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> Map<String, Object> params = new HashMap<String, Object>();
	 * params.put("parentId", parentId); findAllByKeys(params, User.class).
	 * 
	 * @param fielaValues is map of field and corresponding value.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return List of Entity object or null.
	 */
	
	@Override
	public <T> List<T> findAllByKeys(Map<String, Object> fielaValues, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		for (Map.Entry<String, Object> entry : fielaValues.entrySet()) {
			criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		}
		
		@SuppressWarnings("unchecked")
		List<T> result = criteria.list();
		session.close();
		return (List<T>) (result.size() > 0 ? (List<T>) result : null);
	}
	
	/**
	 * <p>
	 * {@link #findAllByKeysWithALlMatches(boolean, Map, Class)} fetch entity by
	 * {@link #sessionFactory}. This is a common method for all Entity class, so it works for any
	 * entity class.its returns all records of the result.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> Map<String, Object> params = new HashMap<String, Object>();
	 * params.put("parentId", parentId); findAllByKeysWithALlMatches(false,params, User.class).
	 * 
	 * @param isProvider is a boolean value which only imposed for user Entity(if needs only
	 *            provider list form User list then use true otherwise false always).
	 * @param fielaValues is map of field and corresponding value.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return List of Entity object or null.
	 */

	
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> findAllByKeysWithALlMatches(boolean isProvider, Map<String, String> fielaValues, Class<?> className) {
		Session session = getCurrentSession();
		List<T> result = null;
		try {
			Criteria criteria = session.createCriteria(className);
			for (Map.Entry<String, String> entry : fielaValues.entrySet()) {
				criteria.add(Restrictions.ilike(entry.getKey(), entry.getValue(), MatchMode.ANYWHERE));
			}
			if (isProvider) {
				criteria.add(Restrictions.eq("provider", true));
			}
			result = criteria.list();
		}catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		
		if(result!= null){
			return (List<T>) (result.size() > 0 ? (List<T>) result : null);
		}
		
		return null;
	}
	
	/**
	 * <p>
	 * {@link #isExists(String, String, Class)} fetch entity by {@link #sessionFactory}. This is a
	 * common method for all Entity class, so it works for any entity class.its returns true or
	 * false depends on query result.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> Map<String, Object> params = new HashMap<String, Object>();
	 * params.put("parentId", parentId); isExists(params, User.class).
	 * 
	 * @param fielaValues is map of field and corresponding value.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return boolean value.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean isExists(Map<String, Object> fielaValues, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		for (Map.Entry<String, Object> entry : fielaValues.entrySet()) {
			criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		}
		List<Object> result = criteria.list();
		session.close();
		return (result.size() > 0 ? true : false);
	}
	
	/**
	 * <p>
	 * {@link #entityExistsNotEualThisId(String, String, Class)} fetch entity by
	 * {@link #sessionFactory}. This is a common method for all Entity class, so it works for any
	 * entity class.This method is only purpose of data editing checked.Data updating time its
	 * requires to know any records exists with the sane name except this Entity.suppose userName is
	 * unique at updating time of user information same userName should gets update with same
	 * entity, it this scenario this method help us.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> Map<String, Object> params = new HashMap<String, Object>();
	 * params.put("parentId", parentId); isExists(params, User.class).
	 * 
	 * @param fielaValues is map of field and corresponding value.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return boolean value.
	 */
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> boolean entityExistsNotEualThisId(int id, T value, String fieldName, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		criteria.add(Restrictions.eq(fieldName, value));
		criteria.add(Restrictions.ne("id", id));
		List<Object> result = criteria.list();
		session.close();
		return (result.size() > 0 ? true : false);
	}
	
	/**
	 * <p>
	 * {@link #findAll(String)} fetch entity by {@link #sessionFactory}. This is a common method for
	 * RWA Query.List of all Objects.This method directly communicate with database by database
	 * table name.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> findAll("user").
	 * 
	 * @param tableClass is name of table name of database.
	 * @return List of Object or null.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> findAll(String tableClass) {
		Session session = getCurrentSession();
		List<T> result = null;
		try {
			Query query = session.createQuery("from " + tableClass + " t order by t.id desc");
			result = (List<T>) query.list();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		
		return (List<T>) result;
	}
	
	/**
	 * <p>
	 * {@link #findAllByKey(String, String, Class)} fetch entity by {@link #sessionFactory}. This is
	 * a common method for all Entity class, so it works for any entity class.its returns all
	 * records of the result.If only one key is for condition then this method may be used but
	 * {@link #findAllByKeys(Map, Class)} also an alternative option.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> findAllByKey("john","userName", User.class).
	 * 
	 * @param value is search string.
	 * @param fieldName is field name.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return List of Entity object or null.
	 */
	@Override
	public <T> List<T> findAllByKey(String value, String fieldName, Class<?> className) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(className);
		criteria.add(Restrictions.eq(fieldName, value));
		@SuppressWarnings("unchecked")
		List<T> result = criteria.list();
		session.close();
		return (List<T>) (result.size() > 0 ? (List<T>) result : null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Object[]> executeSelectQuery(String sqlQuery, Map<String, Object> params) {
		Session session = getCurrentSession();
		
		List<Object[]> results = null;
		try {
			SQLQuery query = session.createSQLQuery(sqlQuery);
			for (Map.Entry<String, Object> param : params.entrySet()) {
				query.setParameter(param.getKey(), param.getValue());
			}
			
			results = query.list();
			
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		return results;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> executeSelectQuery(String sqlQuery) {
		Session session = getCurrentSession();
		List<T> results = null;
		try {
			SQLQuery query = session.createSQLQuery(sqlQuery);
			results = query.list();
			
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		return results;
	}
	
	/**
	 * <p>
	 * {@link #search(SearchBuilder, int, int, Class)} fetch entity by {@link #sessionFactory}. This
	 * is a common method for all Entity class, so it works for any entity class.Its returns number
	 * of records defined to the configuration (default 10).This method supports pagination with
	 * search option.
	 * </p>
	 * *
	 * <p>
	 * maxRange -1 means maxResult does not consider. offsetreal -1 means setFirstResult does not
	 * consider.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> SearchBuilder searchBuilder; searchBuilder.setDistrict("DHAKA");
	 * search(searchBuilder,1,1, User.class).
	 * 
	 * @param searchBuilder is object of search option.
	 * @param maxResult is total records returns
	 * @param offsetreal is starting position of query.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return List of object or null.
	 */
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> search(SearchBuilder searchBuilder, int maxResult, int offsetreal, Class<?> entityClassName) {
		Session session = getCurrentSession();
		Criteria criteria = session.createCriteria(entityClassName);
		
		criteria = SearchCriteria.createCriteriaCondition(searchBuilder, criteria);
		
		if (offsetreal != -1) {
			criteria.setFirstResult(offsetreal);
		}
		if (maxResult != -1) {
			criteria.setMaxResults(maxResult);
		}
		criteria.addOrder(Order.desc("created"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		List<T> data = new ArrayList<T>();
		try {
			data = (List<T>) criteria.list();
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		
		return data;
	}
	
	/**
	 * <p>
	 * {@link #countBySearch(SearchBuilder, Class)} fetch entity by {@link #sessionFactory}. This is
	 * a common method for all Entity class, so it works for any entity class.its returns count of
	 * the result.This method supports search option.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> SearchBuilder searchBuilder; searchBuilder.setDistrict("DHAKA");
	 * search(searchBuilder, User.class).
	 * 
	 * @param searchBuilder is object of search option.
	 * @param className is name of Entity class who is mapped with database table.
	 * @return total count.
	 */
	@Override
	public int countBySearch(SearchBuilder searchBuilder, Class<?> entityClassName) {
		Session session = getCurrentSession();
		int count = 0;
		Criteria criteria = session.createCriteria(entityClassName);
		criteria = SearchCriteria.createCriteriaCondition(searchBuilder, criteria);
		try {
			count = criteria.list().size();
			
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		
		return count;
	}
	
	/**
	 * <p>
	 * {@link #getDataFromViewByBEId(String, String, String)} fetch entity by
	 * {@link #sessionFactory}. This is a common method for all View, so it works for any View.Its
	 * returns all records of the result.This method gets data by baseEntity ID.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> getDataFromViewByBEId("client","household",
	 * "112233-frtttt-huoie-345555").
	 * 
	 * @param viewName is name of View.
	 * @param entityType is name of ENtity Type Such as "child,member".
	 * @param baseEntityId is client unique id.
	 * @return List of Object or null.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getDataFromViewByBEId(String viewName, String entityType, String baseEntityId) {
		Session session = getCurrentSession();
		List<T> viewData = null;
		try {
			String hql = "SELECT * FROM core.\"" + viewName + "\" " + " where entity_type = '" + entityType + "'"
			        + " and base_entity_id = '" + baseEntityId + "'";
			Query query = session.createSQLQuery(hql);
			viewData = query.list();
			logger.info("data fetched successfully from " + viewName + ", data size: " + viewData.size());
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		return viewData;
	}
	
	/**
	 * <p>
	 * {@link #getDataFromView(SearchBuilder, int, int, String, String, String)} fetch entity by
	 * {@link #sessionFactory}.This is a common method for all View, so it works for any View.This
	 * method supports pagination with search option.
	 * <p>
	 * <p>
	 * maxRange -1 means setMaxResults does not consider. offsetreal -1 means setFirstResult does
	 * not consider.
	 * </p>
	 * 
	 * @param searchBuilder is search option list.
	 * @param offset is number of offset.
	 * @param maxRange is returned maximum number of data.
	 * @param viewName is name of target view.
	 * @param orderingBy is the order by condition of query.
	 * @param entityType is name of ENtity Type Such as "child,member".
	 * @return List<T>.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getDataFromView(SearchBuilder searchBuilder, int maxRange, int offsetreal, String viewName,
	                                   String entityType, String orderingBy) {
		Session session = getCurrentSession();
		List<T> viewData = null;
		try {
			String hql = "SELECT * FROM core.\"" + viewName + "\" " + " where entity_type = '" + entityType + "'  ";
			
			hql = setViewCondition(searchBuilder, hql);
			if (!orderingBy.isEmpty()) {
				hql += " order by " + orderingBy + " asc";
			}
			Query query = session.createSQLQuery(hql);
			if (offsetreal != -1) {
				query.setFirstResult(offsetreal);
			}
			if (maxRange != -1) {
				query.setMaxResults(maxRange);
			}
			
			viewData = query.list();
			logger.info("data fetched successfully from " + viewName + ", data size: " + viewData.size());
			
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		return viewData;
	}
	
	/**
	 * <p>
	 * {@link #getViewDataSize(SearchBuilder, String, String)} fetch entity by
	 * {@link #sessionFactory}. This is a common method for all View, so it works for any View.This
	 * method supports search option.
	 * </p>
	 * <br/>
	 * <b> How to invoke:</b> SearchBuilder searchBuilder; searchBuilder.setDistrict("DHAKA");
	 * search(searchBuilder, User.class).
	 * 
	 * @param entityType is name of ENtity Type Such as "child,member".
	 * @param searchBuilder is object of search option.
	 * @param viewName is name of target view.
	 * @return total count.
	 */
	
	@Override
	public int getViewDataSize(SearchBuilder searchBuilder, String viewName, String entityType) {
		Session session = getCurrentSession();
		int count = 0;
		try {
			String hql = "SELECT * FROM core.\"" + viewName + "\"" + " where entity_type = '" + entityType + "'";
			
			hql = setViewCondition(searchBuilder, hql);
			Query query = session.createSQLQuery(hql);
			count = query.list().size();
		}
		catch (Exception e) {
			logger.error("Data fetch from " + viewName + " error:" + e.getMessage());
		}
		finally {
			session.close();
		}
		
		return count;
	}
	
	private String setViewCondition(SearchBuilder searchBuilder, String hql) {
		if (searchBuilder.getDivision() != null && !searchBuilder.getDivision().isEmpty()) {
			hql = hql + " and division = '" + searchBuilder.getDivision() + "'";
		}
		if (searchBuilder.getDistrict() != null && !searchBuilder.getDistrict().isEmpty()) {
			hql = hql + " and district = '" + searchBuilder.getDistrict() + "'";
		}
		if (searchBuilder.getUpazila() != null && !searchBuilder.getUpazila().isEmpty()) {
			hql = hql + " and upazila = '" + searchBuilder.getUpazila() + "'";
		}
		/*if(searchBuilder.getUnion() != null && !searchBuilder.getUnion().isEmpty()) {
			hql = hql + " and union = '" + searchBuilder.getUnion() + "'";
		}*/
		if (searchBuilder.getWard() != null && !searchBuilder.getWard().isEmpty()) {
			hql = hql + " and ward = '" + searchBuilder.getWard() + "'";
		}
		if (searchBuilder.getSubunit() != null && !searchBuilder.getSubunit().isEmpty()) {
			hql = hql + " and subunit = '" + searchBuilder.getSubunit() + "'";
		}
		if (searchBuilder.getMauzapara() != null && !searchBuilder.getMauzapara().isEmpty()) {
			hql = hql + " and mauzapara = '" + searchBuilder.getMauzapara() + "'";
		}
		if (searchBuilder.getServerVersion() != -1) {
			hql = hql + " and server_version > '" + searchBuilder.getServerVersion() + "'";
		}
		if (searchBuilder.getPregStatus() != null && !searchBuilder.getPregStatus().isEmpty()) {
			hql = hql + " and is_pregnant = '" + searchBuilder.getPregStatus() + "'";
		}
		if (searchBuilder.getName() != null && !searchBuilder.getName().isEmpty()) {
			hql = hql + " and first_name ilike '%" + searchBuilder.getName() + "%'";
		}
		
		logger.info(hql);
		return hql;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> getDataFromSQLFunction(Query query, Session session) {
		
		List<T> aggregatedList = null;
		try {
			aggregatedList = query.list();
			logger.info("Report Data fetched successfully from , aggregatedList size: " + aggregatedList.size());
			
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		return aggregatedList;
	}

	@Override
	public <T> List<T> getDataByMHV(String username) {
		Session session = getCurrentSession();
		List<T> viewData = null;

		try {
			String hql = "select vc1.health_id as household_id, concat(vc1.first_name, ' ', vc1.lastName) as full_name," +
					" count(case when (vc2.gender = 'M' or vc2.gender = 'F') and vc1.provider_id = vc2.provider_id then 1 end) as population_count," +
					" count(case when vc2.gender = 'M' and vc1.provider_id = vc2.provider_id then 1 end) as male_count," +
					" count(case when vc2.gender = 'F' and vc1.provider_id = vc2.provider_id then 1 end) as female_count, vc1.base_entity_id" +
					" from core.\"viewJsonDataConversionOfClient\" vc1" +
					" left join core.\"viewJsonDataConversionOfClient\" vc2 on vc1.base_entity_id = vc2.relationships_id" +
					" where vc1.provider_id = '"+ username +"' and vc1.entity_type = 'ec_household' " +
					"group by vc1.first_name, vc1.lastName, vc1.health_id, vc1.base_entity_id;";
			Query query = session.createSQLQuery(hql);
			viewData = query.list();
			logger.info("data fetched successfully from " + "viewJsonDataConversionOfClient" + ", data size: " + viewData.size());
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		return viewData;
	}

	@Override
	public <T> List<T> getMemberListByHousehold(String householdBaseId, String mhvId) {
		Session session = getCurrentSession();
		List<T> aggregatedList = null;

		try {
			String hql = "select concat(first_name, ' ', lastName) as name, case when gender = 'M' then 'Male' else 'Female' end as gender," +
					" concat(extract(year from age(now(), birth_date)), ' year(s) ', extract(month from age(now(), birth_date)), ' month(s)') as age, health_id" +
					" from core.\"viewJsonDataConversionOfClient\" where relationships_id = '"+householdBaseId+"' and entity_type != 'ec_household' and provider_id = '"+mhvId+"';";
			Query query = session.createSQLQuery(hql);
			aggregatedList = query.list();
			logger.info("data fetched successfully from viewJsonDataConversionOfClient, data size: "+ aggregatedList.size());
		}
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			session.close();
		}
		return aggregatedList;
	}

	@Override
	public <T> T getMemberByHealthId(String healthId) {
		Session session = getCurrentSession();
		T member = null;
		try {
			String hql = "select * from core.\"viewJsonDataConversionOfClient\" where health_id = '"+ healthId +"';";
			Query query = session.createSQLQuery(hql);
			List<T> members = query.list();
			if (members.size() > 0) {
				member = members.get(0);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return member;
	}

	@Override
	public <T> T getMemberByBaseEntityId(String baseEntityId) {
		Session session = getCurrentSession();
		T member = null;
		try {
			String hql = "select * from core.\"viewJsonDataConversionOfClient\" where base_entity_id = '"+ baseEntityId +"';";
			Query query = session.createSQLQuery(hql);
			List<T> members = query.list();
			if (members.size() > 0) {
				member = members.get(0);
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return member;
	}

	@Override
	public <T> List<T> getMemberListByCC(String ccName) {
		Session session = getCurrentSession();
		List<T> memberList = null;
		try {
			String hql = "select concat(vc.first_name, ' ', vc.lastName) as name, case when vc.gender = 'M' then 'Male' else 'Female' end as gender," +
					" concat(extract(year from age(now(), vc.birth_date)), ' year(s) ', extract(month from age(now(), vc.birth_date)), ' month(s)') as age," +
					" vc.health_id, vc.base_entity_id, r.status from core.\"viewJsonDataConversionOfClient\" vc left join" +
					" core.reviews r on vc.base_entity_id = r.base_entity_id where vc.cc_name = '"
					+ ccName +"' and vc.entity_type != 'ec_household';";
			Query query = session.createSQLQuery(hql);
			memberList = query.list();
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return memberList;
	}

	@Override
	public <T> List<T> getUpazilaList() {
		Session session = getCurrentSession();
		List<T> upazilaList = null;
		try {
			String hql = "select distinct(upazila), count(case when entity_type = 'ec_household' then 1 end) as household_count," +
					" count(case when entity_type != 'ec_household' then 1 end) as population_count from core.\"viewJsonDataConversionOfClient\" group by upazila;\n";
			Query query = session.createSQLQuery(hql);
			upazilaList = query.list();
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return upazilaList;
	}

	@Override
	public <T> List<T> getCCListByUpazila(SearchBuilder searchBuilder) {
		Session session = getCurrentSession();
		List<T> ccList = null;
		try {
			String hql = "select distinct(cc_name), provider_id, count(case when entity_type = 'ec_household' then 1 end) as household_count," +
					" count(case when gender = 'M' or gender = 'F' then 1 end) as population_count, count(case when gender='F' then 1 end) as female," +
					" count(case when gender = 'M' then 1 end) as male from core.\"viewJsonDataConversionOfClient\" where upazila = '"
					+ searchBuilder.getUpazila() +"' and cc_name != '' group by cc_name, provider_id order by cc_name, provider_id;";
			Query query = session.createSQLQuery(hql);
			ccList = query.list();
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return ccList;
	}

	@Override
	public List<Report> getMHVListFilterWise(String filterString) {
		Session session = getCurrentSession();
		List<Report> mhvList = null;
		try {
			String hql = "select distinct(provider_id) as mhv, count(case when entity_type = 'ec_household' then 1 end) as household," +
					" count(case when entity_type != 'ec_household' then 1 end) as population, count(case when gender='F' then 1 end) as female," +
					" count(case when gender = 'M' then 1 end) as male from core.\"viewJsonDataConversionOfClient\" "+
					filterString +" group by provider_id order by provider_id;";
			Query query = session.createSQLQuery(hql)
					.addScalar("mhv", StandardBasicTypes.STRING)
					.addScalar("household", StandardBasicTypes.INTEGER)
					.addScalar("population", StandardBasicTypes.INTEGER)
					.addScalar("female", StandardBasicTypes.INTEGER)
					.addScalar("male", StandardBasicTypes.INTEGER)
					.setResultTransformer(Transformers.aliasToBean(Report.class));
			mhvList = query.list();
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return mhvList;
	}
	
	@Override
	public List<Object[]> getHouseHoldReports(String startDate, String endDate, String filterString, String searched_value, List<Object[]> allSKs) {
		String[] values = searched_value.split(":");
		searched_value = values[0]+(values.length > 1?"'":"");
		Session session = getCurrentSession();
		String conditionString = " where cast(date_created as text) between :startDate and :endDate";
		
		if(!"empty".equalsIgnoreCase(searched_value)) {
			conditionString += " and "+searched_value;
		}
		System.out.println("Size:"+ allSKs.size());
		System.out.println(filterString + " " + searched_value);
		if (allSKs.size() != 0) {
			String providerIds = "";
			int size = allSKs.size();
			for (int i = 0; i < size; i++) {
				providerIds += "'" + allSKs.get(i)[1].toString() + "'";
				if (i != size - 1)
					providerIds += ",";
			}
			if (filterString.equalsIgnoreCase("sk_id") && searched_value.equalsIgnoreCase("empty")) {
				conditionString = conditionString + " and sk_id in (" + providerIds + ")";
			}
		}
		
		System.out.println("conditionstring"+ conditionString);

		List<Object[]> mhvList = null;
		try {
			String hql = "with report as ("
					+ "select *, (select first_name from core.users where username = "+filterString+") from ("
					+ "select " + "distinct("+filterString+"),"
					+ "sum(case when entity_type = 'ec_family' then 1 else 0 end) as house_hold_count,"
					+ "sum(case when house_hold_type = 'NVO' then 1 else 0 end) as nvo,"
					+ "sum(case when house_hold_type = 'BRAC VO' then 1 else 0 end) as vo,"
					+ "(sum(case when house_hold_type = 'NVO' or house_hold_type = 'BRAC VO' then 1 else 0 end) ) as total_household,"
					+ "sum(case when gender = 'M' or gender = 'F' then 1 else 0 end) as population,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 6 then 1 else 0 end) as zero_to_six_months,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 6 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 12 then 1 else 0 end) as seven_to_twelve_months,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 12 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 18 then 1 else 0 end) as thirteen_to_eighteen,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 18 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 24 then 1 else 0 end) as nineteen_to_twenty_four,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 24 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 36 then 1 else 0 end) as twenty_five_to_thirty_six,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 36 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 60 then 1 else 0 end) as thirty_seven_to_sixty,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 60 then 1 else 0 end) as children_under_five,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 60 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 120 then 1 else 0 end) as children_five_to_ten,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 120 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 228 and gender = 'M' then 1 else 0 end) as ten_to_nineteen_year_male,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 120 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 228 and gender = 'F' then 1 else 0 end) as ten_to_nineteen_year_female,"
					+ "(sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 120 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 228 and gender = 'M' then 1 else 0 end) + sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 120 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 228 and gender = 'F' then 1 else 0 end) ) as total_mf_ten_to_nineteen,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 228 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 420 and gender = 'M' then 1 else 0 end) as nineteen_to_thirty_five_male,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 228 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 420 and gender = 'F' then 1 else 0 end) as nineteen_to_thirty_five_female,"
					+ "(sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 228 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 420 and gender = 'M' then 1 else 0 end) + sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 228 and ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) < 420 and gender = 'F' then 1 else 0 end) ) as total_mf_aged_nineteen_tO_thirty_five,"
					+ "sum(case when ((extract( year from now() ) - extract( year from birth_date)) * 12) + extract(month from now() ) - extract(month from birth_date) >= 420 and entity_type = 'ec_family_member' then 1 else 0 end) as population_thirty_five_and_above,"
					+ "sum(case when hh_has_latrine = 'Yes' then 1 else 0 end) as count_has_sanitary_latrine,"
					+ "sum(finger_print_taken) as total_finger_print_taken,"
					+ "sum(finger_print_availability) as total_finger_print_availability"
					+ " from core.\"clientInfoFromJSON\" "
					+ conditionString
					+ " group by "+filterString+") tmp"
					+ ") select * from report" + " union all " + "select " + "'TOTAL',"
					+ "sum(house_hold_count)," + "sum(nvo)," + "sum(vo)," + "sum(total_household),"
					+ "sum(population)," + "sum(zero_to_six_months)," + "sum(seven_to_twelve_months),"
					+ "sum(thirteen_to_eighteen)," + "sum(nineteen_to_twenty_four),"
					+ "sum(twenty_five_to_thirty_six)," + "sum(thirty_seven_to_sixty),"
					+ "sum(children_under_five)," + "sum(children_five_to_ten),"
					+ "sum(ten_to_nineteen_year_male)," + "sum(ten_to_nineteen_year_female),"
					+ "sum(total_mf_ten_to_nineteen)," + "sum(nineteen_to_thirty_five_male),"
					+ "sum(nineteen_to_thirty_five_female)," + "sum(total_mf_aged_nineteen_tO_thirty_five),"
					+ "sum(population_thirty_five_and_above)," + "sum(count_has_sanitary_latrine),"
					+ "sum(total_finger_print_taken)," + "sum(total_finger_print_availability),"
					+ "null" + " from report;";
			
			Query query = session.createSQLQuery(hql)
					.setString("startDate", startDate)
					.setString("endDate", endDate);
			System.out.println("Query"+ hql);
			mhvList = query.list();
		} catch (Exception e) {
			logger.error(e);
		} finally {
			session.close();
		}
		return mhvList;
	}

	//TODO
	@Override
	public List<PhysicalLocation> getProviderLocationTreeByChildRole(int memberId, int childRoleId) {
		List<PhysicalLocation> treeDTOS = new ArrayList<PhysicalLocation>();
		Session session = getCurrentSession();
		try {
			String hql = "select * from core.get_location_tree(:memberId, :childRoleId)";
			Query query = session.createSQLQuery(hql)
					.addScalar("id", StandardBasicTypes.INTEGER)
					.addScalar("code", StandardBasicTypes.STRING)
					.addScalar("name", StandardBasicTypes.STRING)
					.addScalar("leaf_loc_id", StandardBasicTypes.INTEGER)
					.addScalar("member_id", StandardBasicTypes.INTEGER)
					.addScalar("username", StandardBasicTypes.STRING)
					.addScalar("first_name", StandardBasicTypes.STRING)
					.addScalar("last_name", StandardBasicTypes.STRING)
					.addScalar("loc_tag_name", StandardBasicTypes.STRING)
					.setResultTransformer(Transformers.aliasToBean(PhysicalLocation.class))
					.setInteger("memberId", memberId)
					.setInteger("childRoleId", childRoleId);
			treeDTOS = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return treeDTOS;
	}

	@Override
	public List<Object[]> getAllSK(List<Object[]> branches) {
		Session session = getCurrentSession();
		List<Object[]> allSK = null;
		String additionalQuery = "";
		if (branches != null) {
			additionalQuery = " and ub.branch_id in (";
			int size = branches.size();
			for (int i = 0; i < size; i++) {
				additionalQuery += branches.get(i)[0].toString();
				if (i != size-1) additionalQuery += ",";
			}
			additionalQuery += ");";
		}
		try {
			String hql = "select u.id, u.username, concat(u.first_name, ' ', u.last_name) from core.users u"
					+ " join core.user_role ur on u.id = ur.user_id join core.user_branch ub on u.id = ub.user_id"
					+ " where ur.role_id = :skId" + additionalQuery;
			allSK = session.createSQLQuery(hql).setInteger("skId", SK_ID).list();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return allSK;
	}

	@Override
	public List<Object[]> getSKByBranch(Integer branchId) {
		Session session = getCurrentSession();
		List<Object[]> skList = null;
		try {
			String hql = "select u.id, u.username, concat(u.first_name, ' ', u.last_name) from core.users u join core.user_role ur on u.id = ur.user_id"
					+ " join core.user_branch ub on u.id = ub.user_id where ur.role_id = :skId and ub.branch_id = :branchId";
			skList = session.createSQLQuery(hql)
					.setInteger("skId", SK_ID)
					.setInteger("branchId", branchId)
					.list();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return skList;
	}

	@Override
	public <T> List<T> getCatchmentArea(int userId) {
		Session session = getCurrentSession();
		List<T> catchmentAreas = null;
		try {
			String hql = "select * from core.get_user_catchment(:userId)";
			catchmentAreas = session.createSQLQuery(hql).setInteger("userId", userId).list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		return catchmentAreas;
	}

	@Override
	public <T> List<T> getVillageIdByProvider(int memberId, int childRoleId, int locationTagId) {
		Session session = getCurrentSession();
		List<T> results = null;
		try {
			String hql = "select * from core.get_location_tree_id(:memberId, :childRoleId, :locationTagId);";
			Query query = session.createSQLQuery(hql)
					.setInteger("memberId", memberId)
					.setInteger("childRoleId", childRoleId)
					.setInteger("locationTagId", locationTagId);
			results = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return results;
	}

	@Override
	public <T> T countByField(int id, String fieldName, String className) {
		Session session = getCurrentSession();
		List<T> result = null;
		try {
			String hql = "select count(*) from core."+className+ " where " + fieldName + " = :id";
			Query query = session.createSQLQuery(hql).setInteger("id", id);
			result = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return result.get(0);
	}

	public <T> List<T> getReportData(SearchBuilder searchBuilder, String procedureName) {
		Session session = getCurrentSession();
		List<T> aggregatedList = null;
		try {
			String hql = "select * from core." + procedureName + "(array[:division,:district,:upazila"
					+ ",:union,:ward,:cc_name,:subunit,:mauzapara,:provider,:start_date,:end_date,:pregnancy_status,:age_from,:age_to])";
			Query query = session.createSQLQuery(hql);
			setParameter(searchBuilder, query);
			aggregatedList = query.list();

			logger.info("Report Data fetched successfully from " + procedureName
					+", aggregatedList size: " + aggregatedList.size());
		}
		catch (Exception e) {
			logger.error("Data fetch from " + procedureName + " error:" + e.getMessage());
		}
		finally {
			session.close();
		}
		return aggregatedList;
	}

	private void setParameter(SearchBuilder searchFilterBuilder, Query query) {
		if (searchFilterBuilder.getDivision() != null && !searchFilterBuilder.getDivision().isEmpty()) {
			query.setParameter("division", searchFilterBuilder.getDivision());
		} else {
			query.setParameter("division", "");
		}

		if (searchFilterBuilder.getDistrict() != null && !searchFilterBuilder.getDistrict().isEmpty()) {
			query.setParameter("district", searchFilterBuilder.getDistrict());
		} else {
			query.setParameter("district", "");
		}

		if (searchFilterBuilder.getUnion() != null && !searchFilterBuilder.getUnion().isEmpty()) {
			query.setParameter("union", searchFilterBuilder.getUnion());
		} else {
			query.setParameter("union", "");
		}

		if (searchFilterBuilder.getUpazila() != null && !searchFilterBuilder.getUpazila().isEmpty()) {
			query.setParameter("upazila", searchFilterBuilder.getUpazila());
		} else {
			query.setParameter("upazila", "");
		}

		if (searchFilterBuilder.getWard() != null && !searchFilterBuilder.getWard().isEmpty()) {
			query.setParameter("ward", searchFilterBuilder.getWard());
		} else {
			query.setParameter("ward", "");
		}

        if (searchFilterBuilder.getCommunityClinic() != null && !searchFilterBuilder.getCommunityClinic().isEmpty()) {
            query.setParameter("cc_name", searchFilterBuilder.getCommunityClinic());
        } else {
            query.setParameter("cc_name", "");
        }

		if (searchFilterBuilder.getMauzapara() != null && !searchFilterBuilder.getMauzapara().isEmpty()) {
			query.setParameter("mauzapara", searchFilterBuilder.getMauzapara());
		} else {
			query.setParameter("mauzapara", "");
		}

		if (searchFilterBuilder.getSubunit() != null && !searchFilterBuilder.getSubunit().isEmpty()) {
			query.setParameter("subunit", searchFilterBuilder.getSubunit());
		} else {
			query.setParameter("subunit", "");
		}

		if (searchFilterBuilder.getPregStatus() != null && !searchFilterBuilder.getPregStatus().isEmpty()) {
			query.setParameter("pregnancy_status", searchFilterBuilder.getPregStatus());
		} else {
			query.setParameter("pregnancy_status", "");
		}

		if (searchFilterBuilder.getProvider() != null && !searchFilterBuilder.getProvider().isEmpty()) {
			query.setParameter("provider", searchFilterBuilder.getProvider());
		} else {
			query.setParameter("provider", "");
		}

		if (searchFilterBuilder.getStart() != null && !searchFilterBuilder.getStart().isEmpty()
				&& searchFilterBuilder.getEnd() != null && !searchFilterBuilder.getEnd().isEmpty()) {
			query.setParameter("start_date", searchFilterBuilder.getStart());
			query.setParameter("end_date", searchFilterBuilder.getEnd());
		} else {
			query.setParameter("start_date", "");
			query.setParameter("end_date", "");
		}

		if (searchFilterBuilder.getAgeFrom() != null && !searchFilterBuilder.getAgeFrom().isEmpty()
				&& searchFilterBuilder.getAgeTo() != null && !searchFilterBuilder.getAgeTo().isEmpty()) {
			query.setParameter("age_from", searchFilterBuilder.getAgeFrom());
			query.setParameter("age_to", searchFilterBuilder.getAgeTo());
		} else {
			query.setParameter("age_from", "");
			query.setParameter("age_to", "");
		}
	}

	public List<Object[]> getClientInformation(){
		Session session = getCurrentSession();
		List<Object[]> clientInfoList = new ArrayList<Object[]>();
		try {

			String hql = "SELECT Distinct On(c.json ->> 'baseEntityId')\n" +
					"		c.json ->> 'gender' gender, \n" +
					"       c.json->'addresses' -> 0 ->>'country' country, \n" +
					"       c.json->'addresses' -> 0 ->>'stateProvince' division, \n"+
					"       c.json->'addresses' -> 0 ->>'countyDistrict' district, \n"+
					"       c.json->'addresses' -> 0 ->>'cityVillage' village, \n"+
					"       cast(c.json ->> 'birthdate' as date) birthdate, \n" +
					"       c.json ->> 'firstName' first_name, \n" +
//					" 		c.json -> 'attributes' ->> 'Cluster' cluster, \n"+
					" 		c.json -> 'attributes' ->> 'HH_Type' household_type, \n"+
					"       c.json -> 'attributes' ->> 'HOH_Phone_Number' phone_number, \n" +
					"       c.json -> 'attributes' ->> 'householdCode' household_code, \n" +
					"       e.provider_id provider_id, \n" +
					"       cast(e.date_created as date) date_created, \n" +
					"       c.json -> 'attributes' ->> 'SS_Name' ss_name, \n"+
					"       c.json -> 'attributes' ->> 'HH_Type' household_type, \n"+
					"       c.json -> 'attributes' ->> 'Has_Latrine' has_latrine, \n"+
					"       c.json -> 'attributes' ->> 'Number_of_HH_Member' total_member, \n"+
					"       c.json -> 'attributes' ->> 'motherNameEnglish' mother_name, \n"+
					"       c.json -> 'attributes' ->> 'Relation_with_HOH' relation_household, \n"+
					"       c.json -> 'attributes' ->> 'Blood_Group' blood_group, \n"+
					"       c.json -> 'attributes' ->> 'Marital_Status' marital_status, \n"+
					"       c.json->'addresses' -> 0 -> 'addressFields' ->> 'address2' upazila, \n"+
					"       c.json->'addresses' -> 0 -> 'addressFields' ->> 'address1' city_union, \n"+
					"       c.json -> 'attributes' ->> 'nationalId' national_id \n"+
					"FROM   core.client c \n" +
					"       JOIN core.event_metadata e \n" +
					"         ON c.json ->> 'baseEntityId' = e.base_entity_id";

			clientInfoList = session.createSQLQuery(hql).list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return clientInfoList;
	}

	@Override
	public List<Object[]> getClientInfoFilter(String startTime, String endTime, String formName, String sk, List<Object[]> allSKs, Integer pageNumber) {
		String wh = "";
		List<String> conds = new ArrayList<String>();
		String stCond,edCond,formCond,skCond;



		stCond = "Date(date_created) BETWEEN \'" + startTime+"\' AND \'"+endTime+"\'";
		conds.add(stCond);



		formCond = "  event_type =\'" + formName +"\'";
		conds.add(formCond);

		if(!sk.isEmpty()){
			skCond = " provider_id =\'" + sk+"\'";
			conds.add(skCond);
		} else {
			String providerIds = "";
			int size = allSKs.size();
			for (int i = 0; i < size; i++) {
				providerIds += "'"+allSKs.get(i)[1].toString()+"'";
				if (i != size-1) providerIds += ",";
			}
			skCond = " provider_id in ("+providerIds+")";
			conds.add(skCond);
		}
		if(conds.size() == 0) wh = "";
		else {
			wh = " WHERE ";
			wh += conds.get(0);
			for(int i = 1; i < conds.size();i++){
				wh += " AND ";
				wh += conds.get(i);
			}
		}

		Session session = getCurrentSession();
		List<Object[]> clientInfoList = new ArrayList<Object[]>();
		try {

			String hql = "SELECT * FROM core.\"viewJsonDataConversionOfClient\"";
					hql += wh;
					hql += "order by date_created desc limit 10 offset 10 * "+ pageNumber;
					hql += ";";
			clientInfoList = session.createSQLQuery(hql).list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return clientInfoList;
	}


	@Override
	public Integer getClientInfoFilterCount(String startTime, String endTime, String formName, String sk, List<Object[]> allSKs) {
		String wh = "";
		List<String> conds = new ArrayList<String>();
		String stCond,edCond,formCond,skCond;



		stCond = "date_created BETWEEN \'" + startTime+"\' AND \'"+endTime+"\'";
		conds.add(stCond);


		if(formName.contains("-1") == false){
			formCond = "  event_type =\'" + formName +"\'";

			conds.add(formCond);
		}
		if(!sk.isEmpty()){
			skCond = " provider_id =\'" + sk+"\'";
			conds.add(skCond);
		} else {
			String providerIds = "";
			int size = allSKs.size();
			for (int i = 0; i < size; i++) {
				providerIds += "'"+allSKs.get(i)[1].toString()+"'";
				if (i != size-1) providerIds += ",";
			}
			skCond = " provider_id in ("+providerIds+")";
			conds.add(skCond);
		}
		if(conds.size() == 0) wh = "";
		else {
			wh = " WHERE ";
			wh += conds.get(0);
			for(int i = 1; i < conds.size();i++){
				wh += " AND ";
				wh += conds.get(i);
			}
		}

		Session session = getCurrentSession();
		Integer clientInfoCount = 0;
		try {

			String hql = "SELECT COUNT(*) FROM core.\"viewJsonDataConversionOfClient\"";
			hql += wh;
			hql += ";";
			clientInfoCount = ((BigInteger)session.createSQLQuery(hql).uniqueResult()).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return clientInfoCount;
	}

	@Override
	public List<Object[]> getExportByCreator(String username) {
		Session session = getCurrentSession();
		List<Object[]> exportData = new ArrayList<Object[]>();
		try	{
			exportData = session.createSQLQuery("select file_name, status from export where creator = :username order by id desc limit 1")
					.setParameter("username", username).list();


		}  catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return exportData;
	}


	@Override
	public List<Object[]> getUserListByFilterString(int locationId, int locationTagId, int roleId, int branchId) {
		Session session = getCurrentSession();
		List<Object[]> userList = null;
		String where = " where ";
		if (branchId > 0) where += "b.id = "+branchId;
		if (roleId > 0) {
			if (branchId > 0) where += " and r.id = " + roleId;
			else where += "r.id = " + roleId;
		}
		where += ";";
		try {
			String sql = "WITH recursive main_location_tree AS \n" + "( \n" + "       SELECT * \n"
					+ "       FROM   core.location \n" + "       WHERE  id IN ( WITH recursive location_tree AS \n"
					+ "                     ( \n" + "                            SELECT * \n"
					+ "                            FROM   core.location l \n"
					+ "                            WHERE  l.id = :locationId \n" + "                            UNION ALL \n"
					+ "                            SELECT loc.* \n"
					+ "                            FROM   core.location loc \n"
					+ "                            JOIN   location_tree lt \n"
					+ "                            ON     lt.id = loc.parent_location_id ) \n"
					+ "              SELECT DISTINCT(lt.id) \n" + "              FROM            location_tree lt \n"
					+ "              WHERE           lt.location_tag_id = :locationTagId ) \n" + "UNION ALL \n" + "SELECT l.* \n"
					+ "FROM   core.location l \n" + "JOIN   main_location_tree mlt \n"
					+ "ON     l.id = mlt.parent_location_id ) \n" + "SELECT DISTINCT(u.username),\n"
					+ "\t\t\t\tconcat(u.first_name, ' ', u.last_name) as full_name,\n" + "\t\t\t\tu.mobile,\n"
					+ "                r.NAME role_name, \n" + "                b.NAME branch_name, u.id \n"
					+ "FROM            main_location_tree mlt \n" + "JOIN            core.users_catchment_area uca \n"
					+ "ON              uca.location_id = mlt.id \n" + "JOIN            core.users u \n"
					+ "ON              u.id = uca.user_id \n" + "JOIN            core.user_branch ub \n"
					+ "ON              ub.user_id = u.id \n" + "JOIN            core.branch b \n"
					+ "ON              b.id = ub.branch_id \n" + "JOIN            core.user_role ur \n"
					+ "ON              u.id = ur.user_id \n" + "JOIN            core.role r \n"
					+ "ON              ur.role_id = r.id";

			Query query = session.createSQLQuery((branchId > 0 || roleId > 0)?sql+where:sql+";");
			userList = query
					.setInteger("locationId", locationId)
					.setInteger("locationTagId", locationTagId)
					.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return userList;
	}

	@Override
	public List<Object[]> getUserListWithoutCatchmentArea(int roleId, int branchId) {
		List<Object[]> users = new ArrayList<Object[]>();
		Session session = getCurrentSession();
		try {
			String hql = "select \n" + "\tdistinct(u.username),\n" + "\tconcat(u.first_name, ' ', u.last_name) full_name,\n"
					+ "\tu.mobile,\n" + "\tr.name role_name,\n" + "\tb.name branch_name,\n"
					+ "\tu.id from core.users as u \n" + "\t\tjoin core.user_role ur on ur.user_id = u.id \n"
					+ "\t\tjoin core.user_branch ub on ub.user_id = u.id\n"
					+ "\t\tleft join core.team_member tm on tm.person_id = u.id\n"
					+ "\t\tjoin core.role r on r.id = ur.role_id\n" + "\t\tjoin core.branch b on b.id = ub.branch_id\n"
					+ "\twhere tm.id is null";
			if (branchId > 0) hql += " and ub.branch_id = "+branchId;
			if (roleId > 0) hql += " and ur.role_id = "+roleId;
			Query query = session.createSQLQuery(hql);
			users = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return users;
	}

	//TODO
	public <T> List<T> getUniqueLocation(String village, String ward) {
		List<T> locations = null;
		Session session = getCurrentSession();

		try {
			String hql = "select l1.id as id from core.location l1 join core.location l2 on l1.parent_location_id = l2.id"
					+ " where l1.location_tag_id = :villageId and l2.location_tag_id = :unionId and l1.name like concat(:village,':%')"
					+ " and l2.name like concat(:ward,':%');";
			Query query = session.createSQLQuery(hql)
					.addScalar("id", StandardBasicTypes.INTEGER)
					.setString("village", village)
					.setString("ward", ward)
					.setInteger("villageId", VILLAGE_ID)
					.setInteger("unionId", UNION_ID)
					.setResultTransformer(new AliasToBeanResultTransformer(PhysicalLocation.class));
			locations = query.list();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}

		System.out.println("location size::-> "+ locations.size());

		return locations.size()>0?locations:null;
	}

}
