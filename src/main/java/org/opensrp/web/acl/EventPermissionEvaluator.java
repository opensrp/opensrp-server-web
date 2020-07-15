package org.opensrp.web.acl;

import org.opensrp.domain.Event;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class EventPermissionEvaluator extends BasePermissionEvaluator<Event> {

	public boolean hasPermission(Authentication authentication, Event targetDomainObject) {
		return hasPermissionOnEvent(authentication, targetDomainObject.getLocationId());
	}

	private boolean hasPermissionOnEvent(Authentication authentication, String identifier) {
		/* @formatter:off */
		return getAssignedLocations(authentication.getName())
				.stream()
				.anyMatch(a -> a.getJurisdictionId().equals(identifier));
		/* @formatter:on */
	}

	public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
		if (targetId instanceof Event) {
			Event event = (Event) targetId;
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
					.anyMatch((assignedLocation) -> {
						return assignedLocation.getJurisdictionId().equals(event.getLocationId()) ||
								assignedLocation.getOrganizationId().equals(event.getTeamId());
					});
			/* @formatter:on */
		} else if (isCollectionOfResources(targetId, Event.class)) {
			Collection<Event> events = (Collection<Event>) targetId;
			Set<String> organizationIdentifiers = new HashSet<>();
			Set<String> jurisdictionIdentifiers = new HashSet<>();
			/* @formatter:off */
			getAssignedLocations(authentication.getName())
					.stream()
					.forEach((assignedLocation) -> {
						organizationIdentifiers.add(assignedLocation.getOrganizationId());
						jurisdictionIdentifiers.add(assignedLocation.getJurisdictionId());
					});
			return events
					.stream()
					.allMatch((event) -> {
						return jurisdictionIdentifiers.contains(event.getLocationId()) ||
								organizationIdentifiers.contains(event.getTeamId());
					});
			/* @formatter:on */
		}
		return false;
	}
}
