package org.opensrp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@Configuration
public class TestDatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = "jdbc:postgresql://localhost:5432/opensrp";
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            doReturn(connection).when(dataSource).getConnection();
            doReturn(databaseMetaData).when(connection).getMetaData();
            doReturn(databaseUrl).when(databaseMetaData).getURL();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }
}
