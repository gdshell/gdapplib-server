package org.fenixhub.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

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
public class UserRole implements Serializable {
    
    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Id
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

}
