package org.fenixhub.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.fenixhub.entities.App;

@ApplicationScoped
public class AppRepository {

    @Inject
    private EntityManager entityManager;

    public App findById(Long id) {
        return entityManager.find(App.class, id);
    }

    public App findByName(String name) {
        return entityManager.createNamedQuery("App.findByName", App.class).setParameter("name", name).getSingleResult();
    }

    public App findByArchive(String archive) {
        return entityManager.createNamedQuery("App.findByArchive", App.class).setParameter("archive", archive).getSingleResult();
    }

    public void persist(App app) {
        entityManager.persist(app);
    }

    public void update(App app) {
        entityManager.merge(app);
    }

}
