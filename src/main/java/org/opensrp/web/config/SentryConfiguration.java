package org.opensrp.web.config;

import javax.annotation.PostConstruct;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import io.sentry.SentryOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import io.sentry.Sentry;

import java.util.Map;

@Configuration
public class SentryConfiguration {

	@Value("#{opensrp['sentry.dsn'] ?: ''}")
	private String dsn;

	@Value("#{opensrp['sentry.release'] ?: ''}")
	private String release;

	@Value("#{opensrp['sentry.environment'] ?: ''}")
	private String environment;

	@Value("#{opensrp['sentry.tags'] ?: {} }")
	private String tags;

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
			sentryOptions.setEnvironment(environment);
			populateTags(sentryOptions);
		});
	}
	@VisibleForTesting
	protected void populateTags(SentryOptions sentryOptions) {
		if(StringUtils.isNotBlank(tags)) {
			Map<String, String> map;
			try {
				map = new Gson().fromJson(tags, Map.class);
				for (Map.Entry<String, String> extraTagsEntry : map.entrySet()) {
					String key = extraTagsEntry.getKey();
					if (StringUtils.isNotBlank(key))
						sentryOptions.setTag(extraTagsEntry.getKey(), extraTagsEntry.getValue());
				}
			}
			catch (Exception e) {
				LogManager.getLogger().error(e);
			}
		}
	}
}
