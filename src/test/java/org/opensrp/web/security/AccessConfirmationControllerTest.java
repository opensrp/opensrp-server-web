package org.opensrp.web.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.web.servlet.ModelAndView;

@RunWith(JUnit4.class)
public class AccessConfirmationControllerTest {

	private AccessConfirmationController controller;

	private ClientDetailsService clientDetailsService;

	private Map<String, Object> model;

	@Before
	public void setUp() {
		clientDetailsService = mock(ClientDetailsService.class);
		controller = new AccessConfirmationController();
		controller.setClientDetailsService(clientDetailsService);
		model = new HashMap<>();
	}

	@Test
	public void testGetAccessConfirmation() throws Exception {
		AuthorizationRequest clientAuth = mock(AuthorizationRequest.class);
		ClientDetails client = mock(ClientDetails.class);
		when(clientDetailsService.loadClientByClientId(null)).thenReturn(client);
		model.put("authorizationRequest", clientAuth);
		ModelAndView modelAndView = controller.getAccessConfirmation(model);
		verify(clientDetailsService).loadClientByClientId(eq(null));
		assertEquals(clientAuth, modelAndView.getModel().get("auth_request"));
		assertEquals(client, modelAndView.getModel().get("client"));
		assertEquals("access_confirmation", modelAndView.getViewName());
	}

	@Test
	public void testHandleError() throws Exception {
		String view = controller.handleError(model);
		assertEquals("oauth_error", view);
		assertEquals("There was a problem with the OAuth2 protocol", model.get("message"));
	}

}
