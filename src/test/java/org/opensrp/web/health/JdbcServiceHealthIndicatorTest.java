package org.opensrp.web.health;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.web.Constants;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "oauth2"})
public class JdbcServiceHealthIndicatorTest {

    @Autowired
    private JdbcServiceHealthIndicator jdbcServiceHealthIndicator;

    @Test
    public void testDoHealthCheckShouldReturnValidMap() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Whitebox.setInternalState(jdbcServiceHealthIndicator, "dataSource", dataSource);
        ModelMap map = jdbcServiceHealthIndicator.doHealthCheck().call();
        assertNotNull(map);
        assertTrue(map.containsKey(Constants.HealthIndicator.EXCEPTION));
        assertTrue(map.containsKey(Constants.HealthIndicator.STATUS));
        assertEquals(map.get(Constants.HealthIndicator.INDICATOR), "postgres");
    }

}
