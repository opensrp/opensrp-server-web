package org.opensrp.web.custom.service;

import java.util.List;

import org.opensrp.domain.custom.User;

public interface CustomUserService {
	User findByEmail(String email);
	User findByUserName(String userName);
	User registerUser(User user, List<String> roles);
	User authenticate(String user, String password);
}
