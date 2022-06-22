package org.opensrp.web.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.web.Constants;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.service.HealthService;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;

import io.micrometer.core.instrument.MeterRegistry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class HealthCheckMetricUpdaterTest {

    @Autowired
    private HealthCheckMetricUpdater healthCheckMetricUpdater;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    public void testInitialization() {
        assertFalse(meterRegistry.getMeters().isEmpty());
        assertNotNull(meterRegistry.find("health_check_postgres").gauge());
        assertNull(meterRegistry.find("health_check_mysql").gauge());
    }

    @Test
    public void testUpdateHealthStatusMetrics() {
        HealthService healthService = mock(HealthService.class);
        WhiteboxImpl.setInternalState(healthCheckMetricUpdater, "healthService", healthService);
        ModelMap modelMap = new ModelMap();
        ModelMap serviceModelMap = new ModelMap();
        serviceModelMap.put("postgres", true);
        modelMap.put(Constants.HealthIndicator.SERVICES, serviceModelMap);
        doReturn(modelMap).when(healthService).aggregateHealthCheck();
        healthCheckMetricUpdater.updateHealthStatusMetrics();
        assertEquals(1.0, meterRegistry.find("health_check_postgres").gauge().value(), 0.0);
    }
}
