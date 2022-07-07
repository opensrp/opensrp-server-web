/**
 * 
 */
package org.opensrp.web.acl;

import org.opensrp.domain.Organization;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Samuel Githengi created on 06/17/20
 */
@Component
public class OrganizationPermissionEvaluator extends BasePermissionEvaluator<Organization> {
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
		if (targetId instanceof String) {
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
			        .anyMatch(assignedLocation -> assignedLocation.getOrganizationId().equals(targetId));
			/* @formatter:on */
		} else if (isCollectionOfString(targetId)) {
			Collection<String> identifiers = (Collection<String>) targetId;
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
					.allMatch((assignedLocation) -> {
						return identifiers.contains(assignedLocation.getOrganizationId());
						});
			/* @formatter:on */
		} else if (targetId instanceof Organization) {
			return hasPermission(authentication, (Organization) targetId);
		} else if (isCollectionOfResources(targetId, Organization.class)) {
			Collection<Organization> organizations = (Collection<Organization>) targetId;
			/* @formatter:off */
			Set<String> identifiers=getAssignedLocations(authentication.getName())
					.stream()
					.map(assignedLocation-> assignedLocation.getOrganizationId())
					.collect(Collectors.toSet());
			return organizations
					.stream()
					.allMatch((organization) -> {
						return identifiers.contains(organization.getIdentifier());
					});
			/* @formatter:on */
		}
		return targetId == null;
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, Organization organization) {
		/* @formatter:off */
		return getAssignedLocations(authentication.getName())
				.stream()
				.anyMatch(a->a.getOrganizationId().equals(organization.getIdentifier()));
		/* @formatter:on */
	}
}
