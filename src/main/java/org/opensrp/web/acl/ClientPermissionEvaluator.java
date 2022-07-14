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
		return hasPermissionOnClient(authentication, targetDomainObject);
	}

	private boolean hasPermissionOnClient(Authentication authentication, Client client) {
		/* @formatter:off */
		return getAssignedLocations(authentication.getName())
				.stream()
				.anyMatch( (a) -> {
							return a.getJurisdictionId().equals(client.getLocationId()) ||
							a.getOrganizationId().equals(client.getTeamId());
				});
		/* @formatter:on */
	}

	public boolean hasObjectPermission(Authentication authentication, Serializable object, Object permission) {
		if (object instanceof Client) {
			Client client = (Client) object;
			/* @formatter:off */
			return getAssignedLocations(authentication.getName())
					.stream()
					.anyMatch((assignedLocation) -> {
						return assignedLocation.getJurisdictionId().equals(client.getLocationId()) ||
								assignedLocation.getOrganizationId().equals(client.getTeamId());
					});
			/* @formatter:on */
		} else if (isCollectionOfResources(object, Client.class)) {
			Collection<Client> clients = (Collection<Client>) object;
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
		return object == null;
	}
}
