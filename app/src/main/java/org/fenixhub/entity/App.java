package org.fenixhub.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "app")
public class App extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "developer", nullable = false)
    private String developer;

    @Column(name = "registered_at", nullable = false)
    private Integer registeredAt;

    @Column(name = "updated_at", nullable = false)
    private Integer updatedAt;

    @Column(name = "published", nullable = false)
    private Boolean published;

}
