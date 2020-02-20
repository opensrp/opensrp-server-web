package org.opensrp.web.dashboard.util;

import org.opensrp.domain.Practitioner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamMemberWrapper {

	private Practitioner practitioner;
	
	private String username;
	
	private String password;
	
	private String confirmedPassword;
	
	private String email;
	
	private String role;

	private Boolean active;

	public TeamMemberWrapper() { 
		practitioner = new Practitioner();
	}

	public TeamMemberWrapper(Practitioner practitioner, String username, String password, String role) {
		this.practitioner = practitioner;
		this.username = username;
		this.password = password;
		this.role = role;
	}
	
	
}
