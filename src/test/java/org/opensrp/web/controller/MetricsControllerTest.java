package org.opensrp.web.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.micrometer.prometheus.PrometheusMeterRegistry;

public class MetricsControllerTest {

    private final String baseEndpoint = "/metrics";
    @InjectMocks
    private MetricsController metricsController;
    @Mock
    private PrometheusMeterRegistry registry;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(metricsController)
                .addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
        ReflectionTestUtils.setField(metricsController, "registry", registry);
    }

    @Test
    public void testIndexShouldReturnOk() throws Exception {
        String sampleOutput = "# HELP health_check_redis  \n"
                + "# TYPE health_check_redis gauge\n"
                + "health_check_redis 1.0\n"
                + "# HELP postgres_connections Number of active connections to the given db\n"
                + "# TYPE postgres_connections gauge\n"
                + "postgres_connections{database=\"opensrp\",} 4.0\n";
        doReturn(sampleOutput).when(registry).scrape();
        MvcResult result = mockMvc.perform(get(baseEndpoint))
                .andExpect(status().isOk()).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }
}
