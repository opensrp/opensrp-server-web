/**
 * 
 */
package org.opensrp.web.config.security;

import org.opensrp.api.domain.User;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.security.OauthAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @author Samuel Githengi created on 07/02/20
 */
@Component
public class OpenmrsUserDetailsService implements UserDetailsService {
	
	@Autowired
	private OpenmrsUserService openmrsUserService;
	
	@Autowired
	private OauthAuthenticationProvider authenticationProvider;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = openmrsUserService.getUser(username);
		
		return new org.springframework.security.core.userdetails.User(user.getUsername(),"",
		        authenticationProvider.getRolesAsAuthorities(user));
	}
	
}
