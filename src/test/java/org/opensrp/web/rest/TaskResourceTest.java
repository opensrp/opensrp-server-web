package org.opensrp.web.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.TaskUpdate;
import org.opensrp.search.TaskSearchBean;
import org.opensrp.service.TaskService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.bean.Identifier;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.it.TestWebContextLoader;
import org.opensrp.web.rest.v2.TaskResourceV2Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.smartregister.domain.Task;
import org.smartregister.domain.Task.TaskPriority;
import org.smartregister.domain.Task.TaskStatus;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.AssertionErrors.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {"classpath:test-webmvc-config.xml",})
public class TaskResourceTest {

    private final String taskJson = "{\"identifier\":\"tsk11231jh22\",\"planIdentifier\":\"IRS_2018_S1\",\"groupIdentifier\":\"2018_IRS-3734\",\"status\":\"Ready\",\"businessStatus\":\"Not Visited\",\"priority\":3,\"code\":\"IRS\",\"description\":\"Spray House\",\"focus\":\"IRS Visit\",\"for\":\"location.properties.uid:41587456-b7c8-4c4e-b433-23a786f742fc\",\"executionStartDate\":\"2018-11-10T2200\",\"executionEndDate\":\"2021-11-10T2200\",\"authoredOn\":\"2018-10-31T0700\",\"lastModified\":\"2018-10-31T0700\",\"owner\":\"demouser\",\"note\":[{\"authorString\":\"demouser\",\"time\":\"2018-01-01T0800\",\"text\":\"This should be assigned to patrick.\"}],\"serverVersion\":15421904649879,\"reasonReference\":\"reasonreferenceuuid\"}";
    private final String taskUpdateJson = "{\"businessStatus\": \"Not Sprayed\", \"identifier\": \"tsk11231jh22\", \"status\": \"completed\" }";
    private final String BASE_URL = "/rest/task/";
    private final ArgumentCaptor<Task> argumentCaptor = ArgumentCaptor.forClass(Task.class);
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HHmm");
    private final org.opensrp.web.rest.v2.TaskResource taskResourceV2 = new org.opensrp.web.rest.v2.TaskResource();
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected ObjectMapper mapper = new ObjectMapper();
    private MockMvc mockMvc;
    @InjectMocks
    private TaskResource taskResource;
    @Mock
    private TaskService taskService;
    @Captor
    private ArgumentCaptor<List<Task>> listArgumentCaptor;
    @Captor
    private ArgumentCaptor<List<TaskUpdate>> taskUpdatelistArguments;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(taskResource)
                .setControllerAdvice(new GlobalExceptionHandler()).addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
                .build();
        taskResourceV2.init();
    }

    @Test
    public void testGetByUniqueId() throws Exception {
        when(taskService.getTask("tsk11231jh22")).thenReturn(getTask());
        MvcResult result = mockMvc.perform(get(BASE_URL + "/{identifier}", "tsk11231jh22")).andExpect(status().isOk())
                .andReturn();
        verify(taskService, times(1)).getTask("tsk11231jh22");
        verifyNoMoreInteractions(taskService);
        JSONAssert.assertEquals(taskJson, result.getResponse().getContentAsString(), JSONCompareMode.STRICT_ORDER);

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
        MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"plan\":[\"IRS_2018_S1\"],\"group\":[\"2018_IRS-3734\"], \"serverVersion\":15421904649873}".getBytes()))
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
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"plan\":[\"IRS_2018_S1\"],\"owner\":\"demouser\", \"serverVersion\":15421904649873}".getBytes()))
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
                .perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"plan\":[\"IRS_2018_S1\"], \"group\":[\"2018_IRS-3734\"], \"serverVersion\":\"\"}".getBytes()))
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
        mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON)
                .content("{\"plan\":\"\", \"serverVersion\":\"\"}".getBytes())).andExpect(status().isBadRequest());
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
        mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"plan\":[\"IRS_2018_S1\"], \"group\":[\"2018_IRS-3734\"], \"serverVersion\":15421904649873}".getBytes()))
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
        assertTask(argumentCaptor.getValue());
    }

    private void assertTask(Task task) {
        assertEquals("tsk11231jh22", task.getIdentifier());
        assertEquals("2018_IRS-3734", task.getGroupIdentifier());
        assertEquals(TaskStatus.READY, task.getStatus());
        assertEquals("Not Visited", task.getBusinessStatus());
        assertEquals(TaskPriority.ROUTINE, task.getPriority());
        assertEquals("IRS", task.getCode());
        assertEquals("Spray House", task.getDescription());
        assertEquals("IRS Visit", task.getFocus());
        assertEquals("location.properties.uid:41587456-b7c8-4c4e-b433-23a786f742fc", task.getForEntity());
        assertEquals("2018-11-10T2200", task.getExecutionPeriod().getStart().toString(formatter));
        assertEquals("2021-11-10T2200", task.getExecutionPeriod().getEnd().toString(formatter));
        assertEquals("2018-10-31T0700", task.getAuthoredOn().toString(formatter));
        assertEquals("2018-10-31T0700", task.getLastModified().toString(formatter));
        assertEquals("demouser", task.getOwner());
        assertEquals(1, task.getNotes().size());
        assertEquals("demouser", task.getNotes().get(0).getAuthorString());
        assertEquals("2018-01-01T0800", task.getNotes().get(0).getTime().toString(formatter));
        assertEquals("This should be assigned to patrick.", task.getNotes().get(0).getText());
        assertEquals("This should be assigned to patrick.", task.getNotes().get(0).getText());
        assertNull(task.getRequester());
        assertNull(task.getLocation());
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
        assertTask(argumentCaptor.getValue());
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
        String content = "[" + taskJson + "]";
        mockMvc.perform(post(BASE_URL + "/add").contentType(MediaType.APPLICATION_JSON).content(content.getBytes()))
                .andExpect(status().isCreated());
        verify(taskService).saveTasks(listArgumentCaptor.capture());
        verifyNoMoreInteractions(taskService);
        assertEquals(1, listArgumentCaptor.getValue().size());
        assertTask(listArgumentCaptor.getValue().get(0));
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
        assertEquals("{\"identifiers\":[\"task-id-1\"],\"lastServerVersion\":12345}",
                result.getResponse().getContentAsString());
        assertEquals((idsModel.getLeft()).get(0), actualTaskIdList.get(0));
        assertEquals(idsModel.getRight(), actualIdModels.getLastServerVersion());
    }

    private Task getTask() {
        return taskResourceV2.gson.fromJson(TaskResourceV2Test.taskJson, Task.class);
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
        List<Task> tasks = Collections.singletonList(expectedTask);
        when(taskService.getAllTasks(anyLong(), anyInt())).thenReturn(tasks);
        MvcResult result = mockMvc.perform(get(BASE_URL + "/getAll?serverVersion=0&limit=25")).andExpect(status().isOk())
                .andReturn();
        verify(taskService).getAllTasks(anyLong(), anyInt());
        JSONAssert.assertEquals("[" + taskJson + "]", result.getResponse().getContentAsString(),
                JSONCompareMode.STRICT_ORDER);

    }

    @Test
    public void testCountAll() throws Exception {
        when(taskService.countAllTasks(anyLong())).thenReturn(1L);
        MvcResult result = mockMvc.perform(get(BASE_URL + "/countAll?serverVersion=0")).andExpect(status().isOk())
                .andReturn();
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
        MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"plan\":[\"IRS_2018_S1\"],\"group\":[\"2018_IRS-3734\"], \"serverVersion\":15421904649873, \"return_count\":true}"
                                .getBytes()))
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
        MvcResult result = mockMvc.perform(post(BASE_URL + "/sync").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"plan\":[\"IRS_2018_S1\"],\"owner\":\"demouser\", \"serverVersion\":15421904649873, \"return_count\":true}"
                                .getBytes()))
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

    @Test
    public void testGetOptionalTasksWithCount() throws Exception {
        List<Task> tasks = new ArrayList<>();
        tasks.add(getTask());
        int totalRecords = 1;
        when(taskService.getTasksBySearchBean(any(TaskSearchBean.class))).thenReturn(tasks);
        when(taskService.findTaskCountBySearchBean(any(TaskSearchBean.class))).thenReturn(totalRecords);
        MvcResult result = mockMvc.perform(get(BASE_URL + "/search")
                        .param("planIdentifier", "d92851b2-e01b-5176-a24c-33635e3fe056")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        verify(taskService, times(1)).getTasksBySearchBean(any(TaskSearchBean.class));
        verify(taskService, times(1)).findTaskCountBySearchBean(any(TaskSearchBean.class));
        verifyNoMoreInteractions(taskService);
        String responseString = result.getResponse().getContentAsString();
        if (responseString.isEmpty()) {
            fail("Test case failed");
        }
        JsonNode actualObj = mapper.readTree(responseString);
        assertEquals(1, actualObj.get("total_records").asInt());
        assertEquals(1, actualObj.get("tasks").size());
    }

}
