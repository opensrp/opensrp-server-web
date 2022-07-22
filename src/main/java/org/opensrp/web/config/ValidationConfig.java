package org.opensrp.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
public class ValidationConfig {

    @Bean
    public MethodValidationPostProcessor getMethodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

}
