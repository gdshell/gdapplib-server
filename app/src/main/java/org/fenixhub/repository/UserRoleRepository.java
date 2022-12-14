package org.fenixhub.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import org.fenixhub.entity.UserRole;

@ApplicationScoped
public class UserRoleRepository implements PanacheRepositoryBase<UserRole, UserRole> {

    public Uni<List<UserRole>> getRolesOfUserId(String userId) {
        return find("userId", userId).list();
    }

}
