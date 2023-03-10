package org.opensrp.web.config.security;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensrp.TestDatabaseConfig;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"postgres", "jedis"})
public class OAuth2SecurityConfigTest extends TestCase {
	
	
	@Autowired
	DataSource dataSource;
	@Test
	public void testTokenStore() throws SQLException {
		
		OAuth2SecurityConfig oAuth2SecurityConfig = new OAuth2SecurityConfig();
		Whitebox.setInternalState(oAuth2SecurityConfig, "dataSource", dataSource);
		
		JdbcTokenStore jdbcTokenStore = oAuth2SecurityConfig.tokenStore();
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		AuthenticationKeyGenerator authenticationKeyGenerator =  new DefaultAuthenticationKeyGenerator();
//		Mockito.when(jdbcTemplateMock.update(Mockito.eq("delete from oauth_access_token where authentication_id = ?"), Mockito.eq("4e19bbaa33fc65f5951b336d7c11f6fc"))).thenReturn(1);

//		jdbcTemplate.update("insert into oauth_access_token (authentication_id) values (?)", "4e19bbaa33fc65f5951b336d7c11f6fc");
	//	Mockito.when(authenticationKeyGenerator.extractKey(Mockito.any())).thenReturn("some-key");
		Whitebox.setInternalState(jdbcTokenStore, "jdbcTemplate", jdbcTemplate);
		Whitebox.setInternalState(jdbcTokenStore, "authenticationKeyGenerator", authenticationKeyGenerator);
		
		
		OAuth2Request auth2Request = new OAuth2Request(null,
				"some-client-id", null, true,
				null, null, null,
				null, null);
		Authentication authentication = new Authentication() {
			
			private boolean isAuthenticated = true;
			
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}
			
			@Override
			public Object getCredentials() {
				return null;
			}
			
			@Override
			public Object getDetails() {
				return null;
			}
			
			@Override
			public Object getPrincipal() {
				return "some-principle";
			}
			
			@Override
			public boolean isAuthenticated() {
				return this.isAuthenticated;
			}
			
			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
				this.isAuthenticated = isAuthenticated;
			}
			
			@Override
			public String getName() {
				return null;
			}
		};
		OAuth2Authentication authenticationStub = new OAuth2Authentication(auth2Request, authentication);
		OAuth2AccessToken oAuth2AccessTokenStub = new DefaultOAuth2AccessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
		jdbcTokenStore.storeAccessToken(oAuth2AccessTokenStub, authenticationStub);
//		Mockito.verify(jdbcTemplate.update(Mockito.anyString(), Mockito.anyString()));
	}
}
