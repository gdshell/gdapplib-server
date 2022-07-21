package org.fenixhub.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "app")
@Data
@Builder
public class App {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "developer", nullable = false)
    private String developer;

    @Column(name = "published_at", nullable = false)
    private Long publishedAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

}
