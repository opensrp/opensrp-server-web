/**
 * 
 */
package org.opensrp.web.acl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensrp.domain.PlanDefinition;
import org.opensrp.domain.postgres.Jurisdiction;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/04/20
 */
@Component
public class PlanPermissionEvaluator extends BasePermissionEvaluator {
	
	/**
	 * Evaluates if a user with authentication has permission on targetDomainObject
	 * 
	 * @param authentication the authentication object of user
	 * @param planDefinition the plan to evaluate access for
	 * @param permission the permission to evaluate
	 * @return true/false if user has the permission for the plan
	 */
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		PlanDefinition plan = (PlanDefinition) targetDomainObject;
		return hasPermissiononPlan(authentication, plan.getIdentifier())
		                || hasPermissionOnJurisdictions(authentication, plan.getJurisdiction());
	}
	
	private boolean hasPermissiononPlan(Authentication authentication, String identifier) {
		return getAssignedLocations(authentication.getName()).stream().anyMatch(a -> a.getPlanId().equals(identifier));
	}
	
	private boolean hasPermissionOnJurisdictions(Authentication authentication, List<Jurisdiction> jurisdictions) {
		Set<String> jurisdictionIdentifiers = jurisdictions.stream().map(j -> j.getCode()).collect(Collectors.toSet());
		return getAssignedLocations(authentication.getName()).stream().anyMatch((a) -> {
			return jurisdictionIdentifiers.contains(a.getJurisdictionId());
		});
	}
	
}
