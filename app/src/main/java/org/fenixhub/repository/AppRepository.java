package org.fenixhub.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.fenixhub.entities.App;

@ApplicationScoped
public class AppRepository {

    @Inject
    private EntityManager entityManager;

    public App findByName(String name) {
        return entityManager.createNamedQuery("App.findByName", App.class).setParameter("name", name).getSingleResult();
    }

    public void persist(App app) {
        entityManager.persist(app);
    }

    public void update(App app) {
        entityManager.merge(app);
    }

}
