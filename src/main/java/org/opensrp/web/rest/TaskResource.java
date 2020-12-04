package org.opensrp.web.rest;

import static org.opensrp.common.AllConstants.OpenSRPEvent.Form.SERVER_VERSION;
import static org.opensrp.web.Constants.DEFAULT_GET_ALL_IDS_LIMIT;
import static org.opensrp.web.Constants.DEFAULT_LIMIT;
import static org.opensrp.web.Constants.LIMIT;
import static org.opensrp.web.Constants.RETURN_COUNT;
import static org.opensrp.web.Constants.TOTAL_RECORDS;
import static org.opensrp.web.rest.RestUtils.getStringFilter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.opensrp.common.AllConstants.BaseEntity;
import org.opensrp.domain.TaskUpdate;
import org.opensrp.search.TaskSearchBean;
import org.opensrp.service.TaskService;
import org.opensrp.web.bean.Identifier;
import org.opensrp.web.dto.TaskDto;
import org.opensrp.web.dto.TaskSyncRequestWrapper;
import org.opensrp.web.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartregister.domain.Period;
import org.smartregister.domain.Task;
import org.smartregister.domain.Task.TaskPriority;
import org.smartregister.utils.PriorityOrdinalConverter;
import org.smartregister.utils.TaskDateTimeTypeConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Task V1 API that returns {@link TaskPriority} as enum ordinal and execution collapsed
 */
@Controller
@Primary
@Validated
@RequestMapping(value = "/rest/task")
public class TaskResource {
	
	private static Logger logger = LoggerFactory.getLogger(TaskResource.class.toString());
	
	public Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
	        .registerTypeAdapter(TaskPriority.class, new PriorityOrdinalConverter()).create();
	
	public static final String PLAN = "plan";
	
	public static final String GROUP = "group";
	
	public static final String OWNER = "owner";
	
	private TaskService taskService;
	
	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
	
	/**
	 * @param gson the gson to set
	 */
	public void setGson(Gson gson) {
		this.gson = gson;
	}
	
