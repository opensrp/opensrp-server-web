package org.opensrp.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.TaskUpdate;
import org.opensrp.service.TaskService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.bean.Identifier;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.smartregister.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class TaskResourceV2Test {

    public static String taskJson = "{\"identifier\":\"tsk11231jh22\",\"planIdentifier\":\"IRS_2018_S1\",\"groupIdentifier\":\"2018_IRS-3734\",\"status\":\"Ready\",\"businessStatus\":\"Not Visited\",\"priority\":\"routine\",\"code\":\"IRS\",\"description\":\"Spray House\",\"focus\":\"IRS Visit\",\"for\":\"location.properties.uid:41587456-b7c8-4c4e-b433-23a786f742fc\",\"executionPeriod\":{\"start\":\"2018-11-10T2200\",\"end\":\"2021-11-10T2200\"},\"authoredOn\":\"2018-10-31T0700\",\"lastModified\":\"2018-10-31T0700\",\"owner\":\"demouser\",\"note\":[{\"authorString\":\"demouser\",\"time\":\"2018-01-01T0800\",\"text\":\"This should be assigned to patrick.\"}],\"serverVersion\":15421904649879,\"reasonReference\":\"reasonreferenceuuid\"}";
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Autowired
    protected WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @InjectMocks
    private TaskResource taskResource;
    @Mock
    private TaskService taskService;
    private String taskUpdateJson = "{\"businessStatus\": \"Not Sprayed\", \"identifier\": \"tsk11231jh22\", \"status\": \"completed\" }";

    private String BASE_URL = "/rest/v2/task/";

    private ArgumentCaptor<Task> argumentCaptor = ArgumentCaptor.forClass(Task.class);

    @Captor
    private ArgumentCaptor<List<Task>> listArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<TaskUpdate>> taskUpdatelistArguments;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(taskResource)
                .setControllerAdvice(new GlobalExceptionHandler()).
                        addFilter(new CrossSiteScriptingPreventionFilter(), "/*").build();
        taskResource.init();//call this manually because @PostConstruct is not called by JUnit
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
    public void testGetByUniqueIdShouldReturnServerError() throws Exception {
        when(taskService.getTask("tsk11231jh22")).thenThrow(new RuntimeException());
        mockMvc.perform(get(BASE_URL + "/{identifier}", "tsk11231jh22")).andExpect(status().isInternalServerError());
        verify(taskService, times(1)).getTask("tsk11231jh22");
        verifyNoMoreInteractions(taskService);

    }

    @Test
    public void testGetTasksByPlanAndGroup() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        when(taskService.getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873L)).thenReturn(tasks);
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":[\"IRS_2018_S1\"],\"group\":[\"2018_IRS-3734\"], \"serverVersion\":15421904649873}".getBytes()))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873L);
        verifyNoMoreInteractions(taskService);
        JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jsonreponse.length());
        JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
    }

    @Test
    public void testGetTasksByPlanAndOwner() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        when(taskService.getTasksByPlanAndOwner("IRS_2018_S1", "demouser", 15421904649873L)).thenReturn(tasks);
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":[\"IRS_2018_S1\"],\"owner\":\"demouser\", \"serverVersion\":15421904649873}".getBytes()))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksByPlanAndOwner("IRS_2018_S1", "demouser", 15421904649873L);
        verifyNoMoreInteractions(taskService);
        JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jsonreponse.length());
        JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
    }

    @Test
    public void testGetTasksByTaskAndGroupGetMethod() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        when(taskService.getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l)).thenReturn(tasks);
        MvcResult result = mockMvc
                .perform(get(BASE_URL + "/sync").param(TaskResource.PLAN, "IRS_2018_S1")
                        .param(TaskResource.GROUP, "2018_IRS-3734").param(BaseEntity.SERVER_VERSIOIN, "15421904649873"))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l);
        verifyNoMoreInteractions(taskService);
        JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jsonreponse.length());
        JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
    }

    @Test
    public void testPostTasksByTaskAndGroupWithInvalidServerVersionShouldReturnAllServerVersions() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        when(taskService.getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 0l)).thenReturn(tasks);
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":[\"IRS_2018_S1\"], \"group\":[\"2018_IRS-3734\"], \"serverVersion\":\"\"}".getBytes()))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 0l);
        verifyNoMoreInteractions(taskService);
        JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jsonreponse.length());
        JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
    }

    @Test
    public void testGetTasksByTaskAndGroupWithInvalidServerVersionShouldReturnAllServerVersions() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        when(taskService.getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 0l)).thenReturn(tasks);
        MvcResult result = mockMvc
                .perform(get(BASE_URL + "/sync").param(TaskResource.PLAN, "IRS_2018_S1")
                        .param(TaskResource.GROUP, "2018_IRS-3734").param(BaseEntity.SERVER_VERSIOIN, ""))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 0l);
        verifyNoMoreInteractions(taskService);
        JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jsonreponse.length());
        JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
    }

    @Test
    public void testPostTasksByTaskAndGroupWithoutParamsShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content("{\"plan\":\"\", \"serverVersion\":\"\"}".getBytes()))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).getTasksByTaskAndGroup(anyString(), anyString(), anyLong());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testGetTasksByTaskAndGroupWithoutParamsShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL + "/sync").param(TaskResource.PLAN, "").param(BaseEntity.SERVER_VERSIOIN, ""))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).getTasksByTaskAndGroup(anyString(), anyString(), anyLong());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testPostTasksByTaskAndGroupShouldReturnServerError() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        when(taskService.getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l))
                .thenThrow(new RuntimeException());
        mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                .content("{\"plan\":[\"IRS_2018_S1\"], \"group\":[\"2018_IRS-3734\"], \"serverVersion\":15421904649873}".getBytes()))
                .andExpect(status().isInternalServerError());
        verify(taskService, times(1)).getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l);
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testGetTasksByTaskAndGroupShouldReturnServerError() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        when(taskService.getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l))
                .thenThrow(new RuntimeException());
        mockMvc.perform(get(BASE_URL + "/sync").param(TaskResource.PLAN, "IRS_2018_S1")
                .param(TaskResource.GROUP, "2018_IRS-3734").param(BaseEntity.SERVER_VERSIOIN, "15421904649873"))
                .andExpect(status().isInternalServerError());
        verify(taskService, times(1)).getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873l);
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testCreate() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(taskJson.getBytes()))
                .andExpect(status().isCreated());
        verify(taskService, times(1)).addTask(argumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
        assertEquals(taskJson, taskResource.gson.toJson(argumentCaptor.getValue()));
    }

    @Test
    public void testCreateWithInvalidJsonShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(taskJson.substring(3).getBytes()))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).addTask(argumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testCreateShouldReturnServerError() throws Exception {
        when(taskService.addTask(any(Task.class))).thenThrow(new RuntimeException());
        mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(taskJson.getBytes()))
                .andExpect(status().isInternalServerError());
        verify(taskService).addTask(argumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testUpdate() throws Exception {
        mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(taskJson.getBytes()))
                .andExpect(status().isCreated());
        verify(taskService, times(1)).updateTask(argumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
        assertEquals(taskJson, taskResource.gson.toJson(argumentCaptor.getValue()));
    }

    @Test
    public void testUpdateWithInvalidJsonShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(taskJson.substring(1).getBytes()))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).updateTask(argumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testUpdateShouldReturnServerError() throws Exception {
        when(taskService.updateTask(any(Task.class))).thenThrow(new RuntimeException());
        mockMvc.perform(put(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(taskJson.getBytes()))
                .andExpect(status().isInternalServerError());
        verify(taskService).updateTask(argumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testBatchSave() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON)
                .content(taskResource.gson.toJson(tasks).getBytes())).andExpect(status().isCreated());
        verify(taskService).saveTasks(listArgumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
        assertEquals(1, listArgumentCaptor.getValue().size());
        assertEquals(taskJson, taskResource.gson.toJson(listArgumentCaptor.getValue().get(0)));
    }

    @Test
    public void testBatchSaveWithInvalidJsonShouldReturnBadRequest() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON)
                .content(taskResource.gson.toJson(tasks).substring(1).getBytes())).andExpect(status().isBadRequest());
        verify(taskService, never()).saveTasks(listArgumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testBatchSaveWithErrorShouldReturnServerError() throws Exception {
        doThrow(new RuntimeException()).when(taskService).saveTasks(anyList());
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON)
                .content(taskResource.gson.toJson(tasks).getBytes())).andExpect(status().isInternalServerError());
        verify(taskService).saveTasks(listArgumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
    }

    @Test
    public void testFindAllTaskIds() throws Exception {
        Pair<List<String>, Long> idsModel = Pair.of(Collections.singletonList("task-id-1"), 12345l);
        when(taskService.findAllTaskIds(anyLong(), anyInt(), isNull(), isNull())).thenReturn(idsModel);
        MvcResult result = mockMvc.perform(get(BASE_URL + "/findIds?serverVersion=0", "")).andExpect(status().isOk())
                .andReturn();

        String actualTaskIdString = result.getResponse().getContentAsString();
        Identifier actualIdModels = new Gson().fromJson(actualTaskIdString, new TypeToken<Identifier>() {
        }.getType());
        List<String> actualTaskIdList = actualIdModels.getIdentifiers();

        verify(taskService, times(1)).findAllTaskIds(anyLong(), anyInt(), isNull(), isNull());
        verifyNoMoreInteractions(taskService);
        assertEquals("{\"identifiers\":[\"task-id-1\"],\"lastServerVersion\":12345}", result.getResponse().getContentAsString());
        assertEquals((idsModel.getLeft()).get(0), actualTaskIdList.get(0));
        assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());
    }

    private Task getTask() {
        return taskResource.gson.fromJson(taskJson, Task.class);
    }

    private TaskUpdate getTaskUpdates() {
        return new Gson().fromJson(taskUpdateJson, TaskUpdate.class);
    }

    @Test
    public void testUpdateStatus() throws Exception {
        List<TaskUpdate> taskUpdates = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        TaskUpdate taskUpdate = getTaskUpdates();
        taskUpdate.setServerVersion(System.currentTimeMillis());
        ids.add(taskUpdate.getIdentifier());
        taskUpdates.add(taskUpdate);

        mockMvc.perform(post(BASE_URL + "/update_status").contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(taskUpdates).getBytes())).andExpect(status().isCreated());
        verify(taskService).updateTaskStatus(taskUpdatelistArguments.capture());

        verifyNoMoreInteractions(taskService);
        assertEquals(1, taskUpdatelistArguments.getValue().size());
        assertEquals(taskUpdate.getIdentifier(), taskUpdatelistArguments.getValue().get(0).getIdentifier());
        assertEquals(ids.get(0), taskUpdatelistArguments.getValue().get(0).getIdentifier());
    }


    @Test
    public void testGetAll() throws Exception {

        Task expectedTask = getTask();

        List<Task> planDefinitions = Collections.singletonList(expectedTask);
        when(taskService.getAllTasks(anyLong(), anyInt()))
                .thenReturn(planDefinitions);
        MvcResult result = mockMvc
                .perform(get(BASE_URL + "/getAll?serverVersion=0&limit=25"))
                .andExpect(status().isOk()).andReturn();
        verify(taskService).getAllTasks(anyLong(), anyInt());
        assertEquals(taskResource.gson.toJson(planDefinitions), result.getResponse().getContentAsString());

    }

    @Test
    public void testCountAll() throws Exception {
        when(taskService.countAllTasks(anyLong()))
                .thenReturn(1L);
        MvcResult result = mockMvc
                .perform(get(BASE_URL + "/countAll?serverVersion=0"))
                .andExpect(status().isOk()).andReturn();
        verify(taskService).countAllTasks(anyLong());
        assertEquals(1, new JSONObject(result.getResponse().getContentAsString()).optInt("count"));
    }

    @Test
    public void testGetTasksByPlanAndGroupWithReturnCount() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        long totalRecords = 3l;
        when(taskService.getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873L)).thenReturn(tasks);
        when(taskService.countTasksByPlanAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873L)).thenReturn(totalRecords);
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":[\"IRS_2018_S1\"],\"group\":[\"2018_IRS-3734\"], \"serverVersion\":15421904649873, \"return_count\":true}".getBytes()))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksByTaskAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873L);
        verify(taskService, times(1)).countTasksByPlanAndGroup("IRS_2018_S1", "2018_IRS-3734", 15421904649873L);
        verifyNoMoreInteractions(taskService);
        JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jsonreponse.length());
        JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
        Long actualTotalRecords = Long.parseLong(result.getResponse().getHeader("total_records"));
        assertEquals(totalRecords, actualTotalRecords.longValue());
    }

    @Test
    public void testGetTasksByPlanAndOwnerWithReturnCount() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        long totalRecords = 5l;
        when(taskService.getTasksByPlanAndOwner("IRS_2018_S1", "demouser", 15421904649873L)).thenReturn(tasks);
        when(taskService.countTasksByPlanAndOwner("IRS_2018_S1", "demouser", 15421904649873L)).thenReturn(totalRecords);
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"plan\":[\"IRS_2018_S1\"],\"owner\":\"demouser\", \"serverVersion\":15421904649873, \"return_count\":true}".getBytes()))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksByPlanAndOwner("IRS_2018_S1", "demouser", 15421904649873L);
        verify(taskService, times(1)).countTasksByPlanAndOwner("IRS_2018_S1", "demouser", 15421904649873L);
        verifyNoMoreInteractions(taskService);
        JSONArray jsonreponse = new JSONArray(result.getResponse().getContentAsString());
        assertEquals(1, jsonreponse.length());
        JSONAssert.assertEquals(taskJson, jsonreponse.get(0).toString(), JSONCompareMode.STRICT_ORDER);
        Long actualTotalRecords = Long.parseLong(result.getResponse().getHeader("total_records"));
        assertEquals(totalRecords, actualTotalRecords.longValue());
    }

}
