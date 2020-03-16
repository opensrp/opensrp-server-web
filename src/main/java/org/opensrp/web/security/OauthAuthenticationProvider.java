package org.opensrp.web.security;

import org.opensrp.api.domain.User;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class OauthAuthenticationProvider extends DrishtiAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	public OauthAuthenticationProvider(OpenmrsUserService openmrsUserService) {
		super(openmrsUserService);
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (authentication.getDetails() instanceof WebAuthenticationDetails) {
			WebAuthenticationDetails authDetails = ((WebAuthenticationDetails) authentication.getDetails());
			//send requests for basic auth request or requests that dont have session to cache on redis 
			if (authDetails != null && authDetails.getSessionId() == null) {
				return super.authenticate(authentication);
			}
		}
		User user = getDrishtiUser(authentication, authentication.getName());
		// get user after authentication
		if (user == null) {
			throw new BadCredentialsException(USER_NOT_FOUND);
		}

		if (user.getVoided() != null && user.getVoided()) {
			throw new BadCredentialsException(USER_NOT_ACTIVATED);
		}

		Authentication auth = new UsernamePasswordAuthenticationToken(authentication.getName(),
				authentication.getCredentials(), getRolesAsAuthorities(user));
		return auth;
	}

}
