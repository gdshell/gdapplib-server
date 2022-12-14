package org.fenixhub.service;

import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.eclipse.microprofile.jwt.Claims;
import org.fenixhub.dto.ERole;
import org.fenixhub.dto.TokenDto;
import org.fenixhub.dto.UserDto;
import org.fenixhub.entity.RefreshToken;
import org.fenixhub.entity.Role;
import org.fenixhub.entity.User;
import org.fenixhub.entity.UserRole;
import org.fenixhub.mapper.UserMapper;
import org.fenixhub.repository.RefreshTokenRepository;
import org.fenixhub.repository.RoleRepository;
import org.fenixhub.repository.UserRepository;
import org.fenixhub.repository.UserRoleRepository;
import org.fenixhub.utils.Configuration;
import org.fenixhub.utils.Helpers;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthenticationService {
    
    @Inject JWTService jwtService;

    @Inject RoleRepository roleRepository;

    @Inject UserRepository userRepository;
    
    @Inject UserRoleRepository userRoleRepository;

    @Inject RefreshTokenRepository refreshTokenRepository;

    @Inject Configuration configuration;

    @Inject Helpers helpers;

    @Inject UserMapper userMapper;


    @ReactiveTransactional
    public Uni<UserDto> register(UserDto userDto) {
/*        return userRepository.checkIfUsernameExists(userDto.getUsername())
                .onItem().ifNotNull().failWith(() -> new BadRequestException("Username already exists"))
                .onItem().ifNull().continueWith(() -> userRepository.checkIfEmailExists(userDto.getEmail()))
                .onItem().ifNotNull().failWith(() -> new BadRequestException("Email already exists"))
                .onItem().ifNull().continueWith(() -> {
                    userDto.setPassword(userDto.cryptPassword());
                    userDto.setRegisteredAt(helpers.getCurrentTimestamp());
                    userDto.setEmailVerified(false);
                    userDto.setRoles(Set.of(ERole.ROLE_USER));
                    return userMapper.userDtoToUser(userDto);
                })
                .onItem().transformToUni(userRepository::persist)
                .onItem().transformToUni(user -> {
                    return roleRepository.findByName(ERole.ROLE_USER.name())
                            .onItem().ifNull().failWith(() -> new BadRequestException("Role not found"))
                            .onItem().transformToUni(role -> {
                                UserRole userRole = new UserRole();
                                userRole.setUser(user);
                                userRole.setRole(role);
                                return userRoleRepository.persist(userRole);
                            })
                            .onItem().transformToUni(userRole -> Uni.createFrom().item(userMapper.userToUserDto(user)));
                });*/
        return Uni.createFrom().item(userDto)
                .call(() -> {
                    return userRepository.checkIfUsernameExists(userDto.getUsername())
                            .call(exists -> {
                                if (exists) {
                                    throw new BadRequestException("Username is already taken!");
                                }
                                return null;
                            });
                })
                .call(() -> {
                    return userRepository.checkIfEmailExists(userDto.getEmail())
                            .call(exists -> {
                                if (exists) {
                                    throw new BadRequestException("Email is already taken!");
                                }
                                return null;
                            });
                })
                .flatMap(checkedUserDto -> {
                        return userRepository.persist(
                                User.builder()
                                        .password(userDto.cryptPassword())
                                        .username(userDto.getUsername())
                                        .email(userDto.getEmail())
                                        .registeredAt(helpers.today.apply(0))
                                        .emailVerified(false)
                                        .build()
                        ).map(user -> userMapper.userToUserDto(user));
                    }
                );
    }

    @ReactiveTransactional
    public Uni<TokenDto> createSession(UserDto userDto) {

        Uni<RefreshToken> refreshToken = refreshTokenRepository.findRefreshTokenForUser(userDto.getId())
                .onItem().ifNotNull().call(
                        foundToken -> {
                            if (jwtService.isRefreshTokenExpired(foundToken)) {
                                return refreshTokenRepository.delete(foundToken).onItem().ignore().andContinueWithNull();
                            }
                            return Uni.createFrom().item(foundToken);
                        }
                )
                .onItem().ifNull().switchTo(
                        () -> refreshTokenRepository.persist(
                                RefreshToken.builder()
                                        .userId(userDto.getId())
                                        .token(jwtService.generateRefreshToken())
                                        .expiresAt(helpers.today.apply(jwtService.configuration.getExpirationTimeRefreshToken()))
                                        .build()
                        )
                );

        return refreshToken.onItem().transform(
                (RefreshToken token) -> {
                    return TokenDto.builder()
                                .tokenType("bearer")
                                .idToken(
                                        jwtService.generateJWT(
                                                userDto.getId(),
                                                userDto.getEmail(),
                                                userDto.getEmailVerified(),
                                                userDto.getRoles().stream().map(ERole::toString).collect(Collectors.toSet())
                                        )
                                )
                                .expiresIn(configuration.getExpirationTimeJwt())
                                .refreshToken(token.getToken())
                                .build();
                }
        );
    }

    @ReactiveTransactional
    public Uni<TokenDto> login(UserDto userDto) {
        return userRepository.findByEmail(userDto.getEmail())
                .onItem().ifNull().failWith(UnauthorizedException::new)
                .onItem().ifNotNull().transformToUni(Unchecked.function(user -> {
                    if (!BCrypt.checkpw(userDto.getPassword(), user.getPassword())) {
                        throw new UnauthorizedException("Invalid credentials.");
                    }
                    return createSession(userMapper.userToUserDto(user));
                }));
    }

    @ReactiveTransactional
    public Uni<TokenDto> refreshToken(TokenDto oldToken) {
        return refreshTokenRepository.getRefreshTokenForUser(jwtService.getClaim(Claims.sub), oldToken.getRefreshToken())
                .onItem().ifNull().failWith(() -> new ForbiddenException("Refresh token not found."))
                .onItem().ifNotNull().call(
                        foundToken -> {
                            if (jwtService.isRefreshTokenExpired(foundToken)) {
                                throw new ForbiddenException("Refresh token expired.");
                            }
                            return Uni.createFrom().item(foundToken);
                        }
                )
                .flatMap(
                        (RefreshToken token) -> {
                            return userRepository.findById(token.getUserId()).map(
                                    user -> { return userMapper.userToUserDto(user); }
                            );
                        }
                ).flatMap(
                        (UserDto user) -> {
                            refreshTokenRepository.delete(oldToken.getRefreshToken());
                            return createSession(user);
                        }
                );
    }

}
