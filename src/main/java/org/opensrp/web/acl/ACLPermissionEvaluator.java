/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;

import org.opensrp.domain.PlanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/03/20
 */
@Component
public class ACLPermissionEvaluator extends BasePermissionEvaluator implements PermissionEvaluator {
	
	@Autowired
	private PlanPermissionEvaluator planPermissionEvaluator;
	
	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		boolean hasAccess = false;
		if (targetDomainObject == null || permission == null || !hasPermission(authentication, permission.toString())) {
			return hasAccess;
		} else if (targetDomainObject instanceof PlanDefinition) {
			return planPermissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
		}
		return hasAccess;
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
	        Object permission) {
		if (permission == null || !hasPermission(authentication, permission.toString())) {
			return false;
		} else {
			return true;
			
		}
	}
	
}
