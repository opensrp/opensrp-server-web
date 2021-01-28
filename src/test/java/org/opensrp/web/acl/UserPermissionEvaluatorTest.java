package org.opensrp.web.acl;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public class UserPermissionEvaluatorTest {

	private UserPermissionEvaluator userPermissionEvaluator;

	@Before
	public void setUp() {
		userPermissionEvaluator = new UserPermissionEvaluator();
	}

	@Test
	public void testHasObjectPermission() {
		Authentication authentication = getMockedAuthentication();
        Boolean hasObjectPermission = userPermissionEvaluator.hasObjectPermission(authentication,"admin",null);
        assertTrue(hasObjectPermission);
	}

	@Test
	public void testHasPermission() {
		Authentication authentication = getMockedAuthentication();
		Boolean hasPermission = userPermissionEvaluator.hasPermission(authentication,"admin");
		assertTrue(hasPermission);
	}

	private Authentication getMockedAuthentication() {
		Authentication authentication = new Authentication() {

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return "";
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return "Test User";
			}

			@Override
			public boolean isAuthenticated() {
				return false;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
				// No implementation
			}

			@Override
			public String getName() {
				return "admin";
			}
		};

		return authentication;
	}
}
