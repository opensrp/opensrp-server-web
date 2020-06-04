/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 * @author Samuel Githengi created on 06/03/20
 */
public class ACLPermissionEvaluator implements PermissionEvaluator{

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
	        Object permission) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
