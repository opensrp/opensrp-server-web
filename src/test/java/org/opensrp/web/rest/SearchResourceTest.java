
package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.repository.EventsRepository;
import org.opensrp.repository.PlanRepository;
import org.opensrp.repository.SearchRepository;
import org.opensrp.repository.postgres.ClientsRepositoryImpl;
import org.opensrp.service.*;
import org.opensrp.web.rest.it.BaseResourceTest;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.utils.SearchHelper;
import org.smartregister.domain.Address;
import org.smartregister.domain.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class SearchResourceTest  extends BaseResourceTest {
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	private SearchService searchService;
	
	private ClientService clientService;
	
	private EventService eventService;

	private ExportEventDataMapper exportEventDataMapper;


	private TaskGenerator taskGenerator;

	private PlanRepository planRepository;
	@Autowired
	private ClientsRepositoryImpl allClients;

	private final static String BASE_URL = "/rest/search/";
	String phoneNumber = "0727000000";
	public static final DateTime DATE_CREATED = new DateTime(0l, DateTimeZone.UTC);

	public static final String MIDDLE_NAME = "middlename";

	public static final String LAST_NAME = "lastName";

	public static final String IDENTIFIER_TYPE = "type";

	public static final String IDENTIFIER = "value";

	public static final String ATTRIBUTES_NAME = "name";

	public static final String ATTRIBUTES_VALUE = "value";
	public static final String FEMALE = "female";
	String addressType = "addressType";

	String country = "country";

	String stateProvince = "stateProvince";

	String cityVillage = "cityVillage";

	String countryDistrict = "countryDistrict";

	String subDistrict = "subDistrict";

	String town = "town";

	String firstName = "name";

	String male = "male";

	DateTime birthDate = new DateTime(0l, DateTimeZone.UTC);

	DateTime deathDate = new DateTime(1l, DateTimeZone.UTC);
	Address address = new Address().withAddressType(addressType).withCountry(country).withStateProvince(stateProvince)
			.withCityVillage(cityVillage).withCountyDistrict(countryDistrict).withSubDistrict(subDistrict).withTown(town);
	@Before
	public void setUp() {
		SearchRepository searchRepository = Mockito.mock(SearchRepository.class);
		ClientsRepository clientRepository = Mockito.mock(ClientsRepository.class);
		EventsRepository eventsRepository = Mockito.mock(EventsRepository.class);

		searchService = Mockito.spy(new SearchService(searchRepository));
		clientService = Mockito.spy(new ClientService(clientRepository));
		eventService = Mockito.spy(new EventService(eventsRepository, clientService,taskGenerator,planRepository, exportEventDataMapper));
		
	}
	
	@Test
	public void testInstantanceCreatesCorrectly() {
		
		SearchResource searchResource = new SearchResource(searchService, clientService, eventService);
		Assert.assertNotNull(searchResource);
		
	}
	
	@Test
	public void testIntersectionMethodReturnsCorrectResult() {
		
		Client clientA = Mockito.mock(Client.class);
		List<Client> listA = asList(new Client[] { clientA });
		List<Client> result = SearchHelper.intersection(null, listA);
		
		Assert.assertNotNull(result);
		assertEquals(listA, result);
		
	}
	@Test
	public void shouldSearchClientWithBirthDateWithoutColons() throws Exception {
		Client expectedClient = createOneSearchableClient();
		String searchQuery = "birthdate=" + birthDate.toLocalDate();
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);
		assertEquals(expectedClient, actualClient);
	}

	@Test
	public void shouldSearchClientWithMobileNumber() throws Exception {
		Client expectedClient = createOneSearchableClient();
		String searchQuery = "phone_number=" + phoneNumber;
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);
		assertEquals(expectedClient, actualClient);
	}

	@Test
	public void shouldSearchClientByAltName() throws Exception {
		Client expectedClient = createOneSearchableClient();
		String searchQuery = "alt_name=" + "ona";
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);
		assertEquals(expectedClient, actualClient);
	}

	private JsonNode searchClient(String query) throws Exception {
		String searchQuery = "search?" + query;

		return getCallAsJsonNode(BASE_URL + searchQuery, "", status().isOk());
	}

	private Client createOneSearchableClient() {
		Client expectedClient = (Client) new Client("1").withFirstName(firstName).withMiddleName(MIDDLE_NAME)
				.withLastName(LAST_NAME).withGender(male).withBirthdate(birthDate, false).withDeathdate(deathDate, true)
				.withAddress(address);
		expectedClient.setDateCreated(DATE_CREATED);
		expectedClient.withIdentifier(IDENTIFIER_TYPE, IDENTIFIER);
		expectedClient.withAttribute(ATTRIBUTES_NAME, ATTRIBUTES_VALUE);

		Client otherClient = (Client) new Client("2").withFirstName("ff").withMiddleName("fd").withLastName("sfdf")
				.withGender(FEMALE).withBirthdate(birthDate, false).withDeathdate(deathDate, true).withAddress(address);
		otherClient.setDateCreated(DATE_CREATED);
		otherClient.withIdentifier("fsdf", "sfdf");
		otherClient.withAttribute("sfdf", "sfdf");
		otherClient.withAttribute("alt_phone_number",phoneNumber);
		otherClient.withAttribute("alt_name","ona");
		Client otherClient2 = (Client) new Client("3").withFirstName("dd").withMiddleName("fdf").withLastName("sfd")
				.withGender(FEMALE).withBirthdate(birthDate, false).withDeathdate(deathDate, true).withAddress(address);
		otherClient2.setDateCreated(DATE_CREATED);
		otherClient2.withIdentifier("hg", "ghgh");
		otherClient2.withAttribute("hg", "hgh");
		otherClient2.withAttribute("alt_phone_number", phoneNumber);
		otherClient2.withAttribute("alt_name", "ona");
		addObjectToRepository(asList(expectedClient, otherClient, otherClient2), allClients);

		return expectedClient;
	}
}
