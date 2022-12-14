package org.fenixhub.utils;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.runtime.annotations.ConfigItem;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.Startup;
import lombok.Getter;

@Getter
@Startup
@ApplicationScoped
public class Configuration {

    @ConfigProperty(name = "app.range.units", defaultValue = "bytes")
    String rangeUnits;

    @ConfigProperty(name = "app.root.folder", defaultValue = "gdapplib")
    String rootFolder;
    
    @ConfigProperty(name = "app.hash.algorithm", defaultValue = "SHA-256")
    String hashAlgorithm;

    @ConfigProperty(name = "app.archive.type", defaultValue = "tar")
    String archiveType;

    @ConfigProperty(name = "app.compression.type", defaultValue = "br")
    String compressionType;
    
    @ConfigProperty(name = "app.archive.chunk.delimiter", defaultValue = "_")
    String archiveChunkDelimiter;

	@ConfigProperty(name = "mp.jwt.verify.issuer")
	String issuer;

	@ConfigProperty(name = "jwt.expiration")
	int expirationTimeJwt;

    @ConfigItem(name = "refreshToken.expiration")
    int expirationTimeRefreshToken;
}
