package org.opensrp.web.acl;

import org.smartregister.domain.Client;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class ClientPermissionEvaluator extends BasePermissionEvaluator<Client> {

	public boolean hasPermission(Authentication authentication, Client targetDomainObject) {
		return hasPermissionOnClient(authentication, targetDomainObject.getLocationId());
	}

	private boolean hasPermissionOnClient(Authentication authentication, String identifier) {
		/* @formatter:off */
		return getAssignedLocations(authentication.getName())
				.stream()
				.anyMatch(a -> a.getPlanId().equals(identifier));
		/* @formatter:on */
	}

	public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
		if (targetId instanceof Client) {
			Client client = (Client) targetId;
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
					.anyMatch((assignedLocation) -> {
						return assignedLocation.getJurisdictionId().equals(client.getLocationId()) ||
								assignedLocation.getOrganizationId().equals(client.getTeamId());
					});
			/* @formatter:on */
		} else if (isCollectionOfResources(targetId, Client.class)) {
			Collection<Client> clients = (Collection<Client>) targetId;
			Set<String> organizationIdentifiers = new HashSet<>();
			Set<String> jurisdictionIdentifiers = new HashSet<>();
			/* @formatter:off */
			getAssignedLocations(authentication.getName())
					.stream()
					.forEach((assignedLocation) -> {
						organizationIdentifiers.add(assignedLocation.getOrganizationId());
						jurisdictionIdentifiers.add(assignedLocation.getJurisdictionId());
					});
			return clients
					.stream()
					.allMatch((client) -> {
						return jurisdictionIdentifiers.contains(client.getLocationId()) ||
								organizationIdentifiers.contains(client.getTeamId());
					});
			/* @formatter:on */
		}
		return false;
	}

}
