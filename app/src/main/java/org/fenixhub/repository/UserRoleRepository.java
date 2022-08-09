package org.fenixhub.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.fenixhub.entities.UserRole;

@ApplicationScoped
public class UserRoleRepository {
    
    @Inject EntityManager entityManager;

    public void persist(UserRole userRole) {
        entityManager.persist(userRole);
    }

    public List<UserRole> getRolesByUserId(String userId) {
        return entityManager.createNativeQuery("SELECT * FROM app_mgr.user_role WHERE user_id = :userId", UserRole.class)
        .setParameter("userId", userId)
        .getResultList();
    }

}
