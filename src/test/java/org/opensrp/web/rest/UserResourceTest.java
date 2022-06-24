/**
 *
 */
package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.TestSecurityConfig;
import org.opensrp.connector.openmrs.service.OpenmrsUserService;
import org.opensrp.web.bean.ResetPasswordBean;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.WebApplicationContext;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Samuel Githengi created on 10/14/19
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml"}, classes = {
        TestSecurityConfig.class})
public class UserResourceTest {

    private final String BASE_URL = "/rest/user";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Mock
    private OpenmrsUserService userService;
    @Autowired
    private KeycloakDeployment keycloakDeployment;
    @Autowired
    private KeycloakRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("#{opensrp['keycloak.password.reset.endpoint']}")
    private String resetPasswordURL;
    @Value("#{opensrp['keycloak.users.endpoint'] ?: '{0}/admin/realms/{1}/users'}")
    private String usersURL;
    @Captor
    private ArgumentCaptor<ResetPasswordBean> resetPasswordBeanCaptor;
    private MockMvc mockMvc;
    private UserResource userResource;

    @Before
    public void setUp() {
        userResource = webApplicationContext.getBean(UserResource.class);
        userResource.setUserService(userService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
    }

    @Test
    public void testGetAllUsers() throws Exception {
        String expected = "{\"person\":{\"display\":\"Reveal Tes Demo\"},\"display\":\"reveal\",\"uuid\":\"5e33cf03-2352nkh\"}";
        int limit = 10;
        int offset = 5;
        when(userService.getUsers(limit, offset)).thenReturn(new JSONObject(expected));
        MvcResult result = mockMvc.perform(get(BASE_URL + "?page_size=10&start_index=5")).andExpect(status().isOk())
                .andReturn();
        verify(userService).getUsers(limit, offset);
        verifyNoMoreInteractions(userService);
        assertEquals(expected, result.getResponse().getContentAsString());

    }

    @Test
    public void testChangePassword() throws Exception {
        String authServer = "http://localhost:8080/auth/";
        String realm = "opensrp";
        String url = MessageFormat.format(resetPasswordURL, authServer, realm);
        ResetPasswordBean passwordBean = new ResetPasswordBean();
        passwordBean.setNewPassword("KMnj(*10WQSH");
        passwordBean.setConfirmation("KMnj(*10WQSH");
        passwordBean.setCurrentPassword("Lmnj09NBG8");
        when(keycloakDeployment.getAuthServerBaseUrl()).thenReturn(authServer);
        when(keycloakDeployment.getRealm()).thenReturn(realm);
        doReturn(new ResponseEntity<String>("Successfully Password reset", HttpStatus.OK)).when(restTemplate)
                .postForEntity(anyString(), any(ResetPasswordBean.class), eq(String.class));

        Whitebox.setInternalState(userResource, "restTemplate", restTemplate);
        mockMvc.perform(post(BASE_URL + "/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(passwordBean))).andExpect(status().isOk());
        verify(restTemplate).postForEntity(eq(url), resetPasswordBeanCaptor.capture(), eq(String.class));
        assertEquals(passwordBean.getCurrentPassword(), resetPasswordBeanCaptor.getValue().getCurrentPassword());
        assertEquals(passwordBean.getNewPassword(), resetPasswordBeanCaptor.getValue().getNewPassword());
        assertEquals(passwordBean.getConfirmation(), resetPasswordBeanCaptor.getValue().getConfirmation());

    }

    @Test
    public void testChangePasswordWithError() throws Exception {
        String authServer = "http://localhost:8080/auth/";
        String realm = "opensrp";
        String url = MessageFormat.format(resetPasswordURL, authServer, realm);
        ResetPasswordBean passwordBean = new ResetPasswordBean();
        passwordBean.setNewPassword("KMnj(*10WQSH");
        passwordBean.setConfirmation("KMnj(*");
        passwordBean.setCurrentPassword("Lmnj09NBG8");
        when(keycloakDeployment.getAuthServerBaseUrl()).thenReturn(authServer);
        when(keycloakDeployment.getRealm()).thenReturn(realm);
        doThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Passwords not match")).when(restTemplate)
                .postForEntity(anyString(), any(ResetPasswordBean.class), eq(String.class));
        mockMvc.perform(post(BASE_URL + "/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(passwordBean))).andExpect(status().isBadRequest());
        verify(restTemplate, atLeastOnce()).postForEntity(eq(url), resetPasswordBeanCaptor.capture(), eq(String.class));
        assertEquals(passwordBean.getCurrentPassword(), resetPasswordBeanCaptor.getValue().getCurrentPassword());
        assertEquals(passwordBean.getNewPassword(), resetPasswordBeanCaptor.getValue().getNewPassword());
        assertEquals(passwordBean.getConfirmation(), resetPasswordBeanCaptor.getValue().getConfirmation());

    }

    @Test
    public void testGetAllKeycloakUsers() throws Exception {
        String authServer = "http://localhost:8080/auth/";
        String realm = "opensrp";
        String url = MessageFormat.format(usersURL, authServer, realm) + "?first={first}&max={max}";
        when(keycloakDeployment.getAuthServerBaseUrl()).thenReturn(authServer);
        when(keycloakDeployment.getRealm()).thenReturn(realm);
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("first", 0);
        uriVariables.put("max", 100);
        String expected = "[{\"id\":\"wewql9abe70ad66\",\"createdTimestamp\":1595352823770,\"username\":\"campdemo1\",\"enabled\":true,\"totp\":false,\"emailVerified\":false,\"firstName\":\"Abimbola\",\"lastName\":\"Phillips\"}]";
        doReturn(expected).when(restTemplate).getForObject(url, String.class, uriVariables);
        Whitebox.setInternalState(userResource, "restTemplate", restTemplate);
        MvcResult response = mockMvc
                .perform(
                        get(BASE_URL).param("page_size", "100").param("start_index", "0").param("source", UserResource.KEYCLOAK))
                .andExpect(status().isOk()).andReturn();
        verify(restTemplate).getForObject(url, String.class, uriVariables);
        assertEquals(expected, response.getResponse().getContentAsString());

    }

    @Test
    public void testGetKeycloakUsersCount() throws Exception {
        String authServer = "http://localhost:8080/auth/";
        String realm = "opensrp";
        String url = MessageFormat.format(usersURL, authServer, realm) + "/count";
        when(keycloakDeployment.getAuthServerBaseUrl()).thenReturn(authServer);
        when(keycloakDeployment.getRealm()).thenReturn(realm);
        String expected = "120";
        doReturn(new ResponseEntity<String>(expected, HttpStatus.OK)).when(restTemplate).getForEntity(url, String.class);
        Whitebox.setInternalState(userResource, "restTemplate", restTemplate);
        MvcResult response = mockMvc.perform(get(BASE_URL + "/count")).andExpect(status().isOk()).andReturn();
        verify(restTemplate).getForEntity(url, String.class);
        assertEquals(expected, response.getResponse().getContentAsString());

    }
}
