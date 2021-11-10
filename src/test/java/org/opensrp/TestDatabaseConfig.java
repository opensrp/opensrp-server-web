package org.opensrp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

@Configuration
public class TestDatabaseConfig {

    @Bean
    public DataSource dataSource() {
        return mock(DataSource.class);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }
}
