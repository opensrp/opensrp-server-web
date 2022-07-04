package org.opensrp.web.rest.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensrp.repository.postgres.ClientsRepositoryImpl;
import org.opensrp.repository.postgres.EventsRepositoryImpl;
import org.opensrp.web.rest.SearchResource;
import org.smartregister.domain.Address;
import org.smartregister.domain.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
@RunWith(MockitoJUnitRunner.class)
public class SearchResourceTest extends BaseResourceTest {
	MockHttpServletRequest mockHttpServletRequest;



	private final static String BASE_URL = "/rest/search/";

	public static final DateTime DATE_CREATED = new DateTime(0l, DateTimeZone.UTC);

	public static final String MIDDLE_NAME = "middlename";

	public static final String LAST_NAME = "lastName";

	public static final String IDENTIFIER_TYPE = "type";

	public static final String IDENTIFIER = "value";

	public static final String ATTRIBUTES_NAME = "name";

	public static final String ATTRIBUTES_VALUE = "value";

	public static final String FEMALE = "female";

	@Mock
	private SearchResource searchResource;

	@Autowired
	private ClientsRepositoryImpl allClients;

	@Autowired
	private EventsRepositoryImpl allEvents;

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
	String phoneNumber = "0727000000";

	Address address = new Address().withAddressType(addressType).withCountry(country).withStateProvince(stateProvince)
			.withCityVillage(cityVillage).withCountyDistrict(countryDistrict).withSubDistrict(subDistrict).withTown(town);

	@Before
	public void setUp() {
		allClients.removeAll();
		allEvents.removeAll();
	}

	@After
	public void cleanUp() {
		allEvents.removeAll();
		allClients.removeAll();
	}

	@Test
	public void shouldSearchClientWithFirstName() throws Exception {
		Client expectedClient = createOneSearchableClient();

		String searchQuery = "firstName=" + firstName;
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);

		assertEquals(expectedClient, actualClient);

	}

	@Test
	public void shouldSearchClientWithMiddleName() throws Exception {
		Client expectedClient = createOneSearchableClient();

		String searchQuery = "middleName=" + MIDDLE_NAME;
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);

		assertEquals(expectedClient, actualClient);
	}

	@Test
	public void canNotSearchIfAnyNamePortionHasCamelCaseLetter() throws Exception {
		Client expectedClient = createOneSearchableClient();

		String searchQuery = "lastName=" + LAST_NAME;
		JsonNode actualObj = searchClient(searchQuery);

		assertNull(actualObj.get(0));
	}

	@Test
	public void shouldSearchClientWithGender() throws Exception {
		Client expectedClient = createOneSearchableClient();

		String searchQuery = "gender=" + male;
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);

		assertEquals(expectedClient, actualClient);
	}

	@Test
	public void shouldSearchClientWithBirthDate() throws Exception {
		Client expectedClient = createOneSearchableClient();

		String searchQuery = "birthdate=" + birthDate.toLocalDate() + ":" + birthDate.toLocalDate();
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);

		assertEquals(expectedClient, actualClient);
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
	public void shouldSearchClientWithoutAltMobileNumber() throws Exception {
		Client expectedClient = createDifferentClient();
		String searchQuery = "alt_phone_number=" + phoneNumber;
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(1), Client.class);
		assertNotEquals(expectedClient.toString().length(), actualClient.toString().length());
		assertNotEquals(expectedClient.getAttributes().get("alt_phone_number"),
				actualClient.getAttributes().get("alt_phone_number"));
	}

	@Test
	public void shouldSearchClientAltWithMobileNumber() throws Exception {
		Client expectedClient = createOneSearchableClient();
		String searchQuery = "alt_phone_number=" + phoneNumber;
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);
		assertEquals(expectedClient, actualClient);
	}

	@Test
	public void shouldSearchClientByAltName() throws Exception {
		mockHttpServletRequest= new MockHttpServletRequest();
		Client expectedClient = createOneSearchableClient();
		String searchQuery = "alt_name=" + "ona";
		JsonNode actualObj = searchClient(searchQuery);
		mockHttpServletRequest.addParameter("ff",firstName);
		mockHttpServletRequest.addParameter("alt_phone_number",phoneNumber);
		mockHttpServletRequest.addParameter("alt_name","ona");
		mockHttpServletRequest.addParameter("attribute","next_contact_date:2022-06-15");
		mockHttpServletRequest.addParameter("dob", String.valueOf(birthDate));
       verify(searchResource,times(1)).search(mockHttpServletRequest);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);
		assertEquals(expectedClient, actualClient);

	}

	@Test
	public void shouldSearchClientWithLastEdited() throws Exception {
		Client expectedClient = createOneSearchableClient();

		String searchQuery =
				"lastEdited=" + DATE_CREATED.toLocalDate() + ":" + DATE_CREATED.toLocalDate();
		JsonNode actualObj = searchClient(searchQuery);
		Client actualClient = mapper.treeToValue(actualObj.get(0), Client.class);

		assertEquals(expectedClient, actualClient);
	}


	@Test
	public void shouldSearchClientWithAttribute() throws Exception {
		Client expectedClient = createOneSearchableClient();

		String searchQuery = "attribute=" + ATTRIBUTES_NAME + ":" + ATTRIBUTES_VALUE;
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
		otherClient.withAttribute("phone_number",phoneNumber);
		otherClient.withAttribute("alt_name","ona");
		Client otherClient2 = (Client) new Client("3").withFirstName("dd").withMiddleName("fdf").withLastName("sfd")
				.withGender(FEMALE).withBirthdate(birthDate, false).withDeathdate(deathDate, true).withAddress(address);
		otherClient2.setDateCreated(DATE_CREATED);
		otherClient2.withIdentifier("hg", "ghgh");
		otherClient2.withAttribute("hg", "hgh");
		otherClient2.withAttribute("alt_phone_number", phoneNumber);
		otherClient2.withAttribute("phone_number", phoneNumber);
		otherClient2.withAttribute("alt_name", "ona");

		addObjectToRepository(asList(expectedClient, otherClient, otherClient2), allClients);

		return expectedClient;
	}

	private Client createDifferentClient() {
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
		otherClient.withAttribute("phone_number", "0727000000");
		otherClient.withAttribute("alt_name", "ona");
		Client otherClient2 = (Client) new Client("3").withFirstName("dd").withMiddleName("fdf").withLastName("sfd")
				.withGender(FEMALE).withBirthdate(birthDate, false).withDeathdate(deathDate, true).withAddress(address);
		otherClient2.setDateCreated(DATE_CREATED);
		otherClient2.withIdentifier("hg", "ghgh");
		otherClient2.withAttribute("hg", "hgh");
		otherClient2.withAttribute("alt_phone_number", "0727000000");
		otherClient2.withAttribute("phone_number", "0727000000");
		otherClient2.withAttribute("alt_name", "ona");

		addObjectToRepository(asList(expectedClient, otherClient, otherClient2), allClients);
		return expectedClient;
	}

}
