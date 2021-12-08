package org.opensrp.web.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.Constants;
import org.opensrp.web.contract.ServiceHealthIndicator;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.concurrent.Callable;

@Profile("rabbitmq")
@Component
public class RabbitmqServiceHealthIndicator implements ServiceHealthIndicator {

	private static final Logger logger = LogManager.getLogger(RabbitmqServiceHealthIndicator.class.toString());

	@Autowired
	private AmqpAdmin amqpAdmin;

	private final String HEALTH_INDICATOR_KEY = "rabbitmq";

	@Value("#{opensrp['rabbitmq.queue'] ?: ''}")
	private String queueName;

	@Override
	public Callable<ModelMap> doHealthCheck() {
		return () -> {
			ModelMap modelMap = new ModelMap();
			boolean result = false;
			try {
				amqpAdmin.getQueueInfo(queueName);
				result = true;
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

	@Override
	public String getHealthIndicatorKey() {
		return HEALTH_INDICATOR_KEY;
	}
}
