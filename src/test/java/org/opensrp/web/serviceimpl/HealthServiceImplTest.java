package org.opensrp.web.serviceimpl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.web.Constants;
import org.opensrp.web.health.JdbcServiceHealthIndicator;
import org.opensrp.web.health.KeycloakServiceHealthIndicator;
import org.opensrp.web.health.RabbitmqServiceHealthIndicator;
import org.opensrp.web.health.RedisServiceHealthIndicator;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.ModelMap;

import java.util.concurrent.Callable;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
@ActiveProfiles(profiles = {"jedis", "postgres", "oauth2", "rabbitmq"})
public class HealthServiceImplTest {

    @Autowired
    private HealthServiceImpl healthService;

    @Mock
    private JdbcServiceHealthIndicator jdbcServiceHealthIndicator;

    @Mock
    private RabbitmqServiceHealthIndicator rabbitmqServiceHealthIndicator;

    @Mock
    private RedisServiceHealthIndicator redisServiceHealthIndicator;

    @Mock
    private KeycloakServiceHealthIndicator keycloakServiceHealthIndicator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAggregateHealthCheck() {
        Callable<ModelMap> mapCallable = new Callable<ModelMap>() {

            @Override
            public ModelMap call() throws Exception {
                ModelMap modelMap = new ModelMap();
                modelMap.put(Constants.HealthIndicator.STATUS, false);
                modelMap.put(Constants.HealthIndicator.INDICATOR, "serviceA");
                return modelMap;
            }
        };
        doReturn(mapCallable).when(jdbcServiceHealthIndicator).doHealthCheck();
        doReturn(mapCallable).when(rabbitmqServiceHealthIndicator).doHealthCheck();
        doReturn(mapCallable).when(redisServiceHealthIndicator).doHealthCheck();
        doReturn(mapCallable).when(keycloakServiceHealthIndicator).doHealthCheck();
        HealthServiceImpl spyHealthService = spy(healthService);
        Whitebox.setInternalState(spyHealthService, "buildVersion", "3.2");
        ModelMap modelMap = spyHealthService.aggregateHealthCheck();
        assertNotNull(modelMap);
        assertTrue(modelMap.containsKey(Constants.HealthIndicator.PROBLEMS));
        assertTrue(modelMap.containsKey(Constants.HealthIndicator.SERVICES));
        assertTrue(modelMap.containsKey(Constants.HealthIndicator.TIME));
    }
}
