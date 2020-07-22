
package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opensrp.common.AllConstants;
import org.opensrp.domain.setting.SettingConfiguration;
import org.opensrp.search.SettingSearchBean;
import org.smartregister.domain.Client;
import org.opensrp.repository.ClientsRepository;
import org.opensrp.repository.EventsRepository;
import org.opensrp.repository.SearchRepository;
import org.opensrp.service.ClientService;
import org.opensrp.service.EventService;
import org.opensrp.service.SearchService;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.utils.SearchHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class SearchResourceTest {
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	private SearchService searchService;
	
	private ClientService clientService;
	
	private EventService eventService;
	
	private SearchRepository searchRepository;
	
	private ClientsRepository clientRepository;
	
	private EventsRepository eventsRepository;
	
	@Before
	public void setUp() {
		
		searchRepository = Mockito.mock(SearchRepository.class);
		searchService = Mockito.spy(new SearchService(searchRepository));
		clientService = Mockito.spy(new ClientService(clientRepository));
		eventService = Mockito.spy(new EventService(eventsRepository, clientService));
		
	}
	
	@Test
	public void testInstantanceCreatesCorrectly() throws Exception {
		
		SearchResource searchResource = new SearchResource(searchService, clientService, eventService);
		Assert.assertNotNull(searchResource);
		
	}
	
	@Test
	public void testIntersectionMethodReturnsCorrectResult() throws Exception {
		
		Client clientA = Mockito.mock(Client.class);
		List<Client> listA = Arrays.asList(new Client[] { clientA });
		List<Client> result = SearchHelper.intersection(null, listA);
		
		Assert.assertNotNull(result);
		Assert.assertEquals(listA, result);
		
	}
}
