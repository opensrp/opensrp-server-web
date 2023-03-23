package org.opensrp.web.config.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.common.*;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.Serializable;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
@ActiveProfiles(profiles = { "postgres", "jedis" })
public class OAuth2SecurityConfigTest implements Serializable {
	
	@Autowired
	DataSource dataSource;
	
	@Test
	public void testTokenStore() {
		OAuth2SecurityConfig oAuth2SecurityConfig = new OAuth2SecurityConfig();
		Whitebox.setInternalState(oAuth2SecurityConfig, "dataSource", dataSource);
		
		JdbcTokenStore jdbcTokenStore = Mockito.spy(oAuth2SecurityConfig.tokenStore());
		JdbcTemplate jdbcTemplate = Mockito.spy(new JdbcTemplate(dataSource));
		AuthenticationKeyGenerator authenticationKeyGenerator = Mockito.spy(new DefaultAuthenticationKeyGenerator());
		Whitebox.setInternalState(jdbcTokenStore, "authenticationKeyGenerator", authenticationKeyGenerator);
		Whitebox.setInternalState(jdbcTokenStore, "jdbcTemplate", jdbcTemplate);
		
		DefaultOAuth2AccessToken oAauth2AccessToken = new DefaultOAuth2AccessToken("OwBog1J31N2e28B68_xdkWfB9v8");
		OAuth2Request auth2Request = new OAuth2Request(null, null, null, true, null,
				null, null, null, null);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("demo", null);
		OAuth2Authentication auth2Authentication = new OAuth2Authentication(auth2Request, authentication);
		
		jdbcTokenStore.storeAccessToken(oAauth2AccessToken, auth2Authentication);
		
		Mockito.verify(authenticationKeyGenerator).extractKey(Mockito.eq(auth2Authentication));
		Mockito.verify(jdbcTemplate).update(Mockito.eq("delete from oauth_access_token where authentication_id = ?"),
				Mockito.eq("7046bfd2677e8ea124d5270f8a6a2223"));
		
	}
	
}
