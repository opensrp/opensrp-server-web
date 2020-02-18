package org.opensrp.domain.custom;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Hassan Mustafa Baig
 *
 */
@Entity
@Table(name="roles", schema = "core")
@Getter
@Setter
@NoArgsConstructor
public class Role extends AbstractPersistable<Long> {

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
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@ManyToMany(cascade=CascadeType.ALL)
	@JoinTable(
	      name="role_privileges", schema = "core",
	      joinColumns={@JoinColumn(name="role_id", referencedColumnName="id")},
	      inverseJoinColumns={@JoinColumn(name="privilege_id", referencedColumnName="id")})
	private Set<Privilege> privileges;	

    public Role(String name) {
        this.name = name;
    }   
}
