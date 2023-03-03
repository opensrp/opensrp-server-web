package org.opensrp.web.config.security;

import junit.framework.TestCase;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

public class OAuth2SecurityConfigTest extends TestCase {
	
	
	
	public void testTokenStore() {
		
		OAuth2SecurityConfig oAuth2SecurityConfig = new OAuth2SecurityConfig();
		DataSource dataSource = Mockito.mock(DataSource.class);
		Whitebox.setInternalState(oAuth2SecurityConfig, "dataSource", dataSource);
		JdbcTokenStore jdbcTokenStore = oAuth2SecurityConfig.tokenStore();
		JdbcTemplate jdbcTemplateMock = Mockito.mock(JdbcTemplate.class);
		AuthenticationKeyGenerator authenticationKeyGenerator = Mockito.mock(AuthenticationKeyGenerator.class);
		Whitebox.setInternalState(jdbcTokenStore, "jdbcTemplate", jdbcTemplateMock);
		Whitebox.setInternalState(jdbcTokenStore, "authenticationKeyGenerator", authenticationKeyGenerator);
		
		Mockito.when(authenticationKeyGenerator.extractKey(Mockito.any())).thenReturn("some-key");
		
		
		
		OAuth2Authentication authenticationMock = Mockito.mock(OAuth2Authentication.class);
		OAuth2AccessToken tokenMock = Mockito.mock(OAuth2AccessToken.class);
		Mockito.spy(jdbcTokenStore);
		
		jdbcTokenStore.storeAccessToken(tokenMock, authenticationMock);
		Mockito.verify(jdbcTemplateMock.update(Mockito.anyString()));
	}
}
