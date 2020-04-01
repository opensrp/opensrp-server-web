package org.opensrp.web.config;

import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
@EnableWebMvc
public class SwaggerConfig extends WebMvcConfigurerAdapter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic";
    private static final String HEADER = "header";

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("(/rest/.*)|(/multimedia/.*)|(/security/.*)|(/user-details)"))
                .build()
                .apiInfo(getApiInfo())
                .securityContexts(Lists.newArrayList(securityContext()))
                .securitySchemes(Lists.newArrayList(new ApiKey(BASIC, AUTHORIZATION, HEADER)));
    }

    public ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("OPENSRP API")
                .description("Open Smart Register Platform (OpenSRP)")
                .version("VERSION 1.0")
                .license("OPENSRP LICENSE")
                .licenseUrl("https://github.com/OpenSRP/opensrp-server-web/blob/master/LICENSE")
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(
                        new SecurityReference(AUTHORIZATION, new AuthorizationScope[0])))
                .forPaths(PathSelectors.regex("/rest/.*"))
                .build();
    }
}
