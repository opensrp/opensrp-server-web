/**
 *
 */
package org.opensrp.web.acl;

import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * @author Samuel Githengi created on 06/17/20
 */
public interface PermissionContract<T> {

    boolean hasPermission(Authentication authentication, T targetDomainObject);

    boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission);
}
