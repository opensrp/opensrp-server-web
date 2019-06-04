package org.opensrp.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opensrp.api.domain.User;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@RunWith(JUnit4.class)
public class OauthAuthenticationProviderTest {

	private OauthAuthenticationProvider authenticationProvider;

	private Authentication authentication;

	private OpenmrsUserService openmrsUserService;

	@Before
	public void setUp() {
		openmrsUserService = mock(OpenmrsUserService.class);
		authenticationProvider = new OauthAuthenticationProvider(openmrsUserService, mock(PasswordEncoder.class));
		authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("user1");
		when(authentication.getCredentials()).thenReturn("myPassword");
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticateWithNonExistentUserShouldFail() throws JSONException {
		when(openmrsUserService.getUser("user1")).thenReturn(null);
		authenticationProvider.authenticate(authentication);
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticateWithDeletedUserShouldFail() throws JSONException {
		User user = new User(UUID.randomUUID().toString());
		user.setDateVoided(new Date());
		when(openmrsUserService.getUser("user1")).thenReturn(user);
		authenticationProvider.authenticate(authentication);
	}

	@Test
	public void testAuthenticateWithValidUserShouldAuthorize() throws JSONException {
		User user = new User(UUID.randomUUID().toString());
		when(openmrsUserService.getUser("user1")).thenReturn(user);
		when(openmrsUserService.authenticate("user1", "myPassword")).thenReturn(true);
		authenticationProvider.authenticate(authentication);
		verify(openmrsUserService).getUser("user1");
		verify(openmrsUserService).authenticate("user1", "myPassword");
	}

	@Test
	public void testGetRolesAsAuthoritiesRuturnsAllRoles() throws JSONException {
		User user = new User(UUID.randomUUID().toString());
		user.setRoles(Arrays.asList("Provider", "OpenSRP: Get All Events"));
		when(openmrsUserService.getUser("user1")).thenReturn(user);
		when(openmrsUserService.authenticate("user1", "myPassword")).thenReturn(true);
		Authentication auth = authenticationProvider.authenticate(authentication);
		verify(openmrsUserService).getUser("user1");
		verify(openmrsUserService).authenticate("user1", "myPassword");
		assertEquals(2, auth.getAuthorities().size());
		assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ALL_EVENTS")));
		assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OPENMRS")));
	}

}
