package org.fenixhub.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<Archive> findByAppId(Integer appId) {
        return entityManager.createNativeQuery("SELECT * FROM app_mgr.archive WHERE app_id = :appId", Archive.class).setParameter("appId", appId).getResultList();
    }

    public List<Archive> findByParams(Map<String, Object> whereValues) {
        String stringQuery = "SELECT archive FROM Archive archive WHERE ";
        List<String> whereKeys = whereValues.keySet().stream().map(key -> key + " = :" + key).collect(Collectors.toList());
        stringQuery += String.join(" AND ", whereKeys);
        Query query = entityManager.createQuery(stringQuery, Archive.class);
        whereValues.forEach((key, value) -> query.setParameter(key, value));
        return query.getResultList();
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

    public void setCompleted(Archive archive, boolean b) {
        archive.setCompleted(b);
        update(archive);
    }

}
