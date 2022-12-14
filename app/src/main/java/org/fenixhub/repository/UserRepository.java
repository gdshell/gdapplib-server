package org.fenixhub.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import org.fenixhub.entity.User;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;


@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, String> {

    public Uni<Boolean> checkIfUsernameExists(String username) {
        return count("username", username).map(count -> count > 0);
    }

    public Uni<Boolean> checkIfEmailExists(String email) {
        return count("email", email).map(count -> count > 0);
    }

    public Uni<User> findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public Uni<User> findByUsername(String username) {
        return find("username", username).firstResult();
    }

}
