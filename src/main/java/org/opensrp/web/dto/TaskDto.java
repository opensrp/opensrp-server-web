/**
 *
 */
package org.opensrp.web.dto;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.smartregister.domain.Task;

/**
 * @author Samuel Githengi created on 11/18/20
 */
@Getter
@Setter
public class TaskDto extends Task {

    private static final long serialVersionUID = 1801263421730964348L;

    private DateTime executionStartDate;

    private DateTime executionEndDate;

}
