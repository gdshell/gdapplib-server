package org.fenixhub.repository;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.fenixhub.entities.RefreshToken;

@ApplicationScoped
public class RefreshTokenRepository {
    
    @Inject EntityManager entityManager;

    public void persist(RefreshToken token) {
        entityManager.persist(token);
    }

    public Optional<RefreshToken> getRefreshTokenForUser(String userId) {
        List<RefreshToken> refreshTokens = entityManager.createQuery("select rt from RefreshToken rt where rt.userId = :userId", RefreshToken.class)
        .setParameter("userId", userId)
        .getResultList();
        if (refreshTokens.size() > 0) {
            return Optional.of(refreshTokens.get(0));
        } else {
            return Optional.empty();
        }
    }

    public RefreshToken find(String refreshToken, String userId) {
        List<RefreshToken> refreshTokenList = entityManager.createQuery(
            "select refreshToken from RefreshToken refreshToken where refreshToken.token = :refreshToken and refreshToken.userId = :userId", RefreshToken.class)
        .setParameter("refreshToken", refreshToken)
        .setParameter("userId", userId)
        .getResultList();
        if (refreshTokenList.size() > 0) {
            return refreshTokenList.get(0);
        } else {
            return null;
        }
    }

    public void delete(RefreshToken refreshToken) {
        entityManager.remove(refreshToken);
    }

}
