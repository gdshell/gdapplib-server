package org.fenixhub.repository;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.fenixhub.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RoleRepository {

    private static final Logger LOG = LoggerFactory.getLogger(RoleRepository.class);

    @Inject EntityManager entityManager;

    private List<Role> roles;

    void onStart(@Observes StartupEvent ev) {      
        roles = (ArrayList<Role>) entityManager.createQuery("select role from Role role", Role.class).getResultList();
        LOG.info("Roles found: " + roles.size());
    }

    public List<Role> getRoles() {
        return roles.subList(0, roles.size());
    }

    public Role getRoleByName(String role) {
        for (Role r : roles) {
            if (r.getName().equals(role)) {
                return r;
            }
        }
        return null;
    }

    public Role getRoleById(Integer id) {
        for (Role r : roles) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        return null;
    }

}
