package org.opensrp.repository;

import java.util.List;

public interface CustomBaseRepository<T> {
	
	T get(String id, String table);
	
	void add(T entity, String table, String district, String division, String branch, String village);
	
	void update(T entity, String table, String district, String division, String branch, String village);
	
	List<T> getAll(String table);
	
	void safeRemove(T entity, String table);
	
}
