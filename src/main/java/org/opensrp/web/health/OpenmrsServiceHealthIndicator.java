package org.opensrp.web.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.Constants;
import org.opensrp.web.contract.ServiceHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Callable;

@Component
public class OpenmrsServiceHealthIndicator implements ServiceHealthIndicator {

	private static final Logger logger = LogManager.getLogger(OpenmrsServiceHealthIndicator.class.toString());

	@Value("#{opensrp['openmrs.url'] ?: ''}")
	private String openmrsUrl;

	@Value("#{opensrp['health.endpoint.openmrs.connectionTimeout'] ?: 5000 }")
	private Integer openmrsConnectionTimeout;

	@Value("#{opensrp['health.endpoint.openmrs.readTimeout'] ?: 5000 }")
	private Integer openmrsReadTimeout;

	private final String HEALTH_INDICATOR_KEY = "openmrs";

	@Override
	public Callable<ModelMap> doHealthCheck() {
		return () -> {
			ModelMap modelMap = new ModelMap();
			boolean result = false;
			try {
				SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
				simpleClientHttpRequestFactory.setConnectTimeout(openmrsConnectionTimeout);
				simpleClientHttpRequestFactory.setReadTimeout(openmrsReadTimeout);

				RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
				ResponseEntity<String> responseEntity = restTemplate.getForEntity(openmrsUrl,
						String.class);
				result = responseEntity.getStatusCode() == HttpStatus.OK;
			}
			catch (Exception e) {
				logger.error(e);
				modelMap.put(Constants.HealthIndicator.EXCEPTION, e.getMessage());
			}
			modelMap.put(Constants.HealthIndicator.STATUS, result);
			modelMap.put(Constants.HealthIndicator.INDICATOR, HEALTH_INDICATOR_KEY);
			return modelMap;
		};
	}
}
