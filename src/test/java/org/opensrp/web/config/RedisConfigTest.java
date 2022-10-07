package org.opensrp.web.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*",  "com.sun.org.apache.xerces.*"})
public class RedisConfigTest {
	private RedisConfig redisConfig;

	@Before
	public void setUp() {
		redisConfig = new RedisConfig();
	}

	@Test
	public void testGetArchitectureReturnedOnValidInput() throws Exception {
		WhiteboxImpl.setInternalState(redisConfig, "redisArchitecture", "standalone");
		RedisConfig.Architecture architecture = WhiteboxImpl.invokeMethod(redisConfig, "getArchitecture");
		Assert.assertEquals(architecture, RedisConfig.Architecture.STANDALONE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetArchitectureShouldThrowExceptionOnInvalidInput() throws Exception {
		WhiteboxImpl.setInternalState(redisConfig, "redisArchitecture", "");
		WhiteboxImpl.invokeMethod(redisConfig, "getArchitecture");
	}

	@Test
	public void testGetJedisConnectionFactoryReturnNonNullStandaloneJedisConnectionFactory() throws Exception {
		WhiteboxImpl.setInternalState(redisConfig, "redisArchitecture", "standalone");
		WhiteboxImpl.setInternalState(redisConfig, "redisDatabase", 0);
		WhiteboxImpl.setInternalState(redisConfig, "redisPassword", "redis");
		WhiteboxImpl.setInternalState(redisConfig, "redisPort", 6379);
		WhiteboxImpl.setInternalState(redisConfig, "redisHost", "localhost");

		JedisConnectionFactory jedisConnectionFactory = WhiteboxImpl.invokeMethod(redisConfig,
				"getJedisConnectionFactory",
				JedisClientConfiguration.builder().build());
		Assert.assertNotNull(jedisConnectionFactory);
		Assert.assertNotNull(jedisConnectionFactory.getStandaloneConfiguration());
		Assert.assertNull(jedisConnectionFactory.getSentinelConfiguration());
	}

	@Test
	public void testGetJedisConnectionFactoryReturnNonNullSentinelJedisConnectionFactory() throws Exception {
		WhiteboxImpl.setInternalState(redisConfig, "redisArchitecture", "sentinel");
		WhiteboxImpl.setInternalState(redisConfig, "redisDatabase", 0);
		WhiteboxImpl.setInternalState(redisConfig, "redisMaster", "mymaster");
		WhiteboxImpl.setInternalState(redisConfig, "redisSentinels", "");

		JedisConnectionFactory jedisConnectionFactory = WhiteboxImpl.invokeMethod(redisConfig,
				"getJedisConnectionFactory",
				JedisClientConfiguration.builder().build());
		Assert.assertNotNull(jedisConnectionFactory);
		Assert.assertNotNull(jedisConnectionFactory.getSentinelConfiguration());
	}

	@Test
	public void testGetRedisConnectionFactoryReturnNonNullSentinelRedisConfiguration() throws Exception {
		WhiteboxImpl.setInternalState(redisConfig, "redisDatabase", 0);
		WhiteboxImpl.setInternalState(redisConfig, "redisPassword", "redis");
		WhiteboxImpl.setInternalState(redisConfig, "redisMaster", "mymaster");
		WhiteboxImpl.setInternalState(redisConfig, "redisSentinels", "");

		RedisConfiguration configuration = WhiteboxImpl.invokeMethod(redisConfig, "getRedisConnectionFactory",
				RedisConfig.Architecture.SENTINEL);
		Assert.assertNotNull(configuration);
		Assert.assertTrue(configuration instanceof RedisSentinelConfiguration);
	}

	@Test
	public void testGetRedisConnectionFactoryReturnNonNullStandaloneRedisConfiguration() throws Exception {
		WhiteboxImpl.setInternalState(redisConfig, "redisDatabase", 0);
		WhiteboxImpl.setInternalState(redisConfig, "redisPassword", "redis");
		WhiteboxImpl.setInternalState(redisConfig, "redisHost", "localhost");
		WhiteboxImpl.setInternalState(redisConfig, "redisPort", 6379);

		RedisConfiguration configuration = WhiteboxImpl.invokeMethod(redisConfig, "getRedisConnectionFactory",
				RedisConfig.Architecture.STANDALONE);
		Assert.assertNotNull(configuration);
		Assert.assertTrue(configuration instanceof RedisStandaloneConfiguration);
	}
}
