/**
 *
 */
package org.opensrp.web.rest.v2;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.GsonBuilder;
import org.joda.time.DateTime;
import org.opensrp.web.dto.TaskDto;
import org.smartregister.domain.Task;
import org.smartregister.domain.Task.TaskPriority;
import org.smartregister.utils.TaskDateTimeTypeConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Samuel Githengi created on 11/12/20 Task V2 API that returns {@link TaskPriority} as enum
 * name
 */
@Controller("TaskResourceV2")
@RequestMapping(value = "/rest/v2/task")
public class TaskResource extends org.opensrp.web.rest.TaskResource {

    @PostConstruct
    @VisibleForTesting
    public void init() {
        setGson(new GsonBuilder().registerTypeAdapter(DateTime.class, new TaskDateTimeTypeConverter()).create());
    }

    @Override
    public Task convertToDTO(Task task) {
        return task;
    }

    @Override
    public List<Task> convertToDTO(List<Task> taskList) {
        return taskList;
    }

    @Override
    public Task convertToDomain(TaskDto taskDto) {
        return taskDto;
    }

    @Override
    public List<Task> convertToDomain(List<TaskDto> taskList) {
        return new ArrayList<>(taskList);
    }

}
