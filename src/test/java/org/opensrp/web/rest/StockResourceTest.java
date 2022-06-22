package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.opensrp.common.AllConstants.Stock.PROVIDERID;
import static org.opensrp.common.AllConstants.Stock.TIMESTAMP;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.opensrp.common.AllConstants;
import org.opensrp.dto.CsvBulkImportDataSummary;
import org.opensrp.dto.FailedRecordSummary;
import org.opensrp.search.StockSearchBean;
import org.opensrp.service.StockService;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.smartregister.domain.Inventory;
import org.smartregister.domain.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class StockResourceTest {

    private final String BASE_URL = "/rest/stockresource/";
    private final String SYNC_PAYLOAD = "{"
            + "\"stocks\": [{\"identifier\":123,\"providerid\":\"test-id\"}]"
            + "}";
    private final String INVENTORY_PAYLOAD = "{\n"
            + "    \"productName\" : \"Midwifery Kit\",\n"
            + "    \"unicefSection\" : \"Health Worker\",\n"
            + "    \"quantity\" : 10,\n"
            + "    \"deliveryDate\" : \"2019-12-12\",\n"
            + "    \"donor\" : \"XYZ donor\",\n"
            + "    \"servicePointId\":\"loc-1\",\n"
            + "    \"poNumber\":111,\n"
            + "    \"serialNumber\":\"1234serial\"\n"
            + "}";
    private final String POST_SYNC_PAYLOAD = "{\n"
            + "  \"locations\": [\n"
            + "     \"90397\"\n"
            + "  ]\n"
            + "}";
    private final String IMPORT_INVENTORY_SUMMARY_REPORT_WITH_ERRORS = "\"Total Number of Rows in the CSV \",2\r\n\"Rows processed \",1\r\n\"\n\"\r\nRow Number,Reason of Failure\r\n1,[PO Number should be a whole number]\r\n";
    private final String IMPORT_INVENTORY_SUMMARY_REPORT_WITHOUT_ERRORS = "\"Total Number of Rows in the CSV \",2\r\n\"Rows processed \",2\r\n\"";
    private final String VALIDATE_INVENTORY_SUMMARY = "{\"rowCount\":2}";
    private final String ERROR_OCCURED = "Error occurred";
    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected ObjectMapper mapper = new ObjectMapper();
    private MockMvc mockMvc;
    @Mock
    private StockService stockService;
    @InjectMocks
    private StockResource stockResource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(stockResource)
                .addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
                .build();
    }

    @Test
    public void testGetByUniqueId() throws Exception {
        Stock expected = createStock();
        when(stockService.find(any(String.class))).thenReturn(expected);
        Stock actual = stockResource.getByUniqueId("123");
        assertEquals(actual.getIdentifier(), actual.getIdentifier());
    }

    @Test
    public void testGetAll() throws Exception {
        List<Stock> expected = new ArrayList<>();
        expected.add(createStock());
        when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class)))
                .thenReturn(expected);

        MvcResult result = mockMvc.perform(get(BASE_URL + "getall")
                        .param("limit", "1"))
                .andExpect(status().isOk()).andReturn();

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);

        assertEquals(actualObj.get("stocks").get(0).get("identifier").asLong(), 12345l);
        assertEquals(actualObj.get("stocks").get(0).get("id").asText(), "ID-123");
        assertEquals(actualObj.get("stocks").size(), 1);
    }

    @Test
    public void testGetAllWithServerVersionParam() throws Exception {
        List<Stock> expected = new ArrayList<>();
        expected.add(createStock());

        when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class)))
                .thenReturn(expected);

        MvcResult result = mockMvc.perform(get(BASE_URL + "getall?serverVersion=123&limit=1"))
                .andExpect(status().isOk()).andReturn();

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);

        assertEquals(actualObj.get("stocks").get(0).get("identifier").asLong(), 12345l);
        assertEquals(actualObj.get("stocks").get(0).get("id").asText(), "ID-123");
        assertEquals(actualObj.get("stocks").size(), 1);
    }

    @Test
    public void testGetAllWithException() throws Exception {
        when(stockService.findAllStocks(nullable(Long.class), nullable(Integer.class))).thenReturn(null);
        mockMvc.perform(get(BASE_URL + "getall"))
                .andExpect(status().isInternalServerError()).andReturn();
    }

    @Test
    public void testSync() throws Exception {
        List<Stock> expected = new ArrayList<>();
        expected.add(createStock());

        when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class)))
                .thenReturn(expected);

        MvcResult result = mockMvc
                .perform(get(BASE_URL + "/sync").param(AllConstants.BaseEntity.SERVER_VERSIOIN, "15421904649873")
                        .param("limit", "0"))
                .andExpect(status().isOk()).andReturn();

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);
        assertEquals(actualObj.get("stocks").size(), 1);
    }

    @Test
    public void testSyncThrowsException() throws Exception {
        when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class)))
                .thenReturn(null);

        mockMvc.perform(get(BASE_URL + "/sync")
                        .param(AllConstants.BaseEntity.SERVER_VERSIOIN, "15421904649873")
                        .param("limit", "1"))
                .andExpect(status().isInternalServerError())
                .andReturn();
    }

    @Test
    public void testSyncV2() throws Exception {
        List<Stock> expected = new ArrayList<>();
        Stock stock = createStock();
        stock.setLocationId("90397");
        expected.add(stock);

        when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class)))
                .thenReturn(expected);

        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                        .content(POST_SYNC_PAYLOAD.getBytes()))
                .andExpect(status().isOk()).andReturn();

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);
        assertEquals(actualObj.get("stocks").size(), 1);
    }

    @Test
    public void testSyncV2ThrowsException() throws Exception {
        when(stockService.findStocks(any(StockSearchBean.class), any(String.class), any(String.class), any(int.class)))
                .thenReturn(null);

        MvcResult result = mockMvc.perform(
                        post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(POST_SYNC_PAYLOAD.getBytes()))
                .andExpect(status().isInternalServerError()).andReturn();

        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        assertTrue(responseString.contains(ERROR_OCCURED));
    }

    @Test
    public void testCreate() {
        Stock expected = createStock();
        Stock stockObject = new Stock();
        stockObject.setId("ID-123");
        when(stockService.addStock(any(Stock.class))).thenReturn(expected);
        Stock actual = stockResource.create(stockObject);
        assertEquals(actual.getId(), actual.getId());
    }

    @Test
    public void testUpdate() {
        Stock expected = createStock();
        Stock stockObject = new Stock();
        stockObject.setId("ID-123");
        when(stockService.mergeStock(any(Stock.class))).thenReturn(expected);
        Stock actual = stockResource.update(stockObject);
        assertEquals(actual.getId(), actual.getId());
    }

    @Test
    public void testSave() throws Exception {
        when(stockService.addorUpdateStock(any(Stock.class))).thenReturn(createStock());
        MvcResult result = mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON)
                        .content(SYNC_PAYLOAD.getBytes()))
                .andExpect(status().isCreated()).andReturn();

        assertEquals(result.getResponse().getContentAsString(), "");
    }

    @Test
    public void testSaveWithBlankData() throws Exception {
        when(stockService.addorUpdateStock(any(Stock.class))).thenReturn(createStock());
        MvcResult result = mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON)
                        .content("".getBytes()))
                .andExpect(status().isBadRequest()).andReturn();
        assertEquals(result.getResponse().getContentAsString(), "");
    }

    @Test
    public void testRequiredProperties() {
        List<String> actualRequiredProperties = stockResource.requiredProperties();

        assertEquals(2, actualRequiredProperties.size());
        assertTrue(actualRequiredProperties.contains(PROVIDERID));
        assertTrue(actualRequiredProperties.contains(TIMESTAMP));
    }

    @Test
    public void testFilter() {
        List<Stock> expected = new ArrayList<>();
        expected.add(createStock());
        when(stockService.findAllStocks(nullable(Long.class), nullable(Integer.class))).thenReturn(expected);
        List<Stock> actual = stockResource.filter("");
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.get(0).getIdentifier(), actual.get(0).getIdentifier());
    }

    @Test
    public void testCreateInventory() throws Exception {
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(Boolean.TRUE);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());
        Mockito.doNothing().when(stockService).addInventory(any(Inventory.class), anyString());
        MvcResult result = mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(INVENTORY_PAYLOAD.getBytes()))
                .andExpect(status().isCreated()).andReturn();
        verify(stockService).addInventory(any(Inventory.class), anyString());
        assertEquals("", result.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateInventory() throws Exception {
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(Boolean.TRUE);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());
        Mockito.doNothing().when(stockService).updateInventory(any(Inventory.class), anyString());
        MvcResult result = mockMvc.perform(
                        put(BASE_URL + "/{id}", "ead1e3ef-b086-4d99-89f5-3dfc5f67709e").contentType(MediaType.APPLICATION_JSON)
                                .content(INVENTORY_PAYLOAD.getBytes()))
                .andExpect(status().isCreated()).andReturn();
        verify(stockService).updateInventory(any(Inventory.class), anyString());
        assertEquals(result.getResponse().getContentAsString(), "");
    }

    @Test
    public void testDelete() throws Exception {
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/{id}", 1))
                .andExpect(status().isNoContent()).andReturn();
        verify(stockService, Mockito.times(1)).deleteStock(argumentCaptor.capture());
        assertEquals(1, argumentCaptor.getValue().longValue());
    }

    @Test
    public void testGetStockItemsByServicePoint() throws Exception {
        List<Stock> stocks = new ArrayList<>();
        stocks.add(createStock());
        when(stockService.getStocksByServicePointId(any(StockSearchBean.class))).thenReturn(stocks);
        MvcResult result =
                mockMvc.perform(get(BASE_URL + "/servicePointId/{servicePointId}", "loc-1"))
                        .andExpect(status().isOk()).andReturn();

        verify(stockService)
                .getStocksByServicePointId(any(StockSearchBean.class));
        Mockito.verifyNoMoreInteractions(stockService);

        List<Stock> response = (List<Stock>) result.getModelAndView().getModel().get("stockList");

        if (response.size() == 0) {
            fail("Test case failed");
        }

        assertEquals(1, response.size());
        assertEquals("12345", response.get(0).getIdentifier());

    }

    @Test
    public void testValidateBulkInventoryDataWithErrors() throws Exception {
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(Boolean.TRUE);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());
        String path = "src/test/resources/sample/inventory.csv";
        MockMultipartFile file = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
                Files.readAllBytes(Paths.get(path)));

        CsvBulkImportDataSummary csvBulkImportDataSummary = new CsvBulkImportDataSummary();
        csvBulkImportDataSummary.setNumberOfCsvRows(2);
        csvBulkImportDataSummary.setNumberOfRowsProcessed(1);
        List<FailedRecordSummary> failedRecordSummaryList = new ArrayList<>();
        FailedRecordSummary failedRecordSummary = new FailedRecordSummary();
        List<String> reasons = new ArrayList<>();
        reasons.add("PO Number should be a whole number");
        failedRecordSummary.setReasonOfFailure(reasons);
        failedRecordSummary.setRowNumber(1);
        failedRecordSummaryList.add(failedRecordSummary);
        csvBulkImportDataSummary.setFailedRecordSummaryList(failedRecordSummaryList);

        when(stockService.validateBulkInventoryData(anyList())).thenReturn(csvBulkImportDataSummary);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart(BASE_URL + "/inventory/validate")
                                .file(file))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        assertEquals(IMPORT_INVENTORY_SUMMARY_REPORT_WITH_ERRORS, responseString);
    }

    @Test
    public void testValidateBulkInventoryDataWithOutErrors() throws Exception {
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(Boolean.TRUE);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());
        String path = "src/test/resources/sample/inventory.csv";
        MockMultipartFile file = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
                Files.readAllBytes(Paths.get(path)));

        CsvBulkImportDataSummary csvBulkImportDataSummary = new CsvBulkImportDataSummary();
        csvBulkImportDataSummary.setNumberOfCsvRows(2);
        csvBulkImportDataSummary.setNumberOfRowsProcessed(1);
        List<FailedRecordSummary> failedRecordSummaryList = new ArrayList<>();
        csvBulkImportDataSummary.setFailedRecordSummaryList(failedRecordSummaryList);

        when(stockService.validateBulkInventoryData(anyList())).thenReturn(csvBulkImportDataSummary);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart(BASE_URL + "/inventory/validate")
                                .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        assertEquals(VALIDATE_INVENTORY_SUMMARY, responseString);
    }

    @Test
    public void testImportInventoryDataWithErrors() throws Exception {
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(Boolean.TRUE);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(getMockedAuthentication());
        String path = "src/test/resources/sample/inventory.csv";
        MockMultipartFile file = new MockMultipartFile("file", "sampleFile.txt", "text/csv",
                Files.readAllBytes(Paths.get(path)));

        CsvBulkImportDataSummary csvBulkImportDataSummary = new CsvBulkImportDataSummary();
        csvBulkImportDataSummary.setNumberOfCsvRows(2);
        csvBulkImportDataSummary.setNumberOfRowsProcessed(2);
        List<FailedRecordSummary> failedRecordSummaryList = new ArrayList<>();
        csvBulkImportDataSummary.setFailedRecordSummaryList(failedRecordSummaryList);

        when(stockService.convertandPersistInventorydata(anyList(), anyString())).thenReturn(csvBulkImportDataSummary);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart(BASE_URL + "/import/inventory")
                                .file(file))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        assertTrue(responseString.contains(IMPORT_INVENTORY_SUMMARY_REPORT_WITHOUT_ERRORS));
    }

    private Stock createStock() {
        Stock stock = new Stock();
        stock.setIdentifier("12345");
        stock.setId("ID-123");
        return stock;
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
                // TODO Auto-generated method stub
            }

            @Override
            public String getName() {
                return "admin";
            }
        };

        return authentication;
    }
}
