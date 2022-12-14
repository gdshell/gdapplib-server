package org.fenixhub.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import org.fenixhub.entity.RefreshToken;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class RefreshTokenRepository implements PanacheRepositoryBase<RefreshToken, String> {

    public Uni<RefreshToken> findRefreshTokenForUser(String userId) {
        return find("userId", userId).firstResult();
    }

    public Uni<RefreshToken> getRefreshTokenForUser(String userId, String token) {
        return find("userId = ?1 and token = ?2", userId, token).firstResult();
    }

}
