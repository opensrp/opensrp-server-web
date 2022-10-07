/**
 * 
 */
package org.opensrp.web.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Samuel Githengi created on 05/11/20
 */
@Configuration
@EnableCaching
public class RedisConfig {
	
	@Value("#{opensrp['redis.host']}")
	private String redisHost;
	
	@Value("#{opensrp['redis.port']}")
	private int redisPort;
	
	@Value("#{opensrp['redis.password']}")
	private String redisPassword;
	
	private int redisDatabase = 0;
	
	@Value("#{opensrp['redis.pool.max.connections']}")
	private int redisMaxConnections = 0;

	@Value("#{opensrp['redis.architecture'] ?: 'standalone'}")
	private String redisArchitecture;

	@Value("#{opensrp['redis.sentinels'] ?: ''}")
	private String redisSentinels;
	@Value("#{opensrp['redis.master'] ?: ''}")
	private String redisMaster;

	protected enum Architecture {
		STANDALONE,
		SENTINEL
	}
	@Profile("lettuce")
	@Bean(name = "lettuceConnectionFactory")
	public RedisConnectionFactory lettuceConnectionFactory() {
		LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
		        .poolConfig(poolConfig()).build();
		return new LettuceConnectionFactory(getRedisConnectionFactory(getArchitecture()),
		        lettuceClientConfiguration);
	}

	private RedisConfiguration getRedisConnectionFactory(Architecture architecture) {
		RedisConfiguration configuration = null;
		if (architecture == Architecture.SENTINEL)
			configuration = redisSentinelConfiguration();
		else if (architecture == Architecture.STANDALONE)
			configuration = redisStandaloneConfiguration();
		return configuration;
	}

	@Profile("lettuce")
	@Bean(name = "redisTemplate")
	public RedisTemplate<String, String> lettuceTemplate() {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(lettuceConnectionFactory());
		return template;
	}
	
	private RedisStandaloneConfiguration redisStandaloneConfiguration() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
		redisStandaloneConfiguration.setDatabase(redisDatabase);
		if(StringUtils.isNotBlank(redisPassword))
			redisStandaloneConfiguration.setPassword(redisPassword);
		return redisStandaloneConfiguration;
	}

	private RedisSentinelConfiguration redisSentinelConfiguration() {
		RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(redisMaster, new HashSet<>(Arrays.asList(redisSentinels.split(","))));
		redisSentinelConfiguration.setDatabase(redisDatabase);
		if(StringUtils.isNotBlank(redisPassword))
			redisSentinelConfiguration.setPassword(redisPassword);
		return redisSentinelConfiguration;
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
		return getJedisConnectionFactory(clientConfiguration);
	}

	private JedisConnectionFactory getJedisConnectionFactory(JedisClientConfiguration clientConfiguration) {
		JedisConnectionFactory connectionFactory = null;
		Architecture architecture = getArchitecture();
		if(architecture == Architecture.STANDALONE)
			connectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration(), clientConfiguration);
		else if (architecture == Architecture.SENTINEL) {
			connectionFactory = new JedisConnectionFactory(redisSentinelConfiguration(), clientConfiguration);
		}
		return connectionFactory;
	}

	@Profile("jedis")
	@Bean(name = "redisTemplate")
	public RedisTemplate<String, String> jedisTemplate() {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	@Profile("lettuce")
	@Bean
	public CacheManager lettuceCacheManager() {
		RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder
				.fromConnectionFactory(lettuceConnectionFactory());
		return builder.build();
	}

	@Profile("jedis")
	@Bean
	public CacheManager cacheManager() {
		RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder
				.fromConnectionFactory(jedisConnectionFactory());
		return builder.build();
	}

	private Architecture getArchitecture() {
		try {
			return Architecture.valueOf(redisArchitecture.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Missing or Invalid redis architecture config: current redis.architecture is '%s'", redisArchitecture));
		}
	}

}
