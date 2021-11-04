package org.opensrp.web.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.Constants;
import org.opensrp.web.contract.ServiceHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.concurrent.Callable;

@Profile("jedis")
@Component
public class RedisServiceHealthIndicator implements ServiceHealthIndicator {

	private static final Logger logger = LogManager.getLogger(RedisServiceHealthIndicator.class.toString());

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	private final String HEALTH_INDICATOR_KEY = "redis";

	@Override
	public Callable<ModelMap> doHealthCheck() {
		return () -> {
			ModelMap modelMap = new ModelMap();
			boolean result = false;
			try {
				String pingResult = redisConnectionFactory.getConnection().ping();
				result = "PONG".equalsIgnoreCase(pingResult);
			}
			catch (Exception e) {
				logger.error(e);
				modelMap.put(Constants.HealthIndicator.EXCEPTION, e.getMessage());
			}
			modelMap.put(Constants.HealthIndicator.STATUS, result);
			modelMap.put(Constants.HealthIndicator.INDICATOR, HEALTH_INDICATOR_KEY);
			return modelMap;
		};
	}
}
