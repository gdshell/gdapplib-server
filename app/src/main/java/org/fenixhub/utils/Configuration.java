package org.fenixhub.utils;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import lombok.Getter;

@Getter
@ApplicationScoped
public class Configuration {
    
    @ConfigProperty(name = "app.range.units", defaultValue = "bytes")
    private String rangeUnits;

    @ConfigProperty(name = "app.root.folder", defaultValue = "gdapplib")
    private String rootFolder;
    
    @ConfigProperty(name = "app.hash.algorithm", defaultValue = "SHA-256")
    private String hashAlgorithm;

    @ConfigProperty(name = "app.archive.type", defaultValue = "tar")
    private String archiveType;

    @ConfigProperty(name = "app.compression.type", defaultValue = "br")
    private String compressionType;
}
