package org.opensrp.web.rest;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.Task;
import org.opensrp.domain.TaskUpdate;
import org.opensrp.service.TaskService;
import org.opensrp.util.TaskDateTimeTypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@Controller
@RequestMapping(value = "/rest/task")
public class TaskResource {

	private static Logger logger = LoggerFactory.getLogger(TaskResource.class.toString());

	public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
			.serializeNulls().create();

	public static final String PLAN = "plan";

	public static final String GROUP = "group";

	private TaskService taskService;

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getByUniqueId(@PathVariable("identifier") String identifier) {
		try {
			return new ResponseEntity<>(gson.toJson(taskService.getTask(identifier)), RestUtils.getJSONUTF8Headers(),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getTasksByTaskAndGroup(HttpServletRequest request) {
		String plan = getStringFilter(PLAN, request);
		String group = getStringFilter(GROUP, request);
		String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(serverVersion);
		} catch (NumberFormatException e) {
			logger.error("server version not a number");
		}
		if (StringUtils.isBlank(plan) || StringUtils.isBlank(group))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		try {
			return new ResponseEntity<>(
					gson.toJson(taskService.getTasksByTaskAndGroup(plan, group, currentServerVersion)),
					RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> create(@RequestBody String entity) {
		try {
			Task task = gson.fromJson(entity, Task.class);
			taskService.addTask(task);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> update(@RequestBody String entity) {
		try {
			Task task = gson.fromJson(entity, Task.class);
			taskService.updateTask(task);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> batchSave(@RequestBody String entity) {
		try {
			Type listType = new TypeToken<List<Task>>() {
			}.getType();
			List<Task> tasks = gson.fromJson(entity, listType);
			Set<String> tasksWithErrors = taskService.saveTasks(tasks);
			if (tasksWithErrors.isEmpty())
				return new ResponseEntity<>("All Tasks  processed", HttpStatus.CREATED);
			else
				return new ResponseEntity<>(
						"Tasks with identifiers not processed: " + String.join(",", tasksWithErrors),
						HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/update_status", method = RequestMethod.POST, consumes = {
			MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> updateStatus(@RequestBody String entity) {
		try {
			Type listType = new TypeToken<List<TaskUpdate>>() {
			}.getType();
			List<TaskUpdate> taskUpdates = gson.fromJson(entity, listType);
			List<String> updateTasks = taskService.updateTaskStatus(taskUpdates);
			if (updateTasks.size() > 0) {
				JSONObject json = new JSONObject();
				json.put("task_ids", updateTasks);
				return new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>("Tasks not Updated: ", HttpStatus.CREATED);
			}
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task update representation" + entity);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
