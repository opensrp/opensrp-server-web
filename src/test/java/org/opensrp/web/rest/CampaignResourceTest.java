package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.Campaign;
import org.opensrp.service.CampaignService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class CampaignResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	@Mock
	private CampaignService campaignService;

	@InjectMocks
	private CampaignResource campaignResource;

	private String campaignJson = "{\"identifier\":\"IRS_2018_S1\",\"title\":\"2019 IRS Season 1\",\"description\":\"This is the 2010 IRS Spray Campaign for Zambia for the first spray season dated 1 Jan 2019 - 31 Mar 2019.\",\"status\":\"In Progress\",\"executionPeriod\":{\"start\":\"2019-01-01\",\"end\":\"2019-03-31\"},\"authoredOn\":\"2018-10-01T0900\",\"lastModified\":\"2018-10-01T0900\",\"owner\":\"jdoe\",\"serverVersion\":15421904649876}";

	private String BASE_URL = "/rest/campaign/";

	private ArgumentCaptor<Campaign> argumentCaptor = ArgumentCaptor.forClass(Campaign.class);

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(campaignResource)
				.setControllerAdvice(new GlobalExceptionHandler()).build();
	}

	@Test
	public void testGetByUniqueId() throws Exception {
		when(campaignService.getCampaign("IRS_2018_S1")).thenReturn(getCampaign());
		MvcResult result = mockMvc.perform(get(BASE_URL + "/{identifier}", "IRS_2018_S1"))
				.andExpect(status().isOk())
				.andReturn();
		verify(campaignService, times(1)).getCampaign("IRS_2018_S1");
		verifyNoMoreInteractions(campaignService);
		assertEquals(campaignJson, result.getResponse().getContentAsString());

	}

	@Test
	public void testGetByUniqueIdShouldReturnServerError() throws Exception {
		when(campaignService.getCampaign("IRS_2018_S1")).thenThrow(new RuntimeException());
		mockMvc.perform(get(BASE_URL + "/{identifier}", "IRS_2018_S1")).andExpect(status().isInternalServerError());
		verify(campaignService, times(1)).getCampaign("IRS_2018_S1");
		verifyNoMoreInteractions(campaignService);

	}

	@Test
	public void testGetCampaigns() throws Exception {
		List<Campaign> campaigns = new ArrayList<>();
		campaigns.add(getCampaign());
		when(campaignService.getAllCampaigns()).thenReturn(campaigns);
		MvcResult result = mockMvc.perform(get(BASE_URL)).andExpect(status().isOk()).andReturn();
		verify(campaignService, times(1)).getAllCampaigns();
		verifyNoMoreInteractions(campaignService);
		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(campaignJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testGetCampaignsShouldReturnServerError() throws Exception {
		when(campaignService.getAllCampaigns()).thenThrow(new RuntimeException());
		mockMvc.perform(get(BASE_URL)).andExpect(status().isInternalServerError());
		verify(campaignService, times(1)).getAllCampaigns();
		verifyNoMoreInteractions(campaignService);
	}

	@Test
	public void testCreate() throws Exception {
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(campaignJson.getBytes()))
				.andExpect(status().isCreated());
		verify(campaignService, times(1)).addCampaign(argumentCaptor.capture());
		verifyNoMoreInteractions(campaignService);
		assertEquals(campaignJson, CampaignResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testCreateWithInvalidJsonShouldReturnBadRequest() throws Exception {
		mockMvc.perform(
				post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(campaignJson.substring(1).getBytes()))
				.andExpect(status().isBadRequest());
		verify(campaignService, never()).addCampaign(argumentCaptor.capture());
		verifyNoMoreInteractions(campaignService);
	}

	@Test
	public void testCreateShouldReturnServerError() throws Exception {
		when(campaignService.addCampaign(any(Campaign.class))).thenThrow(new RuntimeException());
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(campaignJson.getBytes()))
				.andExpect(status().isInternalServerError());
		verify(campaignService).addCampaign(argumentCaptor.capture());
		verifyNoMoreInteractions(campaignService);
	}

	@Test
	public void testUpdate() throws Exception {
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(campaignJson.getBytes()))
				.andExpect(status().isCreated());
		verify(campaignService, times(1)).updateCampaign(argumentCaptor.capture());
		verifyNoMoreInteractions(campaignService);
		assertEquals(campaignJson, CampaignResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testUpdateWithInvalidJsonShouldReturnBadRequest() throws Exception {
		mockMvc.perform(
				put(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(campaignJson.substring(2).getBytes()))
				.andExpect(status().isBadRequest());
		verify(campaignService, never()).addCampaign(argumentCaptor.capture());
		verifyNoMoreInteractions(campaignService);
	}

	@Test
	public void testUpdateShouldReturnServerError() throws Exception {
		when(campaignService.updateCampaign(any(Campaign.class))).thenThrow(new RuntimeException());
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(campaignJson.getBytes()))
				.andExpect(status().isInternalServerError());
		verify(campaignService).updateCampaign(argumentCaptor.capture());
		verifyNoMoreInteractions(campaignService);
	}

	@Test
	public void testSyncByServerVersion() throws Exception {
		List<Campaign> campaigns = new ArrayList<>();
		campaigns.add(getCampaign());
		when(campaignService.getCampaignsByServerVersion(15421904649873l)).thenReturn(campaigns);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "15421904649873"))
				.andExpect(status().isOk()).andReturn();
		verify(campaignService, times(1)).getCampaignsByServerVersion(15421904649873l);
		verifyNoMoreInteractions(campaignService);
		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(campaignJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testSyncByServerVersionWithInvalidServerVersionShouldReturnAll() throws Exception {
		List<Campaign> campaigns = new ArrayList<>();
		campaigns.add(getCampaign());
		when(campaignService.getCampaignsByServerVersion(15421904649873l)).thenReturn(campaigns);
		MvcResult result = mockMvc.perform(get(BASE_URL + "/sync").param(BaseEntity.SERVER_VERSIOIN, "sdg"))
				.andExpect(status().isOk()).andReturn();
		verify(campaignService).getCampaignsByServerVersion(0l);
		verifyNoMoreInteractions(campaignService);

		assertEquals("[]", result.getResponse().getContentAsString());
	}

	private Campaign getCampaign() {
		return CampaignResource.gson.fromJson(campaignJson, Campaign.class);

	}

}
