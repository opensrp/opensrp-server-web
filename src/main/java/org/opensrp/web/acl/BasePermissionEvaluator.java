/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opensrp.domain.AssignedLocations;
import org.opensrp.service.OrganizationService;
import org.opensrp.service.PractitionerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/05/20
 */
@Component
public class BasePermissionEvaluator {
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private PractitionerService practitionerService;
	
	protected List<AssignedLocations> getAssignedLocations(String username) {
		List<Long> organizationIds = practitionerService.getOrganizationIdsByUserId(username);
		if (isEmptyOrNull(organizationIds)) {
			return new ArrayList<>();
		}
		return organizationService.findAssignedLocationsAndPlans(organizationIds);
	}
	
	protected boolean hasPermission(Authentication authentication, String permission) {
		/* @formatter:on */
		return authentication.getAuthorities()
				.stream()
		        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + permission));
		/* @formatter:off */
	}
	
	protected boolean isEmptyOrNull(Collection<? extends Object> collection) {
		return collection == null || collection.isEmpty();
	}
	
	@SuppressWarnings("rawtypes")
	protected boolean isListOfString(Serializable targetId) {
		return targetId instanceof List &&  !((List)targetId).isEmpty() && ((List)targetId).get(0) instanceof String;
	}
	
}
