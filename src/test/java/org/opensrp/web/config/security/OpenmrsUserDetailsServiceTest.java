/**
 * 
 */
package org.opensrp.web.config.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.api.domain.User;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.config.Role;
import org.opensrp.web.security.OauthAuthenticationProvider;
import org.powermock.reflect.Whitebox;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Samuel Githengi created on 07/06/20
 */

public class OpenmrsUserDetailsServiceTest {
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	private OpenmrsUserDetailsService userDetailsService;
	
	@Mock
	private OpenmrsUserService openmrsUserService;
	
	private OauthAuthenticationProvider authenticationProvider;
	
	@Before
	public void setUp() {
		userDetailsService = new OpenmrsUserDetailsService();
		authenticationProvider = new OauthAuthenticationProvider(openmrsUserService);
		Whitebox.setInternalState(userDetailsService, "openmrsUserService", openmrsUserService);
		Whitebox.setInternalState(userDetailsService, "authenticationProvider", authenticationProvider);
	}
	
	@Test
	public void testloadUserByUsername() {
		List<String> roles = Arrays.asList("OpenSRP: Get All Events");
		String username = "johndoe";
		User user = new User("1234", username, null, null, null, roles, roles);
		when(openmrsUserService.getUser(username)).thenReturn(user);
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		assertEquals(username, userDetails.getUsername());
		assertTrue(userDetails.getPassword().isBlank());
		assertEquals(1, userDetails.getAuthorities().size());
		assertEquals("ROLE_"+Role.ALL_EVENTS, userDetails.getAuthorities().iterator().next().getAuthority());
		verify(openmrsUserService).getUser(username);
		
	}
}
