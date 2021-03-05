package org.opensrp.web.config;

import io.sentry.Sentry;
import io.sentry.spring.EnableSentry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import javax.annotation.PostConstruct;

@EnableSentry
@Configuration
public class SentryConfiguration {

	@Value("${sentry.dsn}")
	private String dsn;

	@Value("${sentry.release}")
	private String release;

	@PostConstruct
	public void initialize(){
		if(StringUtils.isNotBlank(dsn)) {
			Sentry.init(sentryOptions -> {
				sentryOptions.setDsn(dsn);
				sentryOptions.setRelease(release);
			});
		}
	}
}
