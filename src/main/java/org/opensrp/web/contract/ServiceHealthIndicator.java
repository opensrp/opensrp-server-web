package org.opensrp.web.contract;

import org.springframework.ui.ModelMap;

import java.util.concurrent.Callable;

public interface ServiceHealthIndicator {
    Callable<ModelMap> doHealthCheck();
    String getHealthIndicatorKey();
}
