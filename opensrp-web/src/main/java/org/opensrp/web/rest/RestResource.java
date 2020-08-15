package org.opensrp.web.rest;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.text.ParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public abstract class RestResource<T> {
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	private T createNew(@RequestBody T entity, HttpServletRequest request) {
		RestUtils.verifyRequiredProperties(requiredProperties(), entity);
		String district = getStringFilter("district", request);
		String division = getStringFilter("division", request);
		String branch = getStringFilter("branch", request);
		String village = getStringFilter("village", request);
		String username = request.getRemoteUser();
		return create(entity, district, division, branch, village, username);
	}
	
	@RequestMapping(value = "/{uniqueId}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	private T updateExisting(@PathVariable("uniqueId") String uniqueId, @RequestBody T entity, HttpServletRequest request) {
		RestUtils.verifyRequiredProperties(requiredProperties(), entity);
		String district = getStringFilter("district", request);
		String division = getStringFilter("division", request);
		String branch = getStringFilter("branch", request);
		String village = getStringFilter("village", request);
		String username = request.getRemoteUser();
		return update(entity, district, division, branch, village, username);//TODO
	}
	
	@RequestMapping(value = "/{uniqueId}", method = RequestMethod.GET)
	@ResponseBody
	private T getById(@PathVariable("uniqueId") String uniqueId, HttpServletRequest request) {
		String district = getStringFilter("district", request);
		String username = request.getRemoteUser();
		return getByUniqueId(uniqueId, district, username);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/search")
	@ResponseBody
	private List<T> searchBy(HttpServletRequest request) throws ParseException {
		
		return search(request);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	private List<T> filterBy(@RequestParam(value = "q", required = true) String query,
	                         @RequestParam("district") String district, @RequestParam("username") String username) {
		
		return filter(query, district, username);
	}
	
	public abstract List<T> filter(String query, String district, String username);
	
	public abstract List<T> search(HttpServletRequest request) throws ParseException;
	
	public abstract T getByUniqueId(String uniqueId, String district, String username);
	
	public abstract List<String> requiredProperties();
	
	public abstract T create(T entity, String district, String division, String branch, String village, String username);
	
	public abstract T update(T entity, String district, String division, String branch, String village, String username);
	
}
