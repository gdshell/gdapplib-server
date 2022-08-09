package org.fenixhub.repository;

import java.math.BigInteger;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fenixhub.entities.App;

@ApplicationScoped
public class AppRepository {

    @Inject EntityManager entityManager;

    public App findById(Integer id) {
        return entityManager.find(App.class, id);
    }

    public boolean checkIfExists(String whereQuery, Map<String, Object> whereValues) {
        Query query = entityManager.createNativeQuery("SELECT COUNT(id) FROM app_mgr.app WHERE " + whereQuery);
        whereValues.forEach((key, value) -> query.setParameter(key, value));
        return !query.getSingleResult().equals(BigInteger.ZERO);
    }

    public App findByName(String name) {
        return (App) entityManager.createNativeQuery("SELECT * FROM app WHERE name = :name", App.class)
        .setParameter("name", name)
        .getSingleResult();
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
