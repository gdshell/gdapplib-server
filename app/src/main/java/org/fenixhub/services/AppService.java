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

import org.fenixhub.dto.AppDto;
import org.fenixhub.entities.App;
import org.fenixhub.mapper.AppMapper;
import org.fenixhub.repository.AppRepository;
import org.fenixhub.utils.Helpers;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AppService {
    
    private static final Logger LOG = Logger.getLogger(AppService.class);
    
    @Inject
    private Helpers helpers;
    
    @Inject
    private AppRepository appRepository;


    @Transactional
    public boolean checkIfAppExists(Long appId) {
        return appRepository.checkIfExists("id = :id", Map.of("id", appId));
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
        .developer(appDto.getDeveloper())
        .registeredAt(helpers.today.apply(0L))
        .updatedAt(helpers.today.apply(0L))
        .published(false)
        .build();
        appRepository.persist(app);

        try {
            Files.createDirectories(helpers.getPathOfApp(app.getId()));
        } catch (IOException e) {
            throw new InternalServerErrorException("Could not create app folder.", e);
        }

        return AppMapper.INSTANCE.appToAppDto(app);
    }

    public void updateApp(Long appId, AppDto appDto) {
        App app = appRepository.findById(appId);
        if (app == null) {
            throw new NotFoundException("App does not exist.");
        }

        if (appDto.getName() != null) {
            app.setName(appDto.getName());
        }
        if (appDto.getPublished() != null) {
            app.setPublished(appDto.getPublished());
        }

        app.setUpdatedAt(helpers.today.apply(0L));
        
        appRepository.update(app);
    }
}
