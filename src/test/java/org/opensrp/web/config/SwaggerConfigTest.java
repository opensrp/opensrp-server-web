package org.opensrp.web.config;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
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

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SwaggerConfigTest {
    private SwaggerConfig swaggerConfig;

    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic";
    private static final String HEADER = "header";

    @Before
    public void setUp() {
        swaggerConfig = mock(SwaggerConfig.class);
    }

    @Test
    public void testApi() {
        assertNull(swaggerConfig.api());
        when(swaggerConfig.api()).thenReturn(createTestDocket());
        when(swaggerConfig.getApiInfo()).thenReturn(createTestApiInfo());
        when(swaggerConfig.securityContext()).thenReturn(createTestSecurityContext());
        Docket docket = swaggerConfig.api();
        assertNotNull(docket);
        assertEquals(docket.getDocumentationType(), DocumentationType.SWAGGER_2.SWAGGER_2);
        assertNotNull(docket.select());
    }

    @Test
    public void testGetApiInfo() {
        assertNull(swaggerConfig.getApiInfo());
        when(swaggerConfig.getApiInfo()).thenReturn(createTestApiInfo());
        ApiInfo apiInfo = swaggerConfig.getApiInfo();
        assertNotNull(apiInfo);
        assertEquals(apiInfo.getTitle(), "Test title");
        assertEquals(apiInfo.getDescription(), "Test description");
        assertEquals(apiInfo.getVersion(), "VERSION 1.0");
        assertEquals(apiInfo.getLicense(), "Test LICENSE");
        assertEquals(apiInfo.getLicenseUrl(), "Test license url");
    }

    @Test
    public void testSecurityContext() {
        assertNull(swaggerConfig.securityContext());
        when(swaggerConfig.securityContext()).thenReturn(createTestSecurityContext());
        SecurityContext securityContext= swaggerConfig.securityContext();
        assertNotNull(securityContext);
        assertEquals(securityContext.getSecurityReferences().get(0).getReference(), AUTHORIZATION);
        assertEquals(securityContext.getSecurityReferences().get(0).getScopes().size(), 0);
    }

    private Docket createTestDocket() {
        return new Docket(DocumentationType.SWAGGER_2.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .build()
                .apiInfo(createTestApiInfo())
                .securityContexts(Lists.newArrayList(createTestSecurityContext()))
                .securitySchemes(Lists.newArrayList(new ApiKey(BASIC, AUTHORIZATION, HEADER)));
    }

    private ApiInfo createTestApiInfo() {
        return new ApiInfoBuilder()
                .title("Test title")
                .description("Test description")
                .version("VERSION 1.0")
                .license("Test LICENSE")
                .licenseUrl("Test license url")
                .build();
    }

    private SecurityContext createTestSecurityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(
                        new SecurityReference(AUTHORIZATION, new AuthorizationScope[0])))
                .forPaths(PathSelectors.regex("/rest/.*"))
                .build();
    }
}
