package org.opensrp.web.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.web.Constants;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "oauth2"})
public class KeycloakServiceHealthIndicatorTest {

    @Autowired
    private KeycloakServiceHealthIndicator keycloakServiceHealthIndicator;

    @Test
    public void testDoHealthCheckShouldReturnValidMap() throws Exception {
        ModelMap map = keycloakServiceHealthIndicator.doHealthCheck().call();
        assertNotNull(map);
        assertTrue(map.containsKey(Constants.HealthIndicator.EXCEPTION));
        assertTrue(map.containsKey(Constants.HealthIndicator.STATUS));
        assertEquals(map.get(Constants.HealthIndicator.INDICATOR), "keycloak");
    }

}
