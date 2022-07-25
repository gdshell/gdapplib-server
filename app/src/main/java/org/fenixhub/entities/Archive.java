package org.fenixhub.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "archive")
public class Archive {

	@Id 
	@Column(name="id", length=36, updatable = false, nullable = false)
    private String id;

    @Column(name = "app_id", nullable = false)
    private Long appId;
    
    @Column(name = "archive", nullable = true)
    private String archive;
    
    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "chunks", nullable = false)
    private Long chunks;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "created_at", nullable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

}
