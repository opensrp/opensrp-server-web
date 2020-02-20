package org.opensrp.web.dashboard.service;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.opensrp.domain.postgres.Report;
import org.opensrp.web.dashboard.util.SearchBuilder;

public interface DatabaseService {
	
	public <T> long save(T t) throws Exception;
	
	public <T> long update(T t) throws Exception;
	
	public <T> int delete(T t);
	
	public <T> T findById(int id, String fieldName, Class<?> className);
	
	public <T> T findByKey(String value, String fieldName, Class<?> className);
	
	public <T> List<T> findAll(String tableClass);

	public <T> List<T> getHouseholdListByMHV(String username, HttpSession session);

	public <T> List<T> getMemberListByHousehold(String householdBaseId, String mhvId);

	public <T> T getMemberByHealthId(String healthId);

	public <T> T getMemberByBaseEntityId(String baseEntityId);

	public <T> List<T> getMemberListByCC(String ccName);

	public <T> List<T> getUpazilaList();

	public <T> List<T> getCCListByUpazila(SearchBuilder searchBuilder);

	public List<Report> getMHVListFilterWise(SearchBuilder searchBuilder);
	
	public List<Object[]> getHouseHoldReports(String startDate, String endDate, String address_value,String searchedValue,List<Object[]> allSKs);

	public  List<Object[]> getAllSks(List<Object[]> branches);

	public List<Object[]> getSKByBranch(Integer branchId);

	public List<Object[]> getClientInformation();

	public List<Object[]> getClientInfoFilter(String startTime, String endTime, String formName, String sk, List<Object[]> allSKs, Integer pageNumber);

	public Integer getClientInfoFilterCount(String startTime, String endTime, String formName, String sk, List<Object[]> allSKs);

	List<Object[]> getByCreator(String username);

}
