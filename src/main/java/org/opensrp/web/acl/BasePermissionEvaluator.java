/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensrp.domain.AssignedLocations;
import org.smartregister.domain.Jurisdiction;
import org.opensrp.service.PhysicalLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;

/**
 * @author Samuel Githengi created on 06/05/20
 */

public abstract class BasePermissionEvaluator<T> implements PermissionContract<T> {

	@Autowired
	private PhysicalLocationService locationService;

	@Cacheable(value = "locationCache", key = "#username")
	protected List<AssignedLocations> getAssignedLocations(String username) {
		return locationService.getAssignedLocations(username);
	}
	
	protected boolean hasPermissionOnJurisdictions(Authentication authentication, List<Jurisdiction> jurisdictions) {
		/* @formatter:off */
		Set<String> jurisdictionIdentifiers = jurisdictions
				.stream()
				.map(j -> j.getCode())
				.collect(Collectors.toSet());
		return hasPermissionOnJurisdictions(authentication, jurisdictionIdentifiers);
		/* @formatter:on */
	}
	
	protected boolean hasPermissionOnJurisdictions(Authentication authentication,
	        Collection<String> jurisdictionIdentifiers) {
		/* @formatter:off */
		return getAssignedLocations(authentication.getName())
				.stream()
				.anyMatch((a) -> {
					return jurisdictionIdentifiers.contains(a.getJurisdictionId());
					});
		/* @formatter:on */
	}
	
	protected boolean hasPermissionOnJurisdiction(Authentication authentication, String jurisdiction) {
		/* @formatter:off */
		
		return getAssignedLocations(authentication.getName())
				.stream()
				.anyMatch((a) -> {
					return jurisdiction.equals(a.getOrganizationId());
					});
		/* @formatter:on */
	}
	
	@SuppressWarnings("rawtypes")
	protected boolean isCollectionOfString(Serializable targetId) {
		return targetId instanceof Collection && !((Collection) targetId).isEmpty()
		        && ((Collection) targetId).iterator().next() instanceof String;
	}
	
	@SuppressWarnings("rawtypes")
	protected boolean isCollectionOfResources(Serializable targetId, Class clazz) {
		return targetId instanceof Collection && !((Collection) targetId).isEmpty()
		        && clazz.isInstance(((Collection) targetId).iterator().next());
	}

	public void setLocationService(PhysicalLocationService locationService) {
		this.locationService = locationService;
	}
}
