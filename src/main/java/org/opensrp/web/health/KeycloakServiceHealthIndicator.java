package org.opensrp.web.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.keycloak.adapters.KeycloakDeployment;
import org.opensrp.web.Constants;
import org.opensrp.web.contract.ServiceHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Callable;

@Profile("oauth2")
@Component
public class KeycloakServiceHealthIndicator implements ServiceHealthIndicator {

	private static final Logger logger = LogManager.getLogger(KeycloakServiceHealthIndicator.class.toString());

	@Autowired
	private KeycloakDeployment keycloakDeployment;

	private final String HEALTH_INDICATOR_KEY = "keycloak";

	@Override
	public Callable<ModelMap> doHealthCheck() {
		return () -> {
			ModelMap modelMap = new ModelMap();
			boolean result = false;
			try {
				RestTemplate restTemplate = new RestTemplate();
				ResponseEntity<String> responseEntity = restTemplate.getForEntity(keycloakDeployment.getRealmInfoUrl(),
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
