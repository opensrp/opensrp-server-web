/**
 * 
 */
package org.opensrp.web.acl;

import java.io.Serializable;

import org.opensrp.domain.PhysicalLocation;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 06/17/20
 */
@Component
public class JurisdictionPermissionEvaluator extends BasePermissionEvaluator<PhysicalLocation> {
	
	@Override
	public boolean hasObjectPermission(Authentication authentication, Serializable targetId, Object permission) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean hasPermission(Authentication authentication, PhysicalLocation jurisdiction) {
		return hasPermissionOnJurisdiction(authentication, jurisdiction.getId());
	}
	
}
