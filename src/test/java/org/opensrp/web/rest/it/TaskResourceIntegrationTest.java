/**
 * 
 */
package org.opensrp.web.rest.it;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.opensrp.service.TaskService;
import org.opensrp.web.GlobalExceptionHandler;
import org.opensrp.web.config.security.filter.CrossSiteScriptingPreventionFilter;
import org.opensrp.web.rest.v2.TaskResource;
import org.smartregister.domain.Task;
import org.smartregister.domain.Task.TaskPriority;
import org.smartregister.domain.Task.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Samuel Githengi created on 11/23/20
 */
public class TaskResourceIntegrationTest {
	
	@Rule
	public MockitoRule rule = MockitoJUnit.rule();
	
	@Autowired
	protected WebApplicationContext webApplicationContext;
	
	private MockMvc mockMvc;
	
	@InjectMocks
	private TaskResource taskResource;
	
	@Autowired
	private TaskService taskService;
	
	private String BASE_URL = "/rest/v2/task/";
	
	private ArgumentCaptor<Task> argumentCaptor = ArgumentCaptor.forClass(Task.class);
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(taskResource)
		        .setControllerAdvice(new GlobalExceptionHandler()).addFilter(new CrossSiteScriptingPreventionFilter(), "/*")
		        .build();
		taskResource.init();//call this manually because @PostConstruct is not called by JUnit
	}
	
	@Test
	public void testCreateBatchShouldGenerateServerVersionWithCorrectOrder() throws Exception {
		List<Task> tasks = getTasks(1);
		tasks.parallelStream().forEach(task -> {
			try {
				mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
				        .content(taskResource.gson.toJson(task).getBytes())).andExpect(status().isCreated());
			}
			catch (Exception e) {
				fail();
			}
		});
		
		verify(taskService, times(tasks.size())).addTask(argumentCaptor.capture());
		verifyNoMoreInteractions(taskService);
	}
	
	private List<Task> getTasks(int count) {
		List<Task> tasks = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Task task = new Task();
			task.setGroupIdentifier("groupIdentifier");
			task.setStatus(TaskStatus.READY);
			task.setPriority(TaskPriority.ROUTINE);
			task.setIdentifier("id" + i);
			tasks.add(task);
		}
		return tasks;
	}
	
}
