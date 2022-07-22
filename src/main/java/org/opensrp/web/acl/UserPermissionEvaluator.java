/**
 *
 */
package org.opensrp.web.acl;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author Samuel Githengi created on 06/17/20
 */
@Component
public class UserPermissionEvaluator extends BasePermissionEvaluator<String> {

    @Override
    public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
        return authentication.getName().equals(targetId);
    }

    @Override
    public boolean hasPermission(Authentication authentication, String username) {
        return authentication.getName().equals(username);
    }

}
