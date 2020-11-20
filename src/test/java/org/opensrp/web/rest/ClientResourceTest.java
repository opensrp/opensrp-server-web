package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensrp.common.AllConstants;
import org.opensrp.web.Constants;
import org.smartregister.domain.Client;
import org.opensrp.domain.postgres.HouseholdClient;
import org.opensrp.search.AddressSearchBean;
import org.opensrp.search.ClientSearchBean;
import org.opensrp.service.ClientService;
import org.opensrp.web.bean.ClientSyncBean;
import org.opensrp.web.bean.Identifier;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.Client.BIRTH_DATE;
import static org.opensrp.common.AllConstants.Client.FIRST_NAME;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class ClientResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private ClientService clientService;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private ClientResource clientResource;

	protected ObjectMapper mapper = new ObjectMapper();

	private final String BASE_URL = "/rest/client";

	private static final String PAGE_SIZE = "pageSize";

	private static final String PAGE_NUMBER = "pageNumber";
	private static final String SEARCHTEXT = "searchText";
	private static final String GENDER = "gender";
	private static final String CLIENTTYPE = "clientType";
	public static final String ALLCLIENTS = "clients";
	public static final String HOUSEHOLD = "ec_family";
	public static final String HOUSEHOLDMEMEBR = "householdMember";
	public static final String ANC = "anc";
	public static final String CHILD = "child";
	public static final String LOCATION_ID = "locationId";


	private String EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON =  "{\n"
			+ "\t\"clients\": [{\n"
			+ "\t\t\"firstName\": \"Test\",\n"
			+ "\t\t\"lastName\": \"User\"\n"
			+ "\t}],\n"
			+ "\t\"total\": 1\n"
			+ "}";


	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(clientResource)
				.addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
				.build();
		clientResource.setObjectMapper(objectMapper);
	}

	@Test
	public void testRequiredProperties() {
		List<String> requiredProperties = clientResource.requiredProperties();
		assertTrue(requiredProperties.contains(FIRST_NAME));
		assertTrue(requiredProperties.contains(GENDER));
		assertTrue(requiredProperties.contains(BIRTH_DATE));
		assertTrue(requiredProperties.contains(BASE_ENTITY_ID));
	}
	
	@Test
	public void testGetByUniqueId() {
		Client expected = createClient();
		when(clientService.find(any(String.class))).thenReturn(expected);
		Client actual = clientResource.getByUniqueId("1");
		assertEquals(actual.getFirstName(),expected.getFirstName());
		assertEquals(actual.getLastName(),expected.getLastName());
		assertEquals(actual.getId(),expected.getId());
		assertEquals(actual.getBaseEntityId(),expected.getBaseEntityId());
	}

	@Test
	public void testCreateClient() {
		Client expected = createClient();
		when(clientService.addClient(any(Client.class))).thenReturn(expected);
		Client obj = new Client("base-entity-id");
		Client actual = clientResource.create(obj);
		assertEquals(actual.getId(),expected.getId());
		assertEquals(actual.getDateCreated(),expected.getDateCreated());
	}

	@Test
	public void testUpdateClient() {
		Client expected = createClient();
		when(clientService.mergeClient(any(Client.class))).thenReturn(expected);
		Client obj = new Client("base-entity-id");
		Client actual = clientResource.update(obj);
		assertEquals(actual.getId(),expected.getId());
		assertEquals(actual.getDateEdited(),expected.getDateEdited());
	}
	
	@Test
	public void testFilter() {
		List<Client> expected = new ArrayList<>();
		expected.add(createClient());
		
		when(clientService.findByDynamicQuery(anyString())).thenReturn(expected);
		List<Client> actual = clientResource.filter("");
		assertEquals(actual.size(),expected.size());
		assertEquals(actual.get(0).getFirstName(),expected.get(0).getFirstName());
	}

	@Test
	public void testSearchByCriteriaWithClientTypeAsClients() throws Exception {

		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		HouseholdClient householdClient = new HouseholdClient();
		householdClient.setTotalCount(1);

		when(clientService.findAllClientsByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(expectedClients);
		when(clientService.findTotalCountHouseholdByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(householdClient);
		when(objectMapper.writeValueAsString(any(Object.class))).thenReturn(EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/searchByCriteria").
				param(AllConstants.BaseEntity.BASE_ENTITY_ID, "15421904649873")
				.param(PAGE_NUMBER, "1").param(PAGE_SIZE, "10").param(SEARCHTEXT, "abc").param(GENDER, "male")
				.param(CLIENTTYPE, ALLCLIENTS))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		ClientSyncBean response = mapper.treeToValue(actualObj, ClientSyncBean.class);

		assertEquals(response.getClients().size(),1);
		assertEquals(response.getTotal().intValue(), 1);
		assertEquals(response.getClients().get(0).getFirstName(), "Test");
		assertEquals(response.getClients().get(0).getLastName(), "User");

	}

	@Test
	public void testSearchByCriteriaWithClientTypeAsHousehold() throws Exception {

		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		HouseholdClient householdClient = new HouseholdClient();
		householdClient.setTotalCount(1);

		when(clientService.findHouseholdByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class), any(DateTime.class),any(DateTime.class))).thenReturn(expectedClients);
		when(clientService.findTotalCountHouseholdByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(householdClient);
		when(objectMapper.writeValueAsString(any(Object.class))).thenReturn(EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/searchByCriteria").
				param(BASE_ENTITY_ID, "15421904649873")
				.param(PAGE_NUMBER, "1").param(PAGE_SIZE, "10").param(SEARCHTEXT, "abc").param(GENDER, "male")
				.param(CLIENTTYPE, HOUSEHOLD))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		ClientSyncBean response = mapper.treeToValue(actualObj, ClientSyncBean.class);

		assertEquals(response.getClients().size(),1);
		assertEquals(response.getTotal().intValue(), 1);
		assertEquals(response.getClients().get(0).getFirstName(), "Test");
		assertEquals(response.getClients().get(0).getLastName(), "User");

	}

	@Test
	public void testSearchByCriteriaWithClientTypeAsHouseholdMember() throws Exception {

		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());

		when(clientService.findMembersByRelationshipId(anyString())).thenReturn(expectedClients);
		when(objectMapper.writeValueAsString(any(Object.class))).thenReturn(EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/searchByCriteria").
				param(BASE_ENTITY_ID, "15421904649873")
				.param(PAGE_NUMBER, "1").param(PAGE_SIZE, "10").param(SEARCHTEXT, "abc").param(GENDER, "male")
				.param(CLIENTTYPE, HOUSEHOLDMEMEBR))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		ClientSyncBean response = mapper.treeToValue(actualObj, ClientSyncBean.class);

		assertEquals(response.getClients().size(),1);
		assertEquals(response.getTotal().intValue(), 1);
		assertEquals(response.getClients().get(0).getFirstName(), "Test");
		assertEquals(response.getClients().get(0).getLastName(), "User");
	}

	@Test
	public void testSearchByCriteriaWithClientTypeAsANC() throws Exception {

		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		int total = 1;

		when(clientService.findAllANCByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(expectedClients);
		when(clientService.findCountANCByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(total);
		when(objectMapper.writeValueAsString(any(Object.class))).thenReturn(EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/searchByCriteria").
				param(BASE_ENTITY_ID, "15421904649873")
				.param(PAGE_NUMBER, "1").param(PAGE_SIZE, "10").param(SEARCHTEXT, "abc").param(GENDER, "male")
				.param(CLIENTTYPE, ANC))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		ClientSyncBean response = mapper.treeToValue(actualObj, ClientSyncBean.class);

		assertEquals(response.getClients().size(),1);
		assertEquals(response.getTotal().intValue(), 1);
		assertEquals(response.getClients().get(0).getFirstName(), "Test");
		assertEquals(response.getClients().get(0).getLastName(), "User");

	}

	@Test
	public void testSearchByCriteriaWithClientTypeAsChild() throws Exception {

		List<Client> expectedClients = new ArrayList<>();
		expectedClients.add(createClient());
		int total = 1;

		when(clientService.findAllChildByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(expectedClients);
		when(clientService.findCountChildByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class))).thenReturn(total);
		when(objectMapper.writeValueAsString(any(Object.class))).thenReturn(EXPECTED_CLIENT_SYNC_BEAN_RESPONSE_JSON);

		MvcResult result = mockMvc.perform(get(BASE_URL + "/searchByCriteria").
				param(BASE_ENTITY_ID, "15421904649873")
				.param(PAGE_NUMBER, "1").param(PAGE_SIZE, "10").param(SEARCHTEXT, "abc").param(GENDER, "male")
				.param(CLIENTTYPE, CHILD)
				.param(LOCATION_ID, "location1,location2"))
				.andExpect(status().isOk()).andReturn();

		String responseString = result.getResponse().getContentAsString();
		if (responseString.isEmpty()) {
			fail("Test case failed");
		}
		JsonNode actualObj = mapper.readTree(responseString);
		ClientSyncBean response = mapper.treeToValue(actualObj, ClientSyncBean.class);

		assertEquals(response.getClients().size(),1);
		assertEquals(response.getTotal().intValue(), 1);
		assertEquals(response.getClients().get(0).getFirstName(), "Test");
		assertEquals(response.getClients().get(0).getLastName(), "User");

	}
	
	@Test
	public void testSearch() throws ParseException {
		List<Client> expected = new ArrayList<>();
		expected.add(createClient());
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		when(httpServletRequest.getParameter("locationIds")).thenReturn("123,345");
		when(clientService.findByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class), nullable(DateTime.class), nullable(DateTime.class))).thenReturn(expected);
		List<Client> clients = clientResource.search(httpServletRequest);
		assertEquals(clients.size(),expected.size());
		assertEquals(clients.get(0).getFirstName(),expected.get(0).getFirstName());
	}

	@Test
	public void testSearchClientsWithRelationships() throws ParseException {
		List<Client> expected = new ArrayList<>();

		Client client = new Client("client-base-entity-id");
		client.setFirstName("Jared");
		client.setLastName("Odinga");
		client.setId("1");
		client.setDateCreated(new DateTime());
		client.setRelationships(new HashMap<>() {{
			put("mother", Collections.singletonList("client-rel-base-entity-id"));
		}});
		expected.add(client);

		//Create client relationship object
		Client clientRelationship = new Client("client-rel-base-entity-id");
		clientRelationship.setFirstName("Milka");
		clientRelationship.setLastName("Madonna");
		clientRelationship.setId("2");
		clientRelationship.setDateCreated(new DateTime());

		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		when(httpServletRequest.getParameter("name")).thenReturn("Jared");
		when(httpServletRequest.getParameter("relationships")).thenReturn("mother");

		when(clientService.findByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class), nullable(DateTime.class),
				nullable(DateTime.class))).thenReturn(expected);
		when(clientService.find("client-rel-base-entity-id")).thenReturn(clientRelationship);
		List<Client> clients = clientResource.search(httpServletRequest);
		assertEquals(clients.size(),2);
		assertEquals(clients.get(0).getFirstName(),client.getFirstName());
		assertEquals(clients.get(1).getFirstName(), clientRelationship.getFirstName());
	}

	@Test
	public void testSearchClientRelationshipWithDependants() throws ParseException {
		List<Client> expected = new ArrayList<>();
		//Create client relationship object
		Client clientRelationship = new Client("client-rel-base-entity-id");
		clientRelationship.setFirstName("Milka");
		clientRelationship.setLastName("Madonna");
		clientRelationship.setId("2");
		clientRelationship.setDateCreated(new DateTime());
		expected.add(clientRelationship);

		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
		when(httpServletRequest.getParameter("name")).thenReturn("Milka");
		when(httpServletRequest.getParameter("searchRelationship")).thenReturn("mother");
		when(clientService.findByCriteria(any(ClientSearchBean.class),any(AddressSearchBean.class),
				nullable(DateTime.class), nullable(DateTime.class))).thenReturn(expected);

		Client client = new Client("client-base-entity-id");
		client.setFirstName("Jared");
		client.setLastName("Odinga");
		client.setId("1");
		client.setDateCreated(new DateTime());
		client.setRelationships(new HashMap<>() {{
			put("mother", Collections.singletonList("client-rel-base-entity-id"));
		}});
		when(clientService.findByRelationshipIdAndType("mother", "client-rel-base-entity-id"))
				.thenReturn(Collections.singletonList(client));

		List<Client> clients = clientResource.search(httpServletRequest);
		assertEquals(clients.size(),2);
		assertEquals(clients.get(0).getFirstName(),clientRelationship.getFirstName());
		assertEquals(clients.get(1).getFirstName(), client.getFirstName());
	}

	private Client createClient() {
		Client client = new Client("base-entity-id");
		client.setFirstName("Test");
		client.setLastName("User");
		client.setId("1");
		client.setDateCreated(new DateTime());
		client.setDateEdited(new DateTime());
		return client;
	}
	
    @Test
    public void testFindAllIds() throws Exception {
        Pair<List<String>, Long> idsModel = Pair.of(Collections.singletonList("client-id-1"), 12345l);
        when(clientService.findAllIds(anyLong(), anyInt(), anyBoolean(), nullable(Date.class), isNull())).thenReturn(idsModel);
        MvcResult result = mockMvc.perform(get(BASE_URL + "/findIds?serverVersion=0&fromDate=2020-10-0614:35:00.000+03:00", "")).andExpect(status().isOk())
                .andReturn();

        String actualTaskIdString = result.getResponse().getContentAsString();
        Identifier actualIdModels = new Gson().fromJson(actualTaskIdString, new TypeToken<Identifier>(){}.getType());
        List<String> actualTaskIdList = actualIdModels.getIdentifiers();


        verify(clientService).findAllIds(anyLong(), anyInt(), anyBoolean(), nullable(Date.class), isNull());
        verifyNoMoreInteractions(clientService);
        assertEquals("{\"identifiers\":[\"client-id-1\"],\"lastServerVersion\":12345}", result.getResponse().getContentAsString());
        assertEquals((idsModel.getLeft()).get(0), actualTaskIdList.get(0));
        assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());
    }

	@Test
	public void testGetAll() throws Exception {
		Client expectedClient = createClient();

		List<Client> clients = Collections.singletonList(expectedClient);
		when(clientService.findByServerVersion(anyLong(),anyInt()))
				.thenReturn(clients);
		mockMvc
				.perform(get(BASE_URL + "/getAll?serverVersion=0&limit=50"))
				.andExpect(status().isOk()).andReturn();
		verify(clientService).findByServerVersion(0, 50);

	}

	@Test
	public void testCountAll() throws Exception {
		when(clientService.countAll(anyLong()))
				.thenReturn(1L);
		MvcResult mvcResult = mockMvc
				.perform(get(BASE_URL + "/countAll?serverVersion=0"))
				.andExpect(status().isOk()).andReturn();
		String strResponse = mvcResult.getResponse().getContentAsString();
		JSONObject jsonObject = new JSONObject(strResponse);
		verify(clientService).countAll(0);
		assertEquals(1, jsonObject.optInt("count"));
	}

	@Test
	public void testGetAllUsesDefaultLimit() throws Exception {
		Client expectedClient = createClient();

		List<Client> clients = Collections.singletonList(expectedClient);
		when(clientService.findByServerVersion(anyLong(),anyInt()))
				.thenReturn(clients);
		mockMvc
				.perform(get(BASE_URL + "/getAll?serverVersion=0"))
				.andExpect(status().isOk()).andReturn();
		verify(clientService).findByServerVersion(0, Constants.DEFAULT_LIMIT);

	}

	@Test
	public void testFindById() throws Exception {
		Client expectedClient = createClient();
		when(clientService.findById("clientid1"))
				.thenReturn(expectedClient);
		mockMvc
				.perform(get(BASE_URL + "/findById?id=clientid1"))
				.andExpect(status().isOk()).andReturn();
		verify(clientService).findById("clientid1");

	}

}
