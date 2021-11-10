package org.opensrp.web.health;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensrp.web.Constants;
import org.opensrp.web.contract.ServiceHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import javax.sql.DataSource;

import java.util.concurrent.Callable;

@Profile("postgres")
@Component
public class JdbcServiceHealthIndicator implements ServiceHealthIndicator {

	private static final Logger logger = LogManager.getLogger(JdbcServiceHealthIndicator.class.toString());

	@Value("#{opensrp['health.endpoint.postgres.queryTimeout'] ?: 2000 }")
	private Integer queryTimeout;

	@Autowired
	private DataSource dataSource;

	private final String HEALTH_INDICATOR_KEY = "postgres";

	@Override
	public Callable<ModelMap> doHealthCheck() {
		return () -> {
			ModelMap modelMap = new ModelMap();
			boolean result = false;
			try {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				jdbcTemplate.setQueryTimeout(queryTimeout);
				result = jdbcTemplate.queryForObject("SELECT 1;", Integer.class) == 1;
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
