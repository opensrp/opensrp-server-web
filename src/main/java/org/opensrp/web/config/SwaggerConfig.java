package org.opensrp.web.config;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

import javax.servlet.ServletContext;

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

@Configuration
@EnableSwagger2
@EnableWebMvc
public class SwaggerConfig extends WebMvcConfigurerAdapter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic";
    private static final String BEARER = "Bearer";
    private static final String HEADER = "header";

    @Autowired
    private ServletContext servletContext;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex(
                        StringUtils.replace("(basePath/rest/.*)|(basePath/multimedia/.*)|(basePath/security/.*)|(basePath/user-details)",
                                "basePath",
                                servletContext.getContextPath()
                        ))) // To be revisited on v3.0.1
                .build()
                .apiInfo(getApiInfo())
                .securityContexts(Lists.newArrayList(securityContext()))
                .securitySchemes(Lists.newArrayList(new ApiKey(BASIC, AUTHORIZATION, HEADER), new ApiKey(BEARER, AUTHORIZATION, HEADER)));
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
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/swagger-ui/")
                .setViewName("forward:/swagger-ui/index.html");
    }

    public SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/rest/.*"))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Lists.newArrayList(
                new SecurityReference("Basic", authorizationScopes), new SecurityReference("Bearer", authorizationScopes));
    }
}
