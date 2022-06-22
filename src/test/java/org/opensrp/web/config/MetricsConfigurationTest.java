package org.opensrp.web.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusMeterRegistry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class MetricsConfigurationTest {

    @Autowired
    private MetricsConfiguration metricsConfiguration;

    @Autowired
    private PrometheusMeterRegistry meterRegistry;

    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitialization() {
        assertFalse(meterRegistry.getMeters().isEmpty());
    }

    @Test
    public void testBuildMetricsTagsShouldReturnListOfOneTag() {
        String key = "host";
        String value = "120.1.2.3";
        List<Tag> tagList = metricsConfiguration.buildMetricsTags(String.format("{%s: %s}", key, value), "");
        assertFalse(tagList.isEmpty());
        assertEquals(1, tagList.size());
        Tag tag = tagList.get(0);
        assertEquals(key, tag.getKey());
        assertEquals(value, tag.getValue());
    }
}
