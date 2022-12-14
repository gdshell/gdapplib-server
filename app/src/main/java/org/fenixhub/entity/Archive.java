package org.fenixhub.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "archive")
public class Archive {

	@Id 
	@Column(name="id", length=36, updatable = false, nullable = false)
    private String id;

    @Column(name = "app_id", nullable = false)
    private Integer appId;
    
    @Column(name = "archive", nullable = true)
    private String archive;
    
    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "chunks", nullable = false)
    private Short chunks;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "created_at", nullable = false)
    private Integer createdAt;

    @Column(name = "updated_at", nullable = false)
    private Integer updatedAt;

    @Column(name = "completed", nullable = false)
    private Boolean completed;

}
