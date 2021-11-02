package org.opensrp.web.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.web.Constants;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "basic_auth", "rabbitmq"})
public class RabbitmqServiceHealthIndicatorTest {

    @Autowired
    private RabbitmqServiceHealthIndicator rabbitmqServiceHealthIndicator;

    @Test
    public void testDoHealthCheckShouldReturnValidMap() throws Exception {
        AmqpAdmin amqpAdmin = mock(AmqpAdmin.class);
        Whitebox.setInternalState(rabbitmqServiceHealthIndicator, "amqpAdmin", amqpAdmin);
        ModelMap map = rabbitmqServiceHealthIndicator.doHealthCheck().call();
        assertNotNull(map);
        assertFalse(map.containsKey(Constants.HealthIndicator.EXCEPTION));
        assertTrue(map.containsKey(Constants.HealthIndicator.STATUS));
        assertEquals(map.get(Constants.HealthIndicator.INDICATOR), "rabbitmq");
    }

}