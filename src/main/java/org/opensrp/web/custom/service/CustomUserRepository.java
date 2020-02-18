package org.opensrp.web.custom.service;

import java.util.Optional;

import org.opensrp.domain.custom.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomUserRepository extends JpaRepository<User, Integer> {
	Optional<User> findByEmail(String email);	
	Optional<User> findByUserName(String userName);
}
