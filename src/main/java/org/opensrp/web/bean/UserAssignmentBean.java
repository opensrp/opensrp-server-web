/**
 * 
 */
package org.opensrp.web.bean;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

/**
 * @author Samuel Githengi created on 09/10/20
 */
@Builder
@Data
public class UserAssignmentBean {
	
	private Set<Long> organizationIds;
	
	private Set<String> jurisdictions;
	
	private Set<String> plans;
	
}
