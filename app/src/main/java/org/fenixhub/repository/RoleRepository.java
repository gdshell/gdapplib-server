package org.fenixhub.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import org.fenixhub.entity.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.List;

@ApplicationScoped
public class RoleRepository implements PanacheRepositoryBase<Role, Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(RoleRepository.class);

    void onStart(@Observes StartupEvent ev) {
        Uni<List<Role>> roles = listAll();
        LOG.info("Roles found: " + roles);
    }

    public Uni<Role> findByName(String name) {
        return find("name", name).firstResult();
    }

}
