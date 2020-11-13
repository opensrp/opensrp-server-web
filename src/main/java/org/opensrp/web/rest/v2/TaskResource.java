/**
 * 
 */
package org.opensrp.web.rest.v2;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.smartregister.domain.Task.TaskPriority;
import org.smartregister.utils.TaskDateTimeTypeConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.GsonBuilder;

/**
 * @author Samuel Githengi created on 11/12/20 Task V2 API that returns {@link TaskPriority} as enum
 *         name
 */
@Controller
@RequestMapping(value = "/rest/v2/task")
public class TaskResource extends org.opensrp.web.rest.TaskResource {
	
	@PostConstruct
	@VisibleForTesting
	protected void init() {
		setGson(new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter()).create());
	}
	
}
