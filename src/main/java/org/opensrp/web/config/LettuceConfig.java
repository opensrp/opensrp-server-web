/**
 * 
 */
package org.opensrp.web.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author Samuel Githengi created on 05/11/20
 */
@Configuration
@Profile("lettuce")
public class LettuceConfig {
	
	@Value("#{opensrp['redis.host']}")
	private String redisHost;
	
	@Value("#{opensrp['redis.port']}")
	private int redisPort;
	
	@Value("#{opensrp['redis.password']}")
	private String redisPassword;
	
	private int redisDatabase = 0;
	
	@Value("#{opensrp['redis.pool.max.connections']}")
	private int redisMaxConnections = 0;
	
	@Bean(name = "lettuceConnectionFactory")
	public RedisConnectionFactory connectionFactory() {
		
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
		redisStandaloneConfiguration.setDatabase(redisDatabase);
		redisStandaloneConfiguration.setPassword(redisPassword);
		
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		poolConfig.setMaxTotal(redisMaxConnections);
		LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
		        .poolConfig(poolConfig).build();
		LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration,
		        lettuceClientConfiguration);
		return redisConnectionFactory;
	}
	
	@Bean(name = "redisTemplate")
	public RedisTemplate<String, String> redisTemplate() {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory());
		return template;
	}
	
}
