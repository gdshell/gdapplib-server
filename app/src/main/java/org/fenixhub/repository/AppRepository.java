package org.fenixhub.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import org.fenixhub.entity.App;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppRepository implements PanacheRepositoryBase<App, Integer> {

    public boolean checkIfExists(String attribute, Object value) {
        return count(attribute, value).map(i -> i > 0).await().indefinitely();
    }

    public Uni<App> findByName(String name) {
        return find("name", name).firstResult();
    }

    public Uni<App> findByArchive(String archive) {
        return find("archive", archive).firstResult();
    }

}
