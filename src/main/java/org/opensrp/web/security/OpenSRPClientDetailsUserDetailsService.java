package org.opensrp.web.security;

import java.util.List;

import org.json.JSONException;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.convert.Converter;

public class OpenSRPClientDetailsUserDetailsService extends ClientDetailsUserDetailsService {

	private OpenmrsUserService openmrsUserService;

	public OpenSRPClientDetailsUserDetailsService(ClientDetailsService clientDetailsService) {
		super(clientDetailsService);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			org.opensrp.api.domain.User user = openmrsUserService.getUser(username);
			User userDetails = new User(username, user.getPassword(), getRolesAsAuthorities(user));
			return userDetails;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

	}

	private List<SimpleGrantedAuthority> getRolesAsAuthorities(org.opensrp.api.domain.User user) {
		return Lambda.convert(user.getRoles(), new Converter<String, SimpleGrantedAuthority>() {

			@Override
			public SimpleGrantedAuthority convert(String role) {
				return new SimpleGrantedAuthority("ROLE_OPENMRS");
			}
		});
	}

}
