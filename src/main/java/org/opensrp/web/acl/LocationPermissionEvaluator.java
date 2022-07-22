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
    public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
        if (targetId instanceof String) {
            return hasPermissionOnJurisdiction(authentication, (String) targetId);
        } else if (isCollectionOfString(targetId)) {
            return hasPermissionOnJurisdictions(authentication, (Collection<String>) targetId);
        } else if (targetId instanceof PhysicalLocation) {
            return hasPermission(authentication, (PhysicalLocation) targetId);
        } else if (isCollectionOfResources(targetId, PhysicalLocation.class)) {
            Collection<PhysicalLocation> jurisdictions = (Collection<PhysicalLocation>) targetId;
            /* @formatter:off */
            Set<String> identifiers = jurisdictions
                    .stream()
                    .map(jurisdiction -> jurisdiction.getId())
                    .collect(Collectors.toSet());
            return hasPermissionOnJurisdictions(authentication, identifiers);
            /* @formatter:on */
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, PhysicalLocation jurisdiction) {
        return hasPermissionOnJurisdiction(authentication, jurisdiction.getId());
    }

}
