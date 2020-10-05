/**
 * 
 */
package org.opensrp.web.bean;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Samuel Githengi created on 09/10/20
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAssignmentBean {
	
	private Set<Long> organizationIds;
	
	private Set<String> jurisdictions;
	
	private Set<String> plans;
	
}
