package org.fenixhub.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.fenixhub.entities.Archive;

@ApplicationScoped
public class ArchiveRepository {

    @Inject
    private EntityManager entityManager;

    public Archive findById(String id) {
        return entityManager.find(Archive.class, id);
    }

    public List<Archive> findByAppId(Long appId) {
        return entityManager.createNativeQuery("SELECT * FROM app_mgr.archive WHERE app_id = :appId", Archive.class).setParameter("appId", appId).getResultList();
    }

    public boolean checkIfExists(String whereQuery, Map<String, String> whereValues) {
        Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM app_mgr.archive WHERE " + whereQuery);
        whereValues.forEach((key, value) -> query.setParameter(key, value));
        return !query.getSingleResult().equals(BigInteger.ZERO);
    }

    public void persist(Archive archive) {
        entityManager.persist(archive);
    }

    public void update(Archive archive) {
        entityManager.merge(archive);
    }

}
