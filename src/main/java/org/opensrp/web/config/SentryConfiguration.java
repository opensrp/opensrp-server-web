package org.opensrp.web.config;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.common.annotations.VisibleForTesting;

import io.sentry.Sentry;

@Configuration
public class SentryConfiguration {
	
	@Value("#{opensrp['sentry.dsn'] ?: ''}")
	private String dsn;
	
	@Value("#{opensrp['sentry.release'] ?: ''}")
	private String release;
	
	@PostConstruct
	public void initialize() {
		if (StringUtils.isNotBlank(dsn)) {
			initializeSentry();
		}
	}
	
	@VisibleForTesting
	protected void initializeSentry() {
		Sentry.init(sentryOptions -> {
			sentryOptions.setDsn(dsn);
			sentryOptions.setRelease(release);
		});
	}
}
