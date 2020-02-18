package org.opensrp.web.custom.service;

import java.util.Optional;

import org.opensrp.domain.custom.UniqueIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UniqueIdentifierRepository extends JpaRepository<UniqueIdentifier, Long> {
	Optional<UniqueIdentifier> findById(Long id);	
	@Query(nativeQuery = true, value="SELECT * FROM core.unique_ids order by id desc LIMIT 1")
	UniqueIdentifier findUniqueIdentifierOrderByIdDesc();	
}
