/**
 *
 */
package org.opensrp.web.acl;

import org.smartregister.domain.PlanDefinition;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
     * @param permission     the permission to evaluate
     * @return true/false if user has the permission for the plan
     */
    public boolean hasPermission(Authentication authentication, PlanDefinition targetDomainObject) {
        PlanDefinition plan = targetDomainObject;
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
     * @param object
     * @param permission
     * @return
     */
    @SuppressWarnings("unchecked")
    public boolean hasObjectPermission(Authentication authentication, Serializable object, Object permission) {
        if (object instanceof String) {
            /* @formatter:off */
            return getAssignedLocations(authentication.getName())
                    .stream()
                    .anyMatch(assignedLocation -> assignedLocation.getPlanId().equals(object));
            /* @formatter:on */
        } else if (isCollectionOfString(object)) {
            Collection<String> identifiers = (Collection<String>) object;
            /* @formatter:off */
            return getAssignedLocations(authentication.getName())
                    .stream()
                    .allMatch((assignedLocation) -> {
                        return identifiers.contains(assignedLocation.getPlanId());
                    });
            /* @formatter:on */
        } else if (object instanceof PlanDefinition) {
            PlanDefinition plan = (PlanDefinition) object;
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
        } else if (isCollectionOfResources(object, PlanDefinition.class)) {
            Collection<PlanDefinition> plans = (Collection<PlanDefinition>) object;
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
        return object == null;
    }
}
