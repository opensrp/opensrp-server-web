package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.opensrp.domain.Multimedia;
import org.opensrp.domain.ProductCatalogue;
import org.opensrp.dto.form.MultimediaDTO;
import org.opensrp.search.ProductCatalogueSearchBean;
import org.opensrp.service.MultimediaService;
import org.opensrp.service.ProductCatalogueService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
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

	@Mock
	private MultimediaService multimediaService;

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

	@Test
	public void testCreate() throws Exception {

		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		ProductCatalogue productCatalogue = createProductCatalog();
		productCatalogue.setUniqueId(1l);
		Authentication authentication = mock(Authentication.class);
		authentication.setAuthenticated(Boolean.TRUE);
		SecurityContext securityContext = mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		byte[] bytes = new byte[10];

		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());

		Mockito.doNothing().when(productCatalogueService).add(any(ProductCatalogue.class));
		when(productCatalogueService.getProductCatalogueByName(anyString())).thenReturn(productCatalogue);
		when(multimediaService.findByCaseId(anyString())).thenReturn(null);
		when(multipartFile.getContentType()).thenReturn("");
		when(multipartFile.getBytes()).thenReturn(bytes);
		when(multipartFile.getOriginalFilename()).thenReturn("Midwifery kit image");
		when(multimediaService.saveFile(any(MultimediaDTO.class),any(byte[].class),anyString())).thenReturn("Success");

		productCatalogueResource.create(multipartFile,productCatalogue);

		verify(productCatalogueService).add(argumentCaptor.capture());

		// verify call

		Mockito.verify(multimediaService).findByCaseId(anyString());

		Mockito.verify(multimediaService).saveFile(Mockito.any(MultimediaDTO.class), Mockito.any(byte[].class),
				anyString());

		ProductCatalogue catalogue = argumentCaptor.getValue();
		assertEquals("Scale", catalogue.getProductName());
	}

	@Test
	public void testUpdate() throws Exception {

		MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
		ProductCatalogue productCatalogue = createProductCatalog();
		productCatalogue.setUniqueId(1l);
		Authentication authentication = mock(Authentication.class);
		authentication.setAuthenticated(Boolean.TRUE);
		SecurityContext securityContext = mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		byte[] bytes = new byte[10];
		Multimedia multimedia = new Multimedia();
		multimedia.setCaseId("1");

		when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());

		Mockito.doNothing().when(productCatalogueService).add(any(ProductCatalogue.class));
		when(productCatalogueService.getProductCatalogueByName(anyString())).thenReturn(productCatalogue);
		when(multimediaService.findByCaseId(anyString())).thenReturn(multimedia);
		Mockito.doNothing().when(multimediaService).deleteMultimedia(any(Multimedia.class));
		when(multipartFile.getContentType()).thenReturn("");
		when(multipartFile.getBytes()).thenReturn(bytes);
		when(multipartFile.getOriginalFilename()).thenReturn("Midwifery kit image");
		when(multimediaService.saveFile(any(MultimediaDTO.class),any(byte[].class),anyString())).thenReturn("Success");

		productCatalogueResource.update(1l,multipartFile,productCatalogue);

		verify(productCatalogueService).update(argumentCaptor.capture());

		Mockito.verify(multimediaService).findByCaseId(anyString());

		Mockito.verify(multimediaService).deleteMultimedia(any(Multimedia.class));

		Mockito.verify(multimediaService).saveFile(Mockito.any(MultimediaDTO.class), Mockito.any(byte[].class),
				anyString());

		ProductCatalogue catalogue = argumentCaptor.getValue();
		assertEquals("Scale", catalogue.getProductName());
	}

	@Test
	public void testGetByUniqueId() throws Exception {
		ProductCatalogue productCatalogue = createProductCatalog();
		productCatalogue.setUniqueId(1l);
		when(productCatalogueService.getProductCatalogue(anyLong()))
				.thenReturn(productCatalogue);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/1"))
				.andExpect(status().isOk())
				.andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(1, actualObj.get("uniqueId").asLong());
		assertEquals("Scale", actualObj.get("productName").asText());
		assertEquals("MT-123", actualObj.get("materialNumber").asText());
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

	private Authentication getMockedAuthentication() {
		Authentication authentication = new Authentication() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return "";
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return "Test User";
			}

			@Override
			public boolean isAuthenticated() {
				return false;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

			}

			@Override
			public String getName() {
				return "admin";
			}
		};

		return authentication;
	}

}
