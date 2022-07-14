/**
 *
 */
package org.opensrp.web.acl;

import org.smartregister.domain.PhysicalLocation;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Samuel Githengi created on 06/17/20
 */
@Component
public class LocationPermissionEvaluator extends BasePermissionEvaluator<PhysicalLocation> {

	@SuppressWarnings("unchecked")
	@Override
	public boolean hasObjectPermission(Authentication authentication, Serializable object, Object permission) {
		if (object instanceof String) {
			return hasPermissionOnJurisdiction(authentication, (String) object);
		} else if (isCollectionOfString(object)) {
			return hasPermissionOnJurisdictions(authentication, (Collection<String>) object);
		} else if (object instanceof PhysicalLocation) {
			return hasPermission(authentication, (PhysicalLocation) object);
		} else if (isCollectionOfResources(object, PhysicalLocation.class)) {
			Collection<PhysicalLocation> jurisdictions = (Collection<PhysicalLocation>) object;
			/* @formatter:off */
			Set<String> identifiers =jurisdictions
					.stream()
					.map(jurisdiction -> jurisdiction.getId())
					.collect(Collectors.toSet());
			return hasPermissionOnJurisdictions(authentication, identifiers);
			/* @formatter:on */
		}
		return object == null;
	}

	@Override
	public boolean hasPermission(Authentication authentication, PhysicalLocation jurisdiction) {
		return hasPermissionOnJurisdiction(authentication, jurisdiction.getId());
	}
}
