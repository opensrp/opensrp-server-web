package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.opensrp.domain.ProductCatalogue;
import org.opensrp.search.ProductCatalogueSearchBean;
import org.opensrp.service.ProductCatalogueService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class ProductCatalogueResourceTest {

	@Mock
	private ProductCatalogueService productCatalogueService;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@InjectMocks
	private ProductCatalogueResource productCatalogueResource;

	@Captor
	private ArgumentCaptor<ProductCatalogue> argumentCaptor;

	private MockMvc mockMvc;

	protected ObjectMapper mapper = new ObjectMapper().enableDefaultTyping();

	private Gson gson = new Gson().newBuilder().create();

	private String BASE_URL = "/rest/product-catalogue";

	private String productCatalogJson = "{\n"
			+ "    \"productName\": \"Midwifery Kit\",\n"
			+ "    \"isAttractiveItem\": true,\n"
			+ "    \"materialNumber\":\"AX-123\",\n"
			+ "    \"availability\": \"available\",\n"
			+ "    \"condition\": \"yes\",\n"
			+ "    \"appropriateUsage\": \"yes\",\n"
			+ "    \"accountabilityPeriod\": 10,\n"
			+ "    \"serverVersion\": 123344\n"
			+ "}";

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(productCatalogueResource)
				.setControllerAdvice(new GlobalExceptionHandler()).
						addFilter(new CrossSiteScriptingPreventionFilter(), "/*").
						build();
	}

	@Test
	public void testGetAll() throws Exception {
		ProductCatalogue productCatalogue = createProductCatalog();
		productCatalogue.setUniqueId(1l);
		List<ProductCatalogue> productCatalogues = new ArrayList<>();
		productCatalogues.add(productCatalogue);
		when(productCatalogueService.getProductCatalogues(any(ProductCatalogueSearchBean.class)))
				.thenReturn(productCatalogues);
		MvcResult result = mockMvc.perform(get(BASE_URL))
				.andExpect(status().isOk())
				.andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(actualObj.size(), 1);
		assertEquals(1, actualObj.get(0).get("uniqueId").asLong());
		assertEquals("Scale", actualObj.get(0).get("productName").asText());
		assertEquals("MT-123", actualObj.get(0).get("materialNumber").asText());
	}

	private ProductCatalogue createProductCatalog() {
		ProductCatalogue productCatalogue = new ProductCatalogue();
		productCatalogue.setProductName("Scale");
		productCatalogue.setIsAttractiveItem(Boolean.TRUE);
		productCatalogue.setMaterialNumber("MT-123");
		productCatalogue.setAvailability("available");
		productCatalogue.setCondition("good condition");
		productCatalogue.setAppropriateUsage("staff is trained to use it appropriately");
		productCatalogue.setAccountabilityPeriod(1);
		productCatalogue.setServerVersion(123456l);
		return productCatalogue;
	}

}
