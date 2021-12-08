package org.opensrp.web.service;

import org.opensrp.web.contract.ServiceHealthIndicator;
import org.springframework.ui.ModelMap;

import java.util.List;

public interface HealthService {

	ModelMap aggregateHealthCheck();

	List<ServiceHealthIndicator> getHealthIndicators();
}
