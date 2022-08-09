package org.fenixhub.services;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.fenixhub.utils.Configuration;
import org.fenixhub.utils.Helpers;

import io.smallrye.jwt.build.Jwt;

@ApplicationScoped
public class JWTService {


	@Inject
	private Configuration configuration;

	@Inject
	private Helpers	helpers;
	
	@Inject
	JsonWebToken jwt;

	/**
	 * This method generate a valid JWT for the user.
	 * @param claims A map object that represent all claims of the user.
	 * @param email	The email of the user which to genereate the JWT
	 * @param userRoles The roles of the user which to generate the JWT
	 * @return A string that represent a valid JWT with an expired time of {@link #expirationTimeJwt}
	 */
	public String generateJWT(String sub, String upn, boolean emailVerified, Set<String> roles) {
		return Jwt
			.subject(sub)										// User ID
			.upn(upn)											// User email
			.claim(Claims.email_verified, emailVerified)
			.groups(new HashSet<>(roles))
			.expiresIn(configuration.getExpirationTimeJwt())	// Expiration time of the JWT
			.issuedAt(helpers.today.apply(0))			// Issued time of the JWT
			.issuer(configuration.getIssuer())
			.sign()
			;
	}

	public String getClaim(Claims claim) {
		return jwt.getClaim(claim);
	}

	/*
	 * Checks whether a claim is present in the JWT and the value is equal to the expected value.
	 */
	public boolean verifyClaim(Claims claimName, String claimExpectedValue) {
		return claimExpectedValue.equals(jwt.getClaim(claimName));
	}

	/**
	 * This method check if the JWT contains the set of roles passed as parameter
	 * @param roles A set of roles to verify in the JWT.
	 * @return If JWT groups contains all roles return {@code True} else return {@code False}
	 */
	public boolean verifyGroups(Set<String> roles) {
		return jwt.getGroups().containsAll(roles);
	}

	/**
	 * Check if the jwt is null or empty or contains blank at the start of jwt.
	 * 
	 * @return If the JWT is null or empty or contains blank at the start of jwt return {@code true} else return {@code false}.
	 */
	public boolean isEmptyOrNull() {
		return  jwt == null || jwt.getRawToken() == null || jwt.getRawToken().isBlank();
	}

	/**
	 * This method is used to genereate a new refreshToken.
	 * @return A String object that represent the refreshToken. The refresh token is generated with {@link UUID}.
	 */
	public String generateRefreshToken() {
		return UUID.randomUUID().toString();
	}

}
