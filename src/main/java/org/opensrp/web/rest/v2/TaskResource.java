/**
 * 
 */
package org.opensrp.web.rest.v2;

import org.joda.time.DateTime;
import org.smartregister.utils.TaskDateTimeTypeConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Samuel Githengi created on 11/12/20
 */
@Controller
@RequestMapping(value = "/rest/v2/task")
public class TaskResource extends org.opensrp.web.rest.TaskResource{
	
	public Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter())
	        .serializeNulls().create();
	
}
