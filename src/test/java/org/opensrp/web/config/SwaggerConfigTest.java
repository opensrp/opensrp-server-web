package org.opensrp.web.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SwaggerConfigTest {
    private ApiInfo apiInfo;
    private SwaggerConfig swaggerConfig;

    @Before
    public void setUp() {
        apiInfo = mock(ApiInfo.class);
        swaggerConfig = mock(SwaggerConfig.class);
    }

    @Test
    public void testApi() {
        assertNull(swaggerConfig.api());
        when(swaggerConfig.api()).thenReturn(createTestDocket());
        when(swaggerConfig.getApiInfo()).thenReturn(createTestApiInfo());
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

    private Docket createTestDocket() {
        return new Docket(DocumentationType.SWAGGER_2.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .build()
                .apiInfo(createTestApiInfo());
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

}
