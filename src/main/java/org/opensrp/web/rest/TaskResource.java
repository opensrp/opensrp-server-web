package org.opensrp.web.rest;

import static org.opensrp.web.rest.RestUtils.getStringFilter;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.Task;
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

@Controller
@RequestMapping(value = "/rest/task")
public class TaskResource {

	private static Logger logger = LoggerFactory.getLogger(TaskResource.class.toString());

	public static final String CAMPAIGN = "campaign";

	public static final String GROUP = "group";

	private TaskService taskService;

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	public static Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
			.serializeNulls().create();

	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getByUniqueId(@PathVariable("identifier") String identifier) {
		return new ResponseEntity<>(gson.toJson(taskService.getTask(identifier)), HttpStatus.OK);
	}

	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getTasksByCampaignAndGroup(HttpServletRequest request) {
		String campaign = getStringFilter(CAMPAIGN, request);
		String group = getStringFilter(GROUP, request);
		String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(serverVersion);
		} catch (NumberFormatException e) {
			logger.error("server version not a number");
		}
		return new ResponseEntity<>(
				gson.toJson(taskService.getTasksByCampaignAndGroup(campaign, group, currentServerVersion)),
				HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<HttpStatus> create(@RequestBody String entity) {
		try {
			Task task = gson.fromJson(entity, Task.class);
			taskService.addTask(task);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation" + entity);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<HttpStatus> update(@RequestBody String entity) {
		try {
			Task task = gson.fromJson(entity, Task.class);
			taskService.updateTask(task);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation" + entity);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
