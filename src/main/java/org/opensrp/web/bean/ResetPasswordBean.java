/**
 * 
 */
package org.opensrp.web.bean;

/**
 * @author Samuel Githengi created on 05/04/20
 */

public class ResetPasswordBean {
	
	private String currentPassword;
	
	private String newPassword;
	
	private String confirmation;
	
	public String getCurrentPassword() {
		return currentPassword;
	}
	
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	
	public String getNewPassword() {
		return newPassword;
	}
	
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	public String getConfirmation() {
		return confirmation;
	}
	
	public void setConfirmation(String confirmation) {
		this.confirmation = confirmation;
	}
	
}
