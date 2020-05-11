package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.api.domain.User;
import org.opensrp.common.domain.UserDetail;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.controller.UserController;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.security.DrishtiAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class UserControllerTest {

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private DrishtiAuthenticationProvider opensrpAuthenticationProvider;

	@Mock
	private OpenmrsUserService openmrsUserService;

	@Captor
	private ArgumentCaptor<Authentication> authenticationCaptor;

	@Captor
	private ArgumentCaptor<String> usernameCaptor;

	private UserController userController;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext).
				addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
		userController = webApplicationContext.getBean(UserController.class);
		userController.setOpenmrsUserService(openmrsUserService);
		userController.setOpensrpAuthenticationProvider(opensrpAuthenticationProvider);
	}

	@Test
	public void testGetUserDetailsShouldUseParam() throws Exception {

		User user = new User(UUID.randomUUID().toString()).withRoles(Collections.singletonList("ROLE_USER"))
				.withUsername("test_user1");

		Authentication authentication = new UsernamePasswordAuthenticationToken("test_user1", "");
		when(openmrsUserService.getUser("test_user1")).thenReturn(user);
		ResponseEntity<UserDetail> result = userController.getUserDetails(authentication, "test_user1", null);
		verify(opensrpAuthenticationProvider, never()).getDrishtiUser(authenticationCaptor.capture(),
				usernameCaptor.capture());
		verify(openmrsUserService).getUser("test_user1");
		assertEquals(HttpStatus.OK, result.getStatusCode());
		UserDetail userDetail = result.getBody();
		assertEquals("test_user1", userDetail.getUserName());
		assertEquals(user.getRoles(), userDetail.getRoles());

	}

	@Test
	public void testGetUserDetailsShouldUseLoggedInUser() throws Exception {
		User user = new User(UUID.randomUUID().toString()).withRoles(Collections.singletonList("ROLE_USER"))
				.withUsername("loggenIn_user");
		Authentication authentication = new UsernamePasswordAuthenticationToken("loggenIn_user", "");
		when(opensrpAuthenticationProvider.getDrishtiUser(any(Authentication.class), anyString())).thenReturn(user);
		when(openmrsUserService.getUser(user.getUsername())).thenReturn(user);
		ResponseEntity<UserDetail> result = userController.getUserDetails(authentication, null, null);
		verify(opensrpAuthenticationProvider, never()).getDrishtiUser(authenticationCaptor.capture(),
				usernameCaptor.capture());
		assertEquals(HttpStatus.OK, result.getStatusCode());
		UserDetail userDetail = result.getBody();
		assertEquals(user.getUsername(), userDetail.getUserName());
		assertEquals(user.getRoles(), userDetail.getRoles());
	}

	@Test
	public void testGetUserDetailsShouldReturnUnauthorized() throws Exception {
		when(opensrpAuthenticationProvider.getDrishtiUser(any(Authentication.class), anyString())).thenReturn(null);
		mockMvc.perform(get("/user-details")).andExpect(status().isUnauthorized()).andReturn();
		verify(opensrpAuthenticationProvider, never()).getDrishtiUser(authenticationCaptor.capture(),
				usernameCaptor.capture());
	}
}
