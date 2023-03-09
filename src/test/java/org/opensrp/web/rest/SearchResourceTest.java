
package org.opensrp.web.rest;

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
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.ExportEventDataMapper;
import org.opensrp.service.SearchService;
import org.opensrp.service.TaskGenerator;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.utils.SearchHelper;
import org.smartregister.domain.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.WebApplicationContext;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class SearchResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private SearchService searchService;

	private ClientService clientService;

	private EventService eventService;

	private ExportEventDataMapper exportEventDataMapper;

	private TaskGenerator taskGenerator;

	private PlanRepository planRepository;

	MockHttpServletRequest mockHttpServletRequest;

	String phoneNumber = "0727000000";

	String firstName = "name";

	DateTime birthDate = new DateTime(0l, DateTimeZone.UTC);

	@Before
	public void setUp() {
		SearchRepository searchRepository = Mockito.mock(SearchRepository.class);
		ClientsRepository clientRepository = Mockito.mock(ClientsRepository.class);
		EventsRepository eventsRepository = Mockito.mock(EventsRepository.class);

		searchService = Mockito.spy(new SearchService(searchRepository));
		clientService = Mockito.spy(new ClientService(clientRepository));
		eventService = Mockito.spy(
				new EventService(eventsRepository, clientService, taskGenerator, planRepository, exportEventDataMapper));

	}

	@Test
	public void testInstantanceCreatesCorrectly() {
		SearchResource searchResource = new SearchResource(searchService, clientService, eventService);
		Assert.assertNotNull(searchResource);

	}

	@Test
	public void testIntersectionMethodReturnsCorrectResult() {
		Client clientA = Mockito.mock(Client.class);
		List<Client> listA = Arrays.asList(new Client[] { clientA });
		List<Client> result = SearchHelper.intersection(null, listA);

		Assert.assertNotNull(result);
		Assert.assertEquals(listA, result);

	}

	@Test
	public void shouldSearchClientWithGetRequest() throws ParseException {
		mockHttpServletRequest = new MockHttpServletRequest();
		mockHttpServletRequest.addParameter("ff", "ona");
		mockHttpServletRequest.addParameter("phone_number", phoneNumber);
		mockHttpServletRequest.addParameter("alt_phone_number", phoneNumber);
		mockHttpServletRequest.addParameter("alt_name", firstName);
		mockHttpServletRequest.addParameter("attribute", "next_contact_date:2022-06-15");
		mockHttpServletRequest.addParameter("dob", String.valueOf(birthDate));
		mockHttpServletRequest.addParameter("identifier", "fsdf" + ":" + "sfdf");
		SearchResource searchResource = new SearchResource(searchService, clientService, eventService);
		List<Client> clients = searchResource.search(mockHttpServletRequest);
		Assert.assertNotNull(clients);
	}

	@Test
	public void shouldSearchClientWithPostRequest() throws ParseException {
		String jsonRequestString = "{\"ff\":\"ona\",\"identifier\":\"fsdf:sfdf\",\"alt_name\":\"name\"," +
				"\"alt_phone_number\":\"0727000000\",\"dob\":\"1970-01-01T00:00:00.000Z\",\"phone_number\":\"0727000000\"," +
				"\"attribute\":\"next_contact_date:2022-06-15\"}";
		SearchResource searchResource = new SearchResource(searchService, clientService, eventService);
		List<Client> clients = searchResource.searchByPost(jsonRequestString);
		Assert.assertNotNull(clients);

	}
}
