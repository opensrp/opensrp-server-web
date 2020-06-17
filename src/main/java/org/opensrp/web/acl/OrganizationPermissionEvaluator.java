/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;

import org.opensrp.domain.Organization;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/17/20
 */
@Component
public class OrganizationPermissionEvaluator  extends BasePermissionEvaluator<Organization> {

	@Override
	public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
		// TODO Auto-generated method stub
		return false;
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
