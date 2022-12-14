package org.fenixhub.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "user_role")
@IdClass(UserRole.class)
public class UserRole extends PanacheEntityBase implements Serializable {
    
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Id
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

}
