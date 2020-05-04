/**
 * 
 */
package org.opensrp.web.bean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Samuel Githengi created on 05/04/20
 */
@Getter
@Setter
public class ResetPasswordBean {
	
	private String currentPassword;
	
	private String newPassword;
	
	private String confirmation;
	
}
