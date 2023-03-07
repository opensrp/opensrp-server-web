/**
 * 
 */
package org.opensrp;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Samuel Githengi created on 05/11/20
 */
@Configuration
@EnableCaching
public class TestRedisConfig {
	
	@Value("#{opensrp['redis.host']}")
	private String redisHost;
	
	@Value("#{opensrp['redis.port']}")
	private int redisPort;
	
	private int redisDatabase = 0;
	
	@Value("#{opensrp['redis.pool.max.connections']}")
	private int redisMaxConnections = 0;
	
	@Value("#{opensrp['redis.password']}")
	private String redisPassword;
	
	
	private RedisStandaloneConfiguration redisStandaloneConfiguration() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
		redisStandaloneConfiguration.setDatabase(redisDatabase);
		redisStandaloneConfiguration.setPassword(redisPassword);
		return redisStandaloneConfiguration;
	}
	
	@SuppressWarnings("rawtypes")
	private GenericObjectPoolConfig poolConfig() {
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(redisMaxConnections);
		return poolConfig;
	}
	
	@Profile("jedis")
	@Bean(name = "jedisConnectionFactory")
	public RedisConnectionFactory jedisConnectionFactory() {
		JedisClientConfiguration clientConfiguration = JedisClientConfiguration.builder().usePooling()
		        .poolConfig(poolConfig()).build();
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration(),
		        clientConfiguration);
		return jedisConnectionFactory;
	}
	
	@Profile("jedis")
	@Bean(name = "redisTemplate")
	public RedisTemplate<String, String> jedisTemplate() {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	@Profile("jedis")
	@Bean
	public CacheManager cacheManager() {
		RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder
		        .fromConnectionFactory(jedisConnectionFactory());
		return builder.build();
	}
}
