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
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = {"postgres"})
public class OAuth2SecurityConfigTest extends TestCase {
	
	
	@Autowired
	DataSource dataSource;
	@Test
	public void testTokenStore() throws SQLException {
		
		OAuth2SecurityConfig oAuth2SecurityConfig = new OAuth2SecurityConfig();
//		DataSource dataSource = Mockito.mock(DataSource.class);
		Whitebox.setInternalState(oAuth2SecurityConfig, "dataSource", dataSource);
		
		JdbcTokenStore jdbcTokenStore = oAuth2SecurityConfig.tokenStore();
		
		JdbcTemplate jdbcTemplateMock = new TestDatabaseConfig().jdbcTemplate();
		Mockito.when(dataSource.getConnection()).thenReturn(Mockito.mock(Connection.class));

		AuthenticationKeyGenerator authenticationKeyGenerator = Mockito.mock(AuthenticationKeyGenerator.class);
		Mockito.when(jdbcTemplateMock.update(Mockito.eq("delete from oauth_access_token where authentication_id = ?"), Mockito.eq("4e19bbaa33fc65f5951b336d7c11f6fc"))).thenReturn(1);
		
	//	Mockito.when(authenticationKeyGenerator.extractKey(Mockito.any())).thenReturn("some-key");
		Whitebox.setInternalState(jdbcTokenStore, "jdbcTemplate", jdbcTemplateMock);
		Whitebox.setInternalState(jdbcTokenStore, "authenticationKeyGenerator", authenticationKeyGenerator);
		
		OAuth2Authentication authenticationMock = Mockito.mock(OAuth2Authentication.class);
		OAuth2AccessToken tokenMock = Mockito.mock(OAuth2AccessToken.class);
		OAuth2Request auth2Request = new OAuth2Request(null,
				"some-client-id", null, true,
				null, null, null,
				null, null);
		Mockito.when(authenticationMock.getOAuth2Request()).thenReturn(auth2Request);
		Mockito.spy(jdbcTokenStore);
		
		jdbcTokenStore.storeAccessToken(tokenMock, authenticationMock);
		Mockito.verify(jdbcTemplateMock.update(Mockito.anyString(), Mockito.anyString()));
	}
}
