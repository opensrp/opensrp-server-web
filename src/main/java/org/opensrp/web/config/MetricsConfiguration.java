package org.opensrp.web.config;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.db.PostgreSQLDatabaseMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.Log4j2Metrics;
import io.micrometer.core.instrument.binder.system.DiskSpaceMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class MetricsConfiguration {

    private static final Logger logger = LogManager.getLogger(MetricsConfiguration.class.toString());
    private final String buildVersion = this.getClass().getPackage().getImplementationVersion();
    @Value("#{opensrp['metrics.tags'] ?: '{}' }")
    private String metricsTags;
    @Value("#{opensrp['sentry.tags'] ?: '{}' }")
    private String sentryTags;
    @Autowired
    private DataSource dataSource;
    @Value("#{opensrp['metrics.include'] ?: 'all' }")
    private Set<String> includedMetrics;
    @Value("#{opensrp['metrics.exclude'] ?: '' }")
    private Set<String> excludedMetrics;

    @PostConstruct
    private void init() {
        MeterRegistry registry = prometheusMeterRegistry();
        includeLog4j2Metrics(registry);
        includeThreadMetrics(registry);
        includeGCMetrics(registry);
        includeMemoryMetrics(registry);
        includeDiskSpaceMetrics(registry);
        includeProcessorMetrics(registry);
        includeUptimeMetrics(registry);
        includeDatabaseMetrics(registry);
    }

    private void includeDatabaseMetrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.DATABASE) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.DATABASE))) {
            String databaseConnectionUrl;
            try {
                databaseConnectionUrl = dataSource.getConnection().getMetaData().getURL();
                if (StringUtils.isNotBlank(databaseConnectionUrl)) {
                    String databaseName = databaseConnectionUrl.substring(databaseConnectionUrl.lastIndexOf("/") + 1);
                    new PostgreSQLDatabaseMetrics(dataSource, databaseName).bindTo(registry);
                }
            } catch (SQLException e) {
                logger.error(e);
            }
        }
    }

    private void includeUptimeMetrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.UPTIME) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.UPTIME)))
            new UptimeMetrics().bindTo(registry);
    }

    private void includeProcessorMetrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.PROCESSOR) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.PROCESSOR)))
            new ProcessorMetrics().bindTo(registry);
    }

    private void includeDiskSpaceMetrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.DISK_SPACE) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.DISK_SPACE)))
            new DiskSpaceMetrics(new File("/")).bindTo(registry);
    }

    private void includeMemoryMetrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.JVM_MEMORY) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.JVM_MEMORY)))
            new JvmMemoryMetrics().bindTo(registry);
    }

    private void includeGCMetrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.JVM_GC) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.JVM_GC)))
            new JvmGcMetrics().bindTo(registry);
    }

    private void includeThreadMetrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.JVM_THREAD) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.JVM_THREAD)))
            new JvmThreadMetrics().bindTo(registry);
    }

    private void includeLog4j2Metrics(MeterRegistry registry) {
        if (!excludedMetrics.contains(Constants.Metrics.LOG4J2) && (includedMetrics.contains(Constants.Metrics.ALL)
                || includedMetrics.contains(Constants.Metrics.LOG4J2)))
            new Log4j2Metrics().bindTo(registry);
    }

    @Bean(name = "prometheusMeterRegistry")
    public MeterRegistry prometheusMeterRegistry() {
        MeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        List<Tag> tagList = Stream.of(buildMetricsTags(metricsTags, ""), buildMetricsTags(sentryTags, "sentry")).flatMap(
                Collection::stream).collect(
                Collectors.toList());
        tagList.add(Tag.of(Constants.HealthIndicator.VERSION, StringUtils.isBlank(buildVersion) ? "" : buildVersion));
        meterRegistry.config()
                .commonTags(tagList);
        return meterRegistry;
    }

    @VisibleForTesting
    protected List<Tag> buildMetricsTags(String tags, String prefix) {
        List<Tag> tagList = new ArrayList<>();
        if (StringUtils.isNotBlank(tags) && tags.length() > 2) {
            Map<String, String> map;
            try {
                map = new Gson().fromJson(tags, Map.class);
                for (Map.Entry<String, String> extraTagsEntry : map.entrySet()) {
                    String key = extraTagsEntry.getKey();
                    if (StringUtils.isNotBlank(key)) {
                        String tagKey;
                        if (StringUtils.isNotBlank(prefix))
                            tagKey = String.format("%s_%s", prefix, extraTagsEntry.getKey());
                        else
                            tagKey = extraTagsEntry.getKey();
                        tagList.add(
                                Tag.of(tagKey, extraTagsEntry.getValue()));
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
        return tagList;
    }
}
