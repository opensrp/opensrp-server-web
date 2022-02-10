package org.opensrp.web.config;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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

import javax.servlet.ServletContext;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class SwaggerConfigTest {
    private SwaggerConfig swaggerConfig;

    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC = "Basic";
    private static final String BEARER = "Bearer";
    private static final String HEADER = "header";

    @Mock
    private ServletContext servletContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn("/opensrp").when(servletContext).getContextPath();
        swaggerConfig = spy(new SwaggerConfig());
        WhiteboxImpl.setInternalState(swaggerConfig, "servletContext", servletContext);
    }

    @Test
    public void testApi() {
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
        assertNotNull(swaggerConfig.getApiInfo());
    }

    @Test
    public void testSecurityContext() {
        assertNotNull(swaggerConfig.securityContext());
    }

    @Test
    public void testAddResourceHandlersShouldInvokeRegistryResourceHandler() {
        String pathPattern = "/swagger-ui/**";
        String resourceLocation = "classpath:/META-INF/resources/webjars/springfox-swagger-ui/";
        ResourceHandlerRegistry mockRegistry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration mockHandlerRegistration = mock(ResourceHandlerRegistration.class);
        doReturn(mockHandlerRegistration).when(mockRegistry).addResourceHandler(eq(pathPattern));

        swaggerConfig.addResourceHandlers(mockRegistry);

        verify(mockRegistry).addResourceHandler(eq(pathPattern));
        verify(mockHandlerRegistration).addResourceLocations(eq(resourceLocation));
    }

    @Test
    public void testAddViewControllersShouldInvokeViewControllerAddMethod() {
        String path = "/swagger-ui/";
        String viewName = "forward:/swagger-ui/index.html";
        ViewControllerRegistry mockViewControllerRegistry = mock(ViewControllerRegistry.class);
        ViewControllerRegistration mockViewControllerRegistration = mock(ViewControllerRegistration.class);
        doReturn(mockViewControllerRegistration).when(mockViewControllerRegistry).addViewController(eq(path));

        swaggerConfig.addViewControllers(mockViewControllerRegistry);

        verify(mockViewControllerRegistry).addViewController(eq(path));
        verify(mockViewControllerRegistration).setViewName(eq(viewName));
    }

    private Docket createTestDocket() {
        return new Docket(DocumentationType.SWAGGER_2.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .build()
                .apiInfo(createTestApiInfo())
                .securityContexts(Lists.newArrayList(createTestSecurityContext()))
                .securitySchemes(Lists.newArrayList(new ApiKey(BASIC, AUTHORIZATION, HEADER), new ApiKey(BEARER, AUTHORIZATION, HEADER)));
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
                .securityReferences(getDefaultAuth())
                .forPaths(PathSelectors.regex("/rest/.*"))
                .build();
    }

	private List<SecurityReference> getDefaultAuth() {
		AuthorizationScope authorizationScope
				= new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Lists.newArrayList(
				new SecurityReference("Basic", authorizationScopes), new SecurityReference("Bearer", authorizationScopes));
	}
}
