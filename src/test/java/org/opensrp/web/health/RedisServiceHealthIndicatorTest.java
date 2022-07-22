package org.opensrp.web.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.web.Constants;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "oauth2"})
public class RedisServiceHealthIndicatorTest {

    @Autowired
    private RedisServiceHealthIndicator redisServiceHealthIndicator;

    @Test
    public void testDoHealthCheckShouldReturnValidMap() throws Exception {
        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        Whitebox.setInternalState(redisServiceHealthIndicator, "redisConnectionFactory", redisConnectionFactory);
        ModelMap map = redisServiceHealthIndicator.doHealthCheck().call();
        assertNotNull(map);
        assertTrue(map.containsKey(Constants.HealthIndicator.EXCEPTION));
        assertTrue(map.containsKey(Constants.HealthIndicator.STATUS));
        assertEquals(map.get(Constants.HealthIndicator.INDICATOR), "redis");
    }

    @Test
    public void testDoHealthCheckShouldReturnValidMapWithoutException() throws Exception {
        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        JedisConnection redisConnection = mock(JedisConnection.class);
        Jedis jedis = mock(Jedis.class);
        doReturn("PONG").when(jedis).ping();
        doReturn(redisConnection).when(redisConnectionFactory).getConnection();
        doReturn(jedis).when(redisConnection).getNativeConnection();
        Whitebox.setInternalState(redisServiceHealthIndicator, "redisConnectionFactory", redisConnectionFactory);
        ModelMap map = redisServiceHealthIndicator.doHealthCheck().call();
        assertNotNull(map);
        assertFalse(map.containsKey(Constants.HealthIndicator.EXCEPTION));
        assertTrue(map.containsKey(Constants.HealthIndicator.STATUS));
        assertEquals(map.get(Constants.HealthIndicator.INDICATOR), "redis");
    }

}
