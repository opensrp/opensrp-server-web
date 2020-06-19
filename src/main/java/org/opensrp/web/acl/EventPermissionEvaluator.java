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
				.anyMatch(a -> a.getPlanId().equals(identifier));
		/* @formatter:on */
	}

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
		} else if (targetId instanceof Event) {
			Event event = (Event) targetId;
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
					.anyMatch((assignedLocation) -> {
						return assignedLocation.getPlanId().equals(event.getLocationId());
					});
			/* @formatter:on */
		} else if (isCollectionOfResources(targetId, Event.class)) {
			Collection<Event> events = (Collection<Event>) targetId;
			Set<String> planIdentifiers = new HashSet<>();
			Set<String> jurisdictionIdentifiers = new HashSet<>();
			/* @formatter:off */
			getAssignedLocations(authentication.getName())
					.stream()
					.forEach((assignedLocation) -> {
						planIdentifiers.add(assignedLocation.getPlanId());
						jurisdictionIdentifiers.add(assignedLocation.getJurisdictionId());
					});
			return events
					.stream()
					.allMatch((event) -> {
						return planIdentifiers.contains(event.getLocationId());
					});
			/* @formatter:on */
		}
		return false;
	}

}
