package org.opensrp.web.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class RedisHttpSessionTest {

	private Jedis jedis;

	@Before
	public void clearRedisData() {
		MockitoAnnotations.initMocks(this);
		jedis = new Jedis("localhost", 6379);
		jedis.flushAll();
	}

	@Test
	public void testRedisIsEmpty() {
		Set<String> result = jedis.keys("*");
		assertEquals(0, result.size());
	}
}
