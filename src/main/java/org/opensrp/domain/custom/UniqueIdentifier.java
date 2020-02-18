package org.opensrp.domain.custom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Hassan Mustafa Baig
 *
 */

@Entity
@Table(name = "unique_ids", schema = "core")
@Getter
@Setter
@NoArgsConstructor
public class UniqueIdentifier extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "identifier")
	private String identifier;

	@Column(name = "status")
	private String status;

	@Column(name = "used_by")
	private String usedBy;

	@Column(name = "location")
	private String location;

	@Column(name = "created_at", columnDefinition = "DATETIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedAt;

	public UniqueIdentifier(String identifier, String status, String usedBy, String location, Date createdAt,
			Date updatedAt) {
		this.identifier = identifier;
		this.status = status;
		this.usedBy = usedBy;
		this.location = location;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}	
}
