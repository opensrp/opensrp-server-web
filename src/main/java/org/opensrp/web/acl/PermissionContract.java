/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;

import org.springframework.security.core.Authentication;

/**
 * @author Samuel Githengi created on 06/17/20
 */
public interface PermissionContract<T> {
	
	boolean hasPermission(Authentication authentication, T targetDomainObject);
	
	boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission);
}
