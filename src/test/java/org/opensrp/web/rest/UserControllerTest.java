package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.api.domain.User;
import org.opensrp.common.domain.UserDetail;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.controller.UserController;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
	private OpenmrsUserService openmrsUserService;
	
	@Mock
	private Authentication authentication;
	
	@Mock
	private KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal;
	
	@Mock
	private KeycloakSecurityContext securityContext;
	
	@Mock
	private AccessToken token;
	
	@Captor
	private ArgumentCaptor<Authentication> authenticationCaptor;
	
	@Captor
	private ArgumentCaptor<String> usernameCaptor;
	
	private UserController userController;
	
	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
		userController = webApplicationContext.getBean(UserController.class);
		userController.setOpenmrsUserService(openmrsUserService);
		when(keycloakPrincipal.getKeycloakSecurityContext()).thenReturn(securityContext);
		when(securityContext.getToken()).thenReturn(token);
	}
	
	@Test
	public void testGetUserDetailsReturnsCorrectDetails() throws Exception {
		
		User user = new User(UUID.randomUUID().toString()).withRoles(Collections.singletonList("ROLE_USER"))
		        .withUsername("test_user1");
		when(authentication.getPrincipal()).thenReturn(keycloakPrincipal);
		when(token.getPreferredUsername()).thenReturn(user.getUsername());
		when(token.getId()).thenReturn(user.getBaseEntityId());
		when(authentication.getAuthorities()).thenAnswer(a -> user.getRoles().stream().map(role -> new GrantedAuthority() {
			
			@Override
			public String getAuthority() {
				return role;
			}
		}).collect(Collectors.toList()));
		ResponseEntity<UserDetail> result = userController.getUserDetails(authentication);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		UserDetail userDetail = result.getBody();
		assertEquals(user.getUsername(), userDetail.getUserName());
		assertEquals(user.getBaseEntityId(), userDetail.getIdentifier());
		assertEquals(user.getRoles(), userDetail.getRoles());
		
	}
	
	@Test
	public void testGetUserDetailReturnsNull() throws Exception {
		ResponseEntity<UserDetail> result = userController.getUserDetails(authentication);
		assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
		UserDetail userDetail = result.getBody();
		assertNull(userDetail);
	}
	
	@Test
	public void testGetUserDetailsShouldReturnUnauthorized() throws Exception {
		mockMvc.perform(get("/user-details")).andExpect(status().isUnauthorized()).andReturn();
	}
}
