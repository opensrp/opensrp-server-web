package org.opensrp.web.rest.shadow;

import org.opensrp.service.TaskService;
import org.opensrp.web.rest.TaskResource;

public class TaskResourceShadow extends TaskResource {

	@Override
	public void setTaskService(TaskService taskService) {
		super.setTaskService(taskService);
	}

}
