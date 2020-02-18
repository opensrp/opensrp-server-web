package org.opensrp.web.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.reflection.Whitebox;
import org.opensrp.domain.custom.Role;
import org.opensrp.domain.custom.User;
import org.opensrp.web.custom.service.CustomUserService;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@RunWith(JUnit4.class)
public class OauthAuthenticationProviderTest {

	private OauthAuthenticationProvider authenticationProvider;

	private Authentication authentication;

	private CustomUserService userService;

	private ArgumentCaptor<Authentication> authCaptor = ArgumentCaptor.forClass(Authentication.class);

	private ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);;

	@Before
	public void setUp() {
		userService = mock(CustomUserService.class);
		authenticationProvider = new OauthAuthenticationProvider(userService);
		authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("user1");
		when(authentication.getCredentials()).thenReturn("myPassword");
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticateWithNonExistentUserShouldFail() throws JSONException {
		when(userService.findByUserName("user1")).thenReturn(null);
		authenticationProvider.authenticate(authentication);
	}

	@Test(expected = BadCredentialsException.class)
	public void testAuthenticateWithDeletedUserShouldFail() throws JSONException {
		User user = new User("user1", "abc", "abc@g.com", null);

		when(userService.findByUserName("user1")).thenReturn(user);
		authenticationProvider.authenticate(authentication);
	}

	@Test
	public void testAuthenticateWithValidUserShouldAuthorize() throws JSONException {
		User user = new User("user1", "abc", "abc@g.com", null);
		when(userService.findByUserName("user1")).thenReturn(user);
		when(userService.authenticate("user1", "myPassword")).thenReturn(user);
		authenticationProvider.authenticate(authentication);
		verify(userService).findByUserName("user1");
		verify(userService).authenticate("user1", "myPassword");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAuthenticateWithDetailsCachesAuthentication() throws JSONException {
		User user = new User("user1", "abc", "abc@g.com", null);
		WebAuthenticationDetails authenticationDetails = mock(WebAuthenticationDetails.class);
		when(authenticationDetails.getRemoteAddress()).thenReturn("::");
		when(authentication.getDetails()).thenReturn(authenticationDetails);
		when(userService.findByUserName("user1")).thenReturn(user);
		when(userService.authenticate("user1", "myPassword")).thenReturn(user);
		HashOperations<String, String, Authentication> hashOperations = mock(HashOperations.class);
		Whitebox.setInternalState(authenticationProvider, "hashOps", hashOperations);
		Whitebox.setInternalState(authenticationProvider, "redisTemplate", mock(RedisTemplate.class));
		authenticationProvider.authenticate(authentication);
		verify(userService).findByUserName("user1");
		verify(userService).authenticate("user1", "myPassword");
		verify(hashOperations).hasKey("::user1", DrishtiAuthenticationProvider.AUTH_HASH_KEY);
		verify(hashOperations).put(stringCaptor.capture(), stringCaptor.capture(), authCaptor.capture());
		assertEquals(2, stringCaptor.getAllValues().size());
		assertEquals("::user1", stringCaptor.getAllValues().get(0));
		assertEquals(DrishtiAuthenticationProvider.AUTH_HASH_KEY, stringCaptor.getAllValues().get(1));
		assertEquals("user1", authCaptor.getValue().getName());
		assertEquals("user1", authCaptor.getValue().getPrincipal());
	}

	@Test
	public void testGetRolesAsAuthoritiesRuturnsAllRoles() throws JSONException {
		User user = new User("user1", "abc", "abc@g.com", null);
		
		Set<Role> roles = new HashSet<>();
		roles.add(new Role("Provider"));
		roles.add(new Role("OpenSRP: Get All Events"));
		
		user.setRoles(roles);
		when(userService.findByUserName("user1")).thenReturn(user);
		when(userService.authenticate("user1", "myPassword")).thenReturn(user);
		Authentication auth = authenticationProvider.authenticate(authentication);
		verify(userService).findByUserName("user1");
		verify(userService).authenticate("user1", "myPassword");
		assertEquals(2, auth.getAuthorities().size());
		assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ALL_EVENTS")));
		assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OPENMRS")));
	}

}
