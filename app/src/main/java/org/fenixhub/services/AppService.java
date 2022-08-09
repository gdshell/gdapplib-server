package org.fenixhub.services;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

import org.eclipse.microprofile.jwt.Claims;
import org.fenixhub.dto.AppDto;
import org.fenixhub.entities.App;
import org.fenixhub.mapper.AppMapper;
import org.fenixhub.repository.AppRepository;
import org.fenixhub.utils.Helpers;
import org.jboss.logging.Logger;

import io.quarkus.security.UnauthorizedException;

@ApplicationScoped
public class AppService {
    
    private static final Logger LOG = Logger.getLogger(AppService.class);
    
    @Inject Helpers helpers;
    
    @Inject AppRepository appRepository;

    @Inject JWTService jwtService;

    @Inject AppMapper appMapper;

    @Transactional
    public boolean checkIfAppExists(Integer appId) {
        return appRepository.checkIfExists("id = :id", Map.of("id", appId));
    }

    @Transactional
    public AppDto getApp(Integer appId) {
        if (!checkIfAppExists(appId)) {
            throw new NotFoundException("App not found.");
        }
        
        return appMapper.appToAppDto(appRepository.findById(appId));
    }

    /*
     * Create a new app.
     * This method will create a new app and store it's root folder in the filesystem.
     * The root folder will be later used to store the app's archive.
     */
    @Transactional
    public AppDto registerApp(AppDto appDto) {
        if (appRepository.checkIfExists("name = :name", Map.of("name", appDto.getName()))) {
            throw new BadRequestException("App already exists.");
        }

        App app = App.builder()
        .name(appDto.getName())
        .developer(jwtService.getClaim(Claims.sub))
        .registeredAt(helpers.today.apply(0))
        .updatedAt(helpers.today.apply(0))
        .published(false)
        .build();
        appRepository.persist(app);

        try {
            Files.createDirectories(helpers.getPathOfApp(app.getId()));
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not create app folder.", e);
        }

        return appMapper.appToAppDto(app);
    }

    @Transactional
    public void updateApp(Integer appId, AppDto appDto) {
        App app = appRepository.findById(appId);
        if (app == null) {
            throw new NotFoundException("App does not exist.");
        }

        if (!jwtService.verifyClaim(Claims.sub, app.getDeveloper())) {
            throw new UnauthorizedException("You are not the developer of this app.");
        }
        
        if (appDto.getName() != null) {
            app.setName(appDto.getName());
        }
        if (appDto.getPublished() != null) {
            app.setPublished(appDto.getPublished());
        }

        app.setUpdatedAt(helpers.today.apply(0));
        
        appRepository.update(app);
    }
}
