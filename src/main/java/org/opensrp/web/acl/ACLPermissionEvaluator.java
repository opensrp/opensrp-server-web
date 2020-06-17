/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;

import org.opensrp.domain.Organization;
import org.opensrp.domain.PhysicalLocation;
import org.opensrp.domain.PlanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/03/20
 */
@Component
public class ACLPermissionEvaluator implements PermissionEvaluator {
	
	@Autowired
	private PlanPermissionEvaluator planPermissionEvaluator;
	
	@Autowired
	private OrganizationPermissionEvaluator organizationPermissionEvaluator;
	
	@Autowired
	private LocationPermissionEvaluator locationPermissionEvaluator;
	
	@Autowired
	private UserPermissionEvaluator userPermissionEvaluator;
	
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		boolean hasAccess = false;
		if (targetDomainObject == null || permission == null || !hasPermission(authentication, permission.toString())) {
			return hasAccess;
		} else if (targetDomainObject instanceof PlanDefinition) {
			return planPermissionEvaluator.hasPermission(authentication, (PlanDefinition) targetDomainObject);
		} else if (targetDomainObject instanceof Organization) {
			return organizationPermissionEvaluator.hasPermission(authentication, (Organization) targetDomainObject);
		} else if (targetDomainObject instanceof PhysicalLocation) {
			return locationPermissionEvaluator.hasPermission(authentication, (PhysicalLocation) targetDomainObject);
		} else if (targetDomainObject instanceof String) {
			return userPermissionEvaluator.hasPermission(authentication, (String) targetDomainObject);
		}
		return hasAccess;
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
	        Object permission) {
		if (permission == null || !hasPermission(authentication, permission.toString())) {
			return false;
		} else if (PlanDefinition.class.getSimpleName().equals(targetType)) {
			return planPermissionEvaluator.hasObjectPermission(authentication, targetId, permission);
		} else if (Organization.class.getSimpleName().equals(targetType)) {
			return organizationPermissionEvaluator.hasObjectPermission(authentication, targetId, permission);
		} else if (PhysicalLocation.class.getSimpleName().equals(targetType)) {
			return locationPermissionEvaluator.hasObjectPermission(authentication, targetId, permission);
		} else if ("User".equals(targetType)) {
			return userPermissionEvaluator.hasObjectPermission(authentication, targetId, permission);
		}
		return false;
	}
	
	protected boolean hasPermission(Authentication authentication, String permission) {
		/* @formatter:on */
		return authentication.getAuthorities().stream()
		        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + permission));
		/* @formatter:off */
	}
	
}
