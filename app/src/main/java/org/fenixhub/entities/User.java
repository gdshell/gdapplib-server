package org.fenixhub.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "user")
public class User {

	@Id 
	@Column(name="id", length=36, updatable = false, nullable = false)
	@GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String id;

    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "registered_at", nullable = false)
    private Integer registeredAt;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @OneToMany(targetEntity = UserRole.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
    private Set<UserRole> roles;

}
