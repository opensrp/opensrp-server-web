/**
 *
 */
package org.opensrp.web.utils;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.opensrp.api.domain.User;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

/**
 * @author Samuel Githengi created on 06/25/20
 */
public class TestData {

	public static Pair<User, Authentication> getAuthentication(AccessToken token,
			KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal,
			RefreshableKeycloakSecurityContext securityContext) {

		List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");
		User user = new User(UUID.randomUUID().toString())
				.withRoles(roles)
				.withUsername("test_user1");

		when(token.getPreferredUsername()).thenReturn(user.getUsername());
		when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(securityContext);
		when(securityContext.getToken()).thenReturn(token);

		Authentication authentication = new Authentication() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getName() {
				return user.getBaseEntityId();
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
				// TODO Auto-generated method stub
			}

			@Override
			public boolean isAuthenticated() {
				return true;
			}

			@Override
			public Object getPrincipal() {
				return keycloakPrincipal;
			}

			@Override
			public Object getDetails() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return roles.stream().map(role -> new GrantedAuthority() {

					private static final long serialVersionUID = 1L;

					@Override
					public String getAuthority() {
						return role;
					}
				}).collect(Collectors.toList());
			}
		};

		return Pair.of(user, authentication);

	}
}
