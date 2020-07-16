/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opensrp.domain.PlanDefinition;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/04/20
 */
@Component
public class PlanPermissionEvaluator extends BasePermissionEvaluator<PlanDefinition> {
	
	/**
	 * Evaluates if a user with authentication has permission on targetDomainObject
	 * 
	 * @param authentication the authentication object of user
	 * @param planDefinition the plan to evaluate access for
	 * @param permission the permission to evaluate
	 * @return true/false if user has the permission for the plan
	 */
	public boolean hasPermission(Authentication authentication, PlanDefinition targetDomainObject) {
		PlanDefinition plan = (PlanDefinition) targetDomainObject;
		return hasPermissiononPlan(authentication, plan.getIdentifier())
		        || hasPermissionOnJurisdictions(authentication, plan.getJurisdiction());
	}
	
	private boolean hasPermissiononPlan(Authentication authentication, String identifier) {
		/* @formatter:off */
		return getAssignedLocations(authentication.getName())
				.stream()
				.anyMatch(a -> a.getPlanId().equals(identifier));
		/* @formatter:on */
	}
	
	/**
	 * @param authentication
	 * @param targetId
	 * @param permission
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
		if (targetId instanceof String) {
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
			        .anyMatch(assignedLocation -> assignedLocation.getPlanId().equals(targetId));
			/* @formatter:on */
		} else if (isCollectionOfString(targetId)) {
			Collection<String> identifiers = (Collection<String>) targetId;
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
					.allMatch((assignedLocation) -> {
						return identifiers.contains(assignedLocation.getPlanId());
						});
			/* @formatter:on */
		} else if (targetId instanceof PlanDefinition) {  //TODO: PlanDefinition can't be an instance of Serializable
			PlanDefinition plan = (PlanDefinition) targetId;
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
					.anyMatch((assignedLocation) -> {
						return assignedLocation.getPlanId().equals(plan.getIdentifier()) 
								|| plan.getJurisdiction()
								.stream()
								.anyMatch(judisdiction -> judisdiction.getCode().equals(assignedLocation.getJurisdictionId()));
						});
			/* @formatter:on */
		} else if (isCollectionOfResources(targetId, PlanDefinition.class)) {
			Collection<PlanDefinition> plans = (Collection<PlanDefinition>) targetId;
			Set<String> planIdentifiers = new HashSet<>();
			Set<String> jurisdictionIdentifiers = new HashSet<>();
			/* @formatter:off */
			getAssignedLocations(authentication.getName())
			.stream()
			.forEach((assignedLocation) -> {
				planIdentifiers.add(assignedLocation.getPlanId());
				jurisdictionIdentifiers.add(assignedLocation.getJurisdictionId());
			});
			return plans
					.stream()
					.allMatch((plan) -> {
						return planIdentifiers.contains(plan.getIdentifier())
								|| plan.getJurisdiction()
								.stream()
								.anyMatch((judisdiction) -> {
									return jurisdictionIdentifiers.contains(judisdiction.getCode());
									});
						});
			/* @formatter:on */
		}
		return false;
	}
	
}
