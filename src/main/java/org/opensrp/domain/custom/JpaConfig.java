package org.opensrp.domain.custom;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"org.opensrp.web.custom"}, entityManagerFactoryRef = "jpaEntityManagerFactory")
public class JpaConfig {

}
