/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;

import org.opensrp.common.domain.UserDetail;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/17/20
 */
@Component
public class UserPermissionEvaluator   extends BasePermissionEvaluator<String>{

	@Override
	public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication, String targetDomainObject) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
