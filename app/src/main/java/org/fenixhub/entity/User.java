package org.fenixhub.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "user")
public class User extends PanacheEntityBase {

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
    private Set<Role> roles;

}
