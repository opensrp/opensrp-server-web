/**
 * 
 */
package org.opensrp.web.acl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opensrp.domain.PlanDefinition;
import org.opensrp.domain.postgres.Jurisdiction;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PractitionerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/04/20
 */
@Component
public class PlanPermissionEvaluator {
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private PractitionerService practitionerService;
	
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
		return hasPermissiononPlan(authentication.getName(), plan.getIdentifier(), permission.toString())
		        || hasPermissionOnJurisdictions(authentication.getName(), plan.getJurisdiction(), permission.toString());
	}
	
	private boolean hasPermissiononPlan(String username, String identifier, String permission) {
		List<Long> organizationIds = practitionerService.getOrganizationIdsByUserId(username);
		if (isEmptyOrNull(organizationIds)) {
			return false;
		}
		return organizationService.findAssignedLocationsAndPlans(organizationIds).stream()
		        .anyMatch(a -> a.getPlanId().equals(identifier));
	}
	
	private boolean hasPermissionOnJurisdictions(String username, List<Jurisdiction> jurisdictions, String permission) {
		Set<String> identifiers = jurisdictions.stream().map(j -> j.getCode()).collect(Collectors.toSet());
		List<Long> organizationIds = practitionerService.getOrganizationIdsByUserId(username);
		if (isEmptyOrNull(organizationIds)) {
			return false;
		}
		return organizationService.findAssignedLocationsAndPlans(organizationIds).stream().anyMatch((a) -> {
			return identifiers.contains(a.getJurisdictionId());
		});
	}
	
	private boolean isEmptyOrNull(Collection<? extends Object> collection) {
		return collection == null || collection.isEmpty();
	}
	
}
