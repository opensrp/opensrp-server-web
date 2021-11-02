package org.opensrp.web.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.Constants;
import org.opensrp.web.health.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Service
public class HealthService {

    private static final Logger logger = LogManager.getLogger(HealthService.class.toString());

    @Autowired
    private JdbcServiceHealthIndicator jdbcServiceHealthIndicator;

    @Autowired(required = false)
    private RabbitmqServiceHealthIndicator rabbitmqServiceHealthIndicator;

    @Autowired
    private RedisServiceHealthIndicator redisServiceHealthIndicator;

    @Autowired(required = false)
    private KeycloakServiceHealthIndicator keycloakServiceHealthIndicator;

    @Value("#{opensrp['sentry.release'] ?: ''}")
    private String serverVersion;

    private List<ServiceHealthIndicator> getHealthIndicators() {
        List<ServiceHealthIndicator> healthIndicators = new ArrayList<>();
        healthIndicators.add(jdbcServiceHealthIndicator);
        healthIndicators.add(redisServiceHealthIndicator);
        if (rabbitmqServiceHealthIndicator != null)
            healthIndicators.add(rabbitmqServiceHealthIndicator);
        if (keycloakServiceHealthIndicator != null)
            healthIndicators.add(keycloakServiceHealthIndicator);
        return healthIndicators;
    }

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
            resultPayload.put(Constants.HealthIndicator.TIME, new SimpleDateFormat(Constants.HealthIndicator.DATE_TIME_FORMAT).format(new Date()));
            if (StringUtils.isNotBlank(serverVersion))
                resultPayload.put(Constants.HealthIndicator.VERSION, serverVersion);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e);
        }
        return resultPayload;
    }
}
