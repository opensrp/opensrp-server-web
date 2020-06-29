/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensrp.domain.AssignedLocations;
import org.opensrp.domain.postgres.Jurisdiction;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PractitionerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

/**
 * @author Samuel Githengi created on 06/05/20
 */

public abstract class BasePermissionEvaluator<T> implements PermissionContract<T> {
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private PractitionerService practitionerService;
	
	protected List<AssignedLocations> getAssignedLocations(String username) {
		List<Long> organizationIds = practitionerService.getOrganizationsByUserId(username).right;
		if (isEmptyOrNull(organizationIds)) {
			return new ArrayList<>();
		}
		return organizationService.findAssignedLocationsAndPlans(organizationIds);
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
	
	protected boolean isEmptyOrNull(Collection<? extends Object> collection) {
		return collection == null || collection.isEmpty();
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
	
}
