package org.opensrp.web.config.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.*;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = { "jedis" })
public class OAuth2SecurityConfigTest {
	
	@Test
	public void testTokenStore() throws SQLException {
		OAuth2SecurityConfig oAuth2SecurityConfig = new OAuth2SecurityConfig();
		DataSource mockDatasource = Mockito.mock(DataSource.class);
		Whitebox.setInternalState(oAuth2SecurityConfig, "dataSource", mockDatasource);
		
		OAuth2SecurityConfig.CustomJdbcTokenStore customJdbcTokenStore = (OAuth2SecurityConfig.CustomJdbcTokenStore) Mockito.spy(
				oAuth2SecurityConfig.tokenStore());
		JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
		Mockito.when(jdbcTemplate.update(Mockito.anyString())).thenReturn(1);
		Mockito.lenient().doNothing().when(customJdbcTokenStore).storeAccessTokenInSuper(Mockito.any(), Mockito.any());
		AuthenticationKeyGenerator authenticationKeyGenerator = Mockito.spy(new DefaultAuthenticationKeyGenerator());
		Whitebox.setInternalState(customJdbcTokenStore, "authenticationKeyGenerator", authenticationKeyGenerator);
		Whitebox.setInternalState(customJdbcTokenStore, "jdbcTemplate", jdbcTemplate);
		
		DefaultOAuth2AccessToken oAauth2AccessToken = new DefaultOAuth2AccessToken("some-token");
		OAuth2Request auth2Request = new OAuth2Request(null, null, null, true, null,
				null, null, null, null);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("demo", null);
		OAuth2Authentication auth2Authentication = new OAuth2Authentication(auth2Request, authentication);
		
		customJdbcTokenStore.storeAccessToken(oAauth2AccessToken, auth2Authentication);
		
		Mockito.verify(authenticationKeyGenerator).extractKey(Mockito.eq(auth2Authentication));
		Mockito.verify(jdbcTemplate).update(Mockito.anyString(),
				Mockito.anyString());
		
	}
	
}
