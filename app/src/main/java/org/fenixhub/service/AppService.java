package org.fenixhub.service;

import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.eclipse.microprofile.jwt.Claims;
import org.fenixhub.dto.AppDto;
import org.fenixhub.entity.App;
import org.fenixhub.mapper.AppMapper;
import org.fenixhub.repository.AppRepository;
import org.fenixhub.utils.Helpers;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@ApplicationScoped
public class AppService {
    
    private static final Logger LOG = Logger.getLogger(AppService.class);
    
    @Inject Helpers helpers;
    
    @Inject AppRepository appRepository;

    @Inject JWTService jwtService;

    @Inject AppMapper appMapper;

    @Transactional
    public boolean checkIfAppExists(Integer appId) {
        return appRepository.checkIfExists("appId", appId);
    }

    @Transactional
    public Uni<AppDto> getApp(Integer appId) {
        if (!checkIfAppExists(appId)) {
            throw new NotFoundException("App not found.");
        }
        
        return appRepository.findById(appId)
                .onItem().transform(app -> appMapper.appToAppDto(app))
                .onFailure().recoverWithNull();
    }

    /*
     * Create a new app.
     * This method will create a new app and store it's root folder in the filesystem.
     * The root folder will be later used to store the app's archive.
     */
    @ReactiveTransactional
    public Uni<AppDto> registerApp(AppDto appDto) {
        if (appRepository.checkIfExists("name", appDto.getName())) {
            throw new BadRequestException("App name already exists.");
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

        return Uni.createFrom().item(appMapper.appToAppDto(app));
    }

    @ReactiveTransactional
    public Uni<AppDto> updateApp(Integer appId, AppDto appDto) {
        return appRepository.findById(appId)
                .onItem().ifNull().failWith(() -> new NotFoundException("App not found."))
                .onItem().ifNotNull()
                .call(
                        Unchecked.function((App app) -> {
                            if (!jwtService.verifyClaim(Claims.sub, app.getDeveloper())) {
                                throw new UnauthorizedException("You are not the developer of this app.");
                            }
                            return Uni.createFrom().item(app);
                        })

                )
                .call(
                    (App app) -> {
                        app.setName(appDto.getName());
                        app.setUpdatedAt(helpers.today.apply(0));
                        return Uni.createFrom().item(app);
                    }
                )
                .map(app -> appMapper.appToAppDto(app));
    }

    public Uni<List<AppDto>> getApps() {
        return appRepository.listAll()
                .onItem().transform(apps -> appMapper.appsToAppDtos(apps))
                .onFailure().recoverWithNull();
    }
}
