package org.opensrp.web.health;

import org.springframework.ui.ModelMap;

import java.util.concurrent.Callable;

public interface ServiceHealthIndicator {
    Callable<ModelMap> doHealthCheck();
}
