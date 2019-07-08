package org.opensrp.web.config;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.models.dto.builder.ApiInfoBuilder;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSwagger //Loads the spring beans required by the framework
public class MySwaggerConfig {

    private SpringSwaggerConfig springSwaggerConfig;

    /**
     * Required to autowire SpringSwaggerConfig
     */
    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    /**
     * Every SwaggerSpringMvcPlugin bean is picked up by the swagger-mvc framework - allowing for multiple
     * swagger groups i.e. same code base multiple swagger resource listings.
     */
    @Bean
    public SwaggerSpringMvcPlugin configureSwagger(){
        //return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
        // .includePatterns(".*");

        SwaggerSpringMvcPlugin swaggerSpringMvcPlugin = new SwaggerSpringMvcPlugin(this.springSwaggerConfig);

        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("Opensrp rest api")
                .description("Smart eregister")
                .termsOfServiceUrl("Url")
                .contact("Me")
                .build();
        swaggerSpringMvcPlugin.apiInfo(apiInfo)
                .apiVersion("1.0")
                .includePatterns(".*rest.*"); // assuming the API lives at something like http://myapp/rest
        return swaggerSpringMvcPlugin;
    }

}
