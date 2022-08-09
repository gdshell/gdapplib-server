package org.fenixhub.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.fenixhub.entities.User;

@ApplicationScoped
public class UserRepository {
    
    @Inject EntityManager entityManager;

    public void persist(User user) {
        entityManager.persist(user);
    }

    public void update(User user) {
        entityManager.merge(user);
    }

    public User findById(String id) {
        return entityManager.find(User.class, id);
    }

    public User findByEmail(String email) {
        List<User> rUsers = entityManager.createNativeQuery("SELECT * FROM app_mgr.user WHERE email = :email", User.class).setParameter("email", email).getResultList();
        if (rUsers.size() > 0) {
            return rUsers.get(0);
        } else {
            return null;
        }
    }

    public User findByUsername(String username) {
        List<User> rUsers = entityManager.createNativeQuery("SELECT * FROM app_mgr.user WHERE username = :username", User.class).setParameter("username", username).getResultList();
        if (rUsers.size() > 0) {
            return rUsers.get(0);
        } else {
            return null;
        }
    }

    public List<UserRoleRepository> findRoleForId(String userId) {
        List<UserRoleRepository> rUsers = entityManager.createNativeQuery("SELECT * FROM app_mgr.user_role WHERE user_id = :userId", UserRoleRepository.class)
        .setParameter("userId", userId)
        .getResultList();
        if (rUsers.size() > 0) {
            return rUsers;
        } else {
            return null;
        }
    }

}
