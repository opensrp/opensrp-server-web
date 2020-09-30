/**
 * 
 */
package org.opensrp.web.exceptions;


/**
 * @author Samuel Githengi created on 09/30/20
 */
public class MissingTeamAssignmentException extends IllegalStateException {

	public MissingTeamAssignmentException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 6550109338327092528L;
	
}
