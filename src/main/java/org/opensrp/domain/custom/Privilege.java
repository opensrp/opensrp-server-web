package org.opensrp.domain.custom;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Hassan Mustafa Baig
 *
 */
@Entity
@Table(name="privileges", schema = "core")
@Getter
@Setter
@NoArgsConstructor
public class Privilege extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;	

	@Column(nullable=false, unique=true, name = "name")
	private String name;
	
	@Column(nullable=true, name = "description")	
	private String description;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(nullable = false, name = "created_on")
	private Date createdOn;
	
	@Column(nullable = false, name = "created_by")
	private String createdBy;
	
	@Column(nullable = true, name = "updated_on")
	private Date updatedOn;
	
	@Column(nullable = true, name = "updated_by")
	private String updatedBy;	

    public Privilege(String name) {
        this.name = name;
    }    
}
