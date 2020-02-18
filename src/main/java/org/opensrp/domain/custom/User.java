package org.opensrp.domain.custom;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Hassan Mustafa Baig
 *
 */
@Entity
@Table(name="user_entity", schema = "core")
@Getter
@Setter
@NoArgsConstructor
public class User extends AbstractPersistable<Long>{
	
	private static final long serialVersionUID = 1L;	
	
	@Transient
	private String openTextPassword;
	
	@Column(nullable=false, unique=true, name = "name")
	private String name;
	
	@Column(nullable=false, name = "email")	
	private String email;
	
	@Column(nullable = false, name = "username")
	private String userName;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "token_expired")
	private boolean tokenExpired;
	
	@Column(nullable = false, name = "salt")
	private String salt;
	
	@Column(nullable = false, name = "passhash")
	private String passHash;
	
	@Column(nullable = false, name = "created_on")
	private Date createdOn;
	
	@Column(nullable = false, name = "created_by")
	private String createdBy;
	
	@Column(nullable = true, name = "updated_on")
	private Date updatedOn;
	
	@Column(nullable = true, name = "updated_by")
	private String updatedBy;	
	
	@ManyToMany(cascade=CascadeType.MERGE, fetch = FetchType.EAGER)
    @JoinTable(name="user_roles", schema = "core",
        joinColumns = {@JoinColumn(name="user_id", referencedColumnName="id")},
        inverseJoinColumns = {@JoinColumn(name="role_id", referencedColumnName="id")}
    )
    private Set<Role> roles;
		
	public User(String userName, String email, String passHash, Set<Role> roles) {
        super();
        this.userName = userName;
        this.email = email;
        this.passHash = passHash;
        this.roles = roles;
    }	      
}
