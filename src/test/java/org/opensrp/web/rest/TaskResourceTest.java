package org.opensrp.web.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.Task;
import org.opensrp.service.TaskService;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = { "classpath:test-webmvc-config.xml", })
public class TaskResourceTest {

	@Autowired
	protected WebApplicationContext webApplicationContext;

	private MockMvc mockMvc;

	private TaskService taskService;

	private String taskJson = "{\"identifier\":\"tsk11231jh22\",\"campaignIdentifier\":\"IRS_2018_S1\",\"groupIdentifier\":\"2018_IRS-3734\",\"status\":\"Ready\",\"businessStatus\":\"Not Visited\",\"priority\":3,\"code\":\"IRS\",\"description\":\"Spray House\",\"focus\":\"IRS Visit\",\"for\":\"location.properties.uid:41587456-b7c8-4c4e-b433-23a786f742fc\",\"executionStartDate\":\"2018-11-10T2200\",\"executionEndDate\":null,\"authoredOn\":\"2018-10-31T0700\",\"lastModified\":\"2018-10-31T0700\",\"owner\":\"demouser\",\"note\":[{\"authorString\":\"demouser\",\"time\":\"2018-01-01T0800\",\"text\":\"This should be assigned to patrick.\"}],\"serverVersion\":15421904649879}";

	private String BASE_URL = "/rest/task/";

	private ArgumentCaptor<Task> argumentCaptor = ArgumentCaptor.forClass(Task.class);

	@Before
	public void setUp() {
		taskService = Mockito.mock(TaskService.class);
		TaskResource taskResource = webApplicationContext.getBean(TaskResource.class);
		taskResource.setTaskService(taskService);
		mockMvc = MockMvcBuilders.webApplicationContextSetup(webApplicationContext).build();
	}

	@Test
	public void testGetByUniqueId() throws Exception {
		when(taskService.getTask("tsk11231jh22")).thenReturn(getTask());
		MvcResult result = mockMvc.perform(get(BASE_URL + "/{identifier}", "tsk11231jh22")).andExpect(status().isOk())
				.andReturn();
		verify(taskService, times(1)).getTask("tsk11231jh22");
		verifyNoMoreInteractions(taskService);
		assertEquals(taskJson, result.getResponse().getContentAsString());

	}

	@Test
	public void testGetTasksByCampaignAndGroup() throws Exception {
		List<Task> tasks = new ArrayList<>();
		tasks.add(getTask());
		when(taskService.getTasksByCampaignAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l)).thenReturn(tasks);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/sync").param(TaskResource.CAMPAIGN, "IRS_2018_S1")
						.param(TaskResource.GROUP, "2018_IRS-3734").param(BaseEntity.SERVER_VERSIOIN, "15421904649873"))
				.andExpect(status().isOk()).andReturn();
		verify(taskService, times(1)).getTasksByCampaignAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l);
		verifyNoMoreInteractions(taskService);
		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testGetTasksByCampaignAndGroupWithInvalidServerVersionShouldReturnAllServerVersions() throws Exception {
		List<Task> tasks = new ArrayList<>();
		tasks.add(getTask());
		when(taskService.getTasksByCampaignAndGroup("IRS_2018_S1", "2018_IRS-3734", 0l)).thenReturn(tasks);
		MvcResult result = mockMvc
				.perform(get(BASE_URL + "/sync").param(TaskResource.CAMPAIGN, "IRS_2018_S1")
						.param(TaskResource.GROUP, "2018_IRS-3734").param(BaseEntity.SERVER_VERSIOIN, ""))
				.andExpect(status().isOk()).andReturn();
		verify(taskService, times(1)).getTasksByCampaignAndGroup("IRS_2018_S1", "2018_IRS-3734", 0l);
		verifyNoMoreInteractions(taskService);
		JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
		assertEquals(1, jsonreponse.length());
		JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
	}

	@Test
	public void testCreate() throws Exception {
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(taskJson.getBytes()))
				.andExpect(status().isCreated());
		verify(taskService, times(1)).addTask(argumentCaptor.capture());
		verifyNoMoreInteractions(taskService);
		assertEquals(taskJson, TaskResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testCreateWithInvalidJsonShouldReturnInternalError() throws Exception {
		mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(taskJson.substring(3).getBytes()))
				.andExpect(status().isInternalServerError());
		verify(taskService, never()).addTask(argumentCaptor.capture());
		verifyNoMoreInteractions(taskService);
	}

	@Test
	public void testUpdate() throws Exception {
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(taskJson.getBytes()))
				.andExpect(status().isCreated());
		verify(taskService, times(1)).updateTask(argumentCaptor.capture());
		verifyNoMoreInteractions(taskService);
		assertEquals(taskJson, TaskResource.gson.toJson(argumentCaptor.getValue()));
	}

	@Test
	public void testUpdateWithInvalidJsonShouldReturnInternalError() throws Exception {
		mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(taskJson.substring(1).getBytes()))
				.andExpect(status().isInternalServerError());
		verify(taskService, never()).updateTask(argumentCaptor.capture());
		verifyNoMoreInteractions(taskService);
	}

	private Task getTask() {
		return TaskResource.gson.fromJson(taskJson, Task.class);
	}

}
