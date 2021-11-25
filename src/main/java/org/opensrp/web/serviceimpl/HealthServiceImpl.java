package org.opensrp.web.serviceimpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.Constants;
import org.opensrp.web.contract.ServiceHealthIndicator;
import org.opensrp.web.health.JdbcServiceHealthIndicator;
import org.opensrp.web.health.OpenmrsServiceHealthIndicator;
import org.opensrp.web.health.RabbitmqServiceHealthIndicator;
import org.opensrp.web.health.RedisServiceHealthIndicator;
import org.opensrp.web.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service("HealthServiceImpl")
public class HealthServiceImpl implements HealthService {

	private static final Logger logger = LogManager.getLogger(HealthServiceImpl.class.toString());

	@Autowired
	private JdbcServiceHealthIndicator jdbcServiceHealthIndicator;

	@Autowired(required = false)
	private RabbitmqServiceHealthIndicator rabbitmqServiceHealthIndicator;

	@Autowired
	private RedisServiceHealthIndicator redisServiceHealthIndicator;

	@Autowired
	private OpenmrsServiceHealthIndicator openmrsServiceHealthIndicator;

	private final String buildVersion = this.getClass().getPackage().getImplementationVersion();

	private List<ServiceHealthIndicator> healthIndicators;

	@Override
	public List<ServiceHealthIndicator> getHealthIndicators() {
		if (healthIndicators == null) {
			healthIndicators = new ArrayList<>();
			healthIndicators.add(jdbcServiceHealthIndicator);
			healthIndicators.add(redisServiceHealthIndicator);
			healthIndicators.add(openmrsServiceHealthIndicator);
			if (rabbitmqServiceHealthIndicator != null)
				healthIndicators.add(rabbitmqServiceHealthIndicator);
		}
		return healthIndicators;
    }

	@Override
	public ModelMap aggregateHealthCheck() {
		ModelMap servicesObjectMap = new ModelMap();
		ModelMap problemsObjectMap = new ModelMap();
		ModelMap resultPayload = new ModelMap();
		List<Callable<ModelMap>> callableList = new ArrayList<>();
		for (ServiceHealthIndicator serviceHealthIndicator : getHealthIndicators()) {
			callableList.add(serviceHealthIndicator.doHealthCheck());
		}

		try {
			List<Future<ModelMap>> futureList = Executors.newFixedThreadPool(callableList.size()).invokeAll(callableList);
			for (Future<ModelMap> resultFuture : futureList) {
				ModelMap modelMap = resultFuture.get();
				Boolean status = (Boolean) modelMap.get(Constants.HealthIndicator.STATUS);
				String indicator = (String) modelMap.get(Constants.HealthIndicator.INDICATOR);
				if (!status)
					problemsObjectMap.put(indicator, modelMap.get(Constants.HealthIndicator.EXCEPTION));
				servicesObjectMap.put(indicator, status);
			}
			resultPayload.put(Constants.HealthIndicator.PROBLEMS, problemsObjectMap);
			resultPayload.put(Constants.HealthIndicator.SERVICES, servicesObjectMap);
			resultPayload.put(Constants.HealthIndicator.TIME,
					new SimpleDateFormat(Constants.HealthIndicator.DATE_TIME_FORMAT).format(new Date()));
			if (StringUtils.isNotBlank(buildVersion))
				resultPayload.put(Constants.HealthIndicator.VERSION, buildVersion);

		}
		catch (InterruptedException | ExecutionException e) {
			logger.error(e);
		}
		return resultPayload;
	}
}