	@RequestMapping(value = "/{identifier}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getByUniqueId(@PathVariable("identifier") String identifier) {
		return new ResponseEntity<>(gson.toJson(convertToDTO(taskService.getTask(identifier))),
		        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/sync", method = RequestMethod.POST, consumes = {
	        MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getTasksByTaskAndGroup(@RequestBody TaskSyncRequestWrapper taskSyncRequestWrapper) {
		String plan = StringUtils.join(taskSyncRequestWrapper.getPlan(), ",");
		String group = StringUtils.join(taskSyncRequestWrapper.getGroup(), ",");
		String owner = taskSyncRequestWrapper.getOwner();
		long serverVersion = taskSyncRequestWrapper.getServerVersion();
		boolean returnCount = taskSyncRequestWrapper.isReturnCount();
		
		long currentServerVersion = 0;
		try {
			currentServerVersion = serverVersion;
		}
		catch (NumberFormatException e) {
			logger.error("server version not a number");
		}
		return getTaskSyncResponse(plan, group, owner, currentServerVersion, returnCount);
	}
	
	// here for backward compatibility
	@RequestMapping(value = "/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getTasksByTaskAndGroupTwo(HttpServletRequest request) {
		String plan = getStringFilter(PLAN, request);
		String group = getStringFilter(GROUP, request);
		String serverVersion = getStringFilter(BaseEntity.SERVER_VERSIOIN, request);
		String owner = getStringFilter(OWNER, request);
		boolean returnCount = Boolean.getBoolean(getStringFilter(RETURN_COUNT, request));
		
		long currentServerVersion = 0;
		try {
			currentServerVersion = Long.parseLong(serverVersion);
		}
		catch (NumberFormatException e) {
			logger.error("server version not a number");
		}
		return getTaskSyncResponse(plan, group, owner, currentServerVersion, returnCount);
	}
	
	private ResponseEntity<String> getTaskSyncResponse(String plan, String group, String owner, long currentServerVersion,
	        boolean returnCount) {
		if (StringUtils.isBlank(plan)) {
			logger.error("Plan Identifier is missing");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		if (!StringUtils.isBlank(group)) {
			String tasks = gson.toJson(convertToDTO(taskService.getTasksByTaskAndGroup(plan, group, currentServerVersion)));
			HttpHeaders headers = RestUtils.getJSONUTF8Headers();
			if (returnCount) {
				Long taskCount = taskService.countTasksByPlanAndGroup(plan, group, currentServerVersion);
				headers.add(TOTAL_RECORDS, String.valueOf(taskCount));
			}
			
			return new ResponseEntity<>(tasks, headers, HttpStatus.OK);
		} else if (!StringUtils.isBlank(owner)) {
			String tasks = gson.toJson(convertToDTO(taskService.getTasksByPlanAndOwner(plan, owner, currentServerVersion)));
			HttpHeaders headers = RestUtils.getJSONUTF8Headers();
			if (returnCount) {
				Long taskCount = taskService.countTasksByPlanAndOwner(plan, owner, currentServerVersion);
				headers.add(TOTAL_RECORDS, String.valueOf(taskCount));
			}
			
			return new ResponseEntity<>(tasks, headers, HttpStatus.OK);
		} else {
			logger.error("Either owner or group identifier field is missing");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> create(@RequestBody String entity) {
		try {
			Task task = convertToDomain(gson.fromJson(entity, TaskDto.class));
			taskService.addTask(task);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@RequestMapping(method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<HttpStatus> update(@RequestBody String entity) {
		try {
			Task task = convertToDomain(gson.fromJson(entity, TaskDto.class));
			taskService.updateTask(task);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
	        MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> batchSave(@RequestBody String entity) {
		try {
			Type listType = new TypeToken<List<TaskDto>>() {}.getType();
			List<TaskDto> tasks = gson.fromJson(entity, listType);
			Set<String> tasksWithErrors = taskService.saveTasks(convertToDomain(tasks));
			if (tasksWithErrors.isEmpty())
				return new ResponseEntity<>("All Tasks  processed", HttpStatus.CREATED);
			else
				return new ResponseEntity<>("Tasks with identifiers not processed: " + String.join(",", tasksWithErrors),
				        HttpStatus.CREATED);
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task representation", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@RequestMapping(value = "/update_status", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE,
	        MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> updateStatus(@RequestBody String entity) {
		try {
			Type listType = new TypeToken<List<TaskUpdate>>() {}.getType();
			List<TaskUpdate> taskUpdates = gson.fromJson(entity, listType);
			List<String> updateTasks = taskService.updateTaskStatus(taskUpdates);
			if (updateTasks.size() > 0) {
				JSONObject json = new JSONObject();
				json.put("task_ids", updateTasks);
				return new ResponseEntity<>(json.toString(), HttpStatus.CREATED);
			} else {
				return new ResponseEntity<>("Tasks not Updated: ", HttpStatus.CREATED);
			}
		}
		catch (JsonSyntaxException e) {
			logger.error("The request doesnt contain a valid task update representation", e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	
	/**
	 * This methods provides an API endpoint that searches for all task Ids ordered by server
	 * version ascending
	 *
	 * @param serverVersion serverVersion using to filter by
	 * @return A list of task Ids
	 */
	@RequestMapping(value = "/findIds", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Identifier> findIds(@RequestParam(value = SERVER_VERSION) long serverVersion,
	        @RequestParam(value = "fromDate", required = false) String fromDate,
	        @RequestParam(value = "toDate", required = false) String toDate) {
		
		Pair<List<String>, Long> taskIdsPair = taskService.findAllTaskIds(serverVersion, DEFAULT_GET_ALL_IDS_LIMIT,
		    Utils.getDateTimeFromString(fromDate), Utils.getDateTimeFromString(toDate));
		Identifier identifiers = new Identifier();
		identifiers.setIdentifiers(taskIdsPair.getLeft());
		identifiers.setLastServerVersion(taskIdsPair.getRight());
		return new ResponseEntity<>(identifiers, HttpStatus.OK);
	}
	
	/**
	 * Fetch tasks ordered by serverVersion ascending
	 *
	 * @param serverVersion serverVersion using to filter by
	 * @param limit upper limit on number of tasks to fetch
	 * @return A list of tasks
	 */
	@RequestMapping(value = "/getAll", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getAll(@RequestParam(value = SERVER_VERSION) long serverVersion,
	        @RequestParam(value = LIMIT, required = false) Integer limit) {
		
		Integer pageLimit = limit == null ? DEFAULT_LIMIT : limit;
		return new ResponseEntity<>(gson.toJson(convertToDTO(taskService.getAllTasks(serverVersion, pageLimit))),
		        RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
		
	}
	
	/**
	 * Fetch count of tasks
	 *
	 * @param serverVersion serverVersion using to filter by
	 * @return A list of tasks
	 */
	@RequestMapping(value = "/countAll", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<ModelMap> countAll(@RequestParam(value = SERVER_VERSION) long serverVersion) {
		Long countOfTasks = taskService.countAllTasks(serverVersion);
		ModelMap modelMap = new ModelMap();
		modelMap.put("count", countOfTasks != null ? countOfTasks : 0);
		return new ResponseEntity<>(modelMap, RestUtils.getJSONUTF8Headers(), HttpStatus.OK);
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET,
			produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<String> getOptionalTasksWithCount(@Valid TaskSearchBean taskSearchBean) {

		HttpHeaders headers = RestUtils.getJSONUTF8Headers();
		int taskCount = taskService.findTaskCountBySearchBean(taskSearchBean);
		headers.add(TOTAL_RECORDS, String.valueOf(taskCount));
		List<Task> tasks;
		if (taskSearchBean != null && taskSearchBean.isReturnTaskCountOnly()) {
			tasks = new ArrayList<>();
		} else {
			tasks = taskService.getTasksBySearchBean(taskSearchBean);
		}

		return new ResponseEntity<>(gson.toJson(tasks), headers, HttpStatus.OK);
	}
	
	/**
	 * Converts a Task to DTO object so that data model for V1 API is maintained
	 * @param task the task to convert
	 * @return TaskDTO v1 task contract
	 */
	public Task convertToDTO(Task task) {
		TaskDto taskDto = new TaskDto();
		BeanUtils.copyProperties(task, taskDto);
		if (task.getExecutionPeriod() != null) {
			taskDto.setExecutionStartDate(task.getExecutionPeriod().getStart());
			taskDto.setExecutionEndDate(task.getExecutionPeriod().getEnd());
		}
		taskDto.setExecutionPeriod(null);
		return taskDto;
	}
	
	/**
	 * Converts a list of Tasks to DTO objects so that data model for V1 API is maintained
	 * @param list of  tasks to convert
	 * @return list of TaskDTO objects
	 */
	public List<Task> convertToDTO(List<Task> taskList) {
		return taskList.stream().map(t -> convertToDTO(t)).collect(Collectors.toList());
	}
	
	/**
	 * Converts a  TaskDTO to domain object for persistence
	 * @param  the TaskDTO object to convert
	 * @return the converted task domain objects
	 */
	public Task convertToDomain(TaskDto taskDto) {
		Task task = new Task();
		BeanUtils.copyProperties(taskDto, task);
		if(taskDto.getExecutionStartDate()!=null || taskDto.getExecutionEndDate()!=null) {
			task.setExecutionPeriod(new Period(taskDto.getExecutionStartDate(),taskDto.getExecutionEndDate()));
		}
		return task;
	}
	
	/**
	 * Converts a list of TaskDTO to domain objects for persistence
	 * @param list of  TaskDTO objects to convert
	 * @return list of converted task domain objects
	 */
	public List<Task> convertToDomain(List<TaskDto> taskList) {
		return taskList.stream().map(t -> convertToDomain(t)).collect(Collectors.toList());
	}

//	private TaskSearchBean createTaskSearchBean(TaskSearchCriteria taskSearchCriteria) {
//		TaskSearchBean taskSearchBean = new TaskSearchBean();
//		if(taskSearchCriteria != null) {
//			taskSearchBean.setPlanIdentifier(taskSearchCriteria.getPlanIdentifier());
//			taskSearchBean.setGroupIdentifiers(taskSearchCriteria.getGroupIdentifiers());
//			taskSearchBean.setCode(taskSearchCriteria.getCode());
//			taskSearchBean.setStatus(taskSearchCriteria.getStatus());
//			taskSearchBean.setBusinessStatus(taskSearchCriteria.getBusinessStatus());
//			taskSearchBean.setPageNumber(taskSearchCriteria.getPageNumber());
//			taskSearchBean.setPageSize(taskSearchCriteria.getPageSize());
//			if(taskSearchCriteria.getOrderByType() != null) {
//				taskSearchBean.setOrderByType(TaskSearchBean.OrderByType.valueOf(taskSearchCriteria.getOrderByType()));
//			}
//			if(taskSearchCriteria.getOrderByFieldName() != null) {
//				taskSearchBean.setOrderByFieldName(TaskSearchBean.FieldName.valueOf(taskSearchCriteria.getOrderByFieldName()));
//			}
//		}
//
//		return taskSearchBean;
//	}
	
}
