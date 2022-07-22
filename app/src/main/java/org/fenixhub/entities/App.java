package org.fenixhub.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

@Entity
@Table(name = "app")
public class App {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "archive", nullable = false)
    private String archive;
    
    @Column(name = "developer", nullable = false)
    private String developer;

    @Column(name = "registered_at", nullable = false)
    private Long registeredAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "published", nullable = false)
    private Boolean published;

}
