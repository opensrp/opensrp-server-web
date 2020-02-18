package org.opensrp.web.custom.service;

import java.util.Optional;

import org.opensrp.domain.custom.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Hassan Mustafa Baig
 *
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>
{
	Optional<Role> findByName(String name);

}
