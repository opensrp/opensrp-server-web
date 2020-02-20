package org.opensrp.web.custom.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.domain.custom.Role;
import org.opensrp.domain.custom.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserServiceImpl implements CustomUserService {

	@Autowired
	CustomUserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	@Override
	public User findByUserName(String userName) {
		return userRepository.findByUserName(userName).orElse(null);
	}
	
	@Override
	public User authenticate(String username, String password) {
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			User user = userRepository.findByUserName(username).orElse(null);
			if (user != null) {
				String generatedSecuredPasswordHash = BCrypt.hashpw(password, user.getSalt());
				if (user.getPassHash().equals(generatedSecuredPasswordHash)) {
					return user;
				} 
			} 
		} 
		
		return null;
	}

	@Transactional
	@Override
	public User registerUser(User userDto, List<String> assignedRoles) {
		User oldUser = userRepository.findByUserName(userDto.getUserName()).orElse(null);

		if (oldUser != null) {
			throw new IllegalArgumentException("User already exists");
		}
		
		User user = new User();
		user.setName(userDto.getName());
		user.setUserName(userDto.getUserName());
		user.setEmail(userDto.getEmail());
		user.setActive(true);
		user.setCreatedOn(new Date());
		user.setCreatedBy(userDto.getCreatedBy());
		user.setTokenExpired(false);

		Set<Role> roles = new HashSet<>();
		
		for (String role : assignedRoles) {
			roles.add(roleRepository.findByName(role).orElse(null));
		}

		user.setRoles(roles);

		String salt = BCrypt.gensalt(12);
		String generatedSecuredPasswordHash = BCrypt.hashpw(userDto.getOpenTextPassword(), salt);

		user.setSalt(salt);
		user.setPassHash(generatedSecuredPasswordHash);		
		
		return userRepository.save(user);
	}
}
