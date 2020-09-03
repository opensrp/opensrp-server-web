package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.service.ProductCatalogueService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class ProductCatalogueResourceTest {

	@Mock
	private ProductCatalogueService productCatalogueService;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@InjectMocks
	private ProductCatalogueResource productCatalogueResource;

	private MockMvc mockMvc;

	protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

	private String BASE_URL = "/rest/product-catalogue";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(productCatalogueResource)
				.setControllerAdvice(new GlobalExceptionHandler()).
						addFilter(new CrossSiteScriptingPreventionFilter(), "/*").
						build();
	}


}
