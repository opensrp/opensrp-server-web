package org.opensrp.web.custom.service;

import java.util.Optional;

import org.opensrp.domain.custom.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Hassan Mustafa Baig
 *
 */
@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Integer>
{
	Optional<Privilege> findByName(String name);

}
