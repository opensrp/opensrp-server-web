package org.opensrp.web.config;

import org.opensrp.web.Constants;
import org.opensrp.web.contract.ServiceHealthIndicator;
import org.opensrp.web.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class HealthCheckMetricUpdater {

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private HealthService healthService;

    private Map<String, Double> healthCheckIndicatorMap = new HashMap<>();

    @PostConstruct
    private void init() {
        for (ServiceHealthIndicator healthIndicator : healthService.getHealthIndicators()) {
            Gauge.builder(String.format(Constants.HealthIndicator.HEALTH_CHECK_PLACEHOLDER,
                    healthIndicator.getHealthIndicatorKey()),
                    () -> healthCheckIndicatorMap.getOrDefault(healthIndicator.getHealthIndicatorKey(), 0.0))
                    .register(registry);
        }
    }

    public void updateHealthStatusMetrics() {
        ModelMap modelMap = healthService.aggregateHealthCheck();
        ModelMap serviceMap = (ModelMap) modelMap.get(Constants.HealthIndicator.SERVICES);
        for (Map.Entry<String, Object> entry : serviceMap.entrySet()) {
            String indicator = entry.getKey();
            Boolean status = (Boolean) entry.getValue();
            healthCheckIndicatorMap.put(indicator, status ? 1.0 : 0.0);
        }
    }
}
