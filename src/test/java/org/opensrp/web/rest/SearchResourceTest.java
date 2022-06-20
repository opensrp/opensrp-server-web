
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
import org.opensrp.service.*;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.utils.SearchHelper;
import org.smartregister.domain.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

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
	String town = "town";

	String firstName = "name";

	String male = "male";

	DateTime birthDate = new DateTime(0l, DateTimeZone.UTC);

	DateTime deathDate = new DateTime(1l, DateTimeZone.UTC);

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
		List<Client> listA = Arrays.asList(new Client[] { clientA });
		List<Client> result = SearchHelper.intersection(null, listA);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(listA, result);
		
	}
	@Test
	public void shouldSearchClient() {
		mockHttpServletRequest= new MockHttpServletRequest();
		mockHttpServletRequest.addParameter("ff","ona");
		mockHttpServletRequest.addParameter("alt_phone_number",phoneNumber);
		mockHttpServletRequest.addParameter("alt_name",firstName);
		mockHttpServletRequest.addParameter("attribute","next_contact_date:2022-06-15");
		mockHttpServletRequest.addParameter("dob", String.valueOf(birthDate));
		List <Client> clients= ReflectionTestUtils.invokeMethod(SearchResource.class,"search",mockHttpServletRequest);
		Assert.assertNotNull(clients);
	}
}
