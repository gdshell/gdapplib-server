package org.fenixhub.services;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.eclipse.microprofile.jwt.Claims;
import org.fenixhub.dto.TokenDto;
import org.fenixhub.dto.UserDto;
import org.fenixhub.entities.RefreshToken;
import org.fenixhub.entities.User;
import org.fenixhub.entities.UserRole;
import org.fenixhub.mapper.UserMapper;
import org.fenixhub.repository.RefreshTokenRepository;
import org.fenixhub.repository.RoleRepository;
import org.fenixhub.repository.UserRepository;
import org.fenixhub.repository.UserRoleRepository;
import org.fenixhub.utils.Configuration;
import org.fenixhub.utils.Helpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.elytron.security.common.BcryptUtil;

@ApplicationScoped
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Inject JWTService jwtService;

    @Inject RoleRepository roleRepository;

    @Inject UserRepository userRepository;
    
    @Inject UserRoleRepository userRoleRepository;

    @Inject RefreshTokenRepository refreshTokenRepository;

    @Inject Configuration configuration;

    @Inject Helpers helpers;

    @Inject UserMapper userMapper;


    @Transactional
    public String register(UserDto userDto) {
        if (userRepository.findByUsername(userDto.getUsername()) != null ) {
            throw new BadRequestException("User with this username already exists.");
        }
        if (userRepository.findByEmail(userDto.getEmail()) != null ) {
            throw new BadRequestException("User with this email already exists.");
        }
        userDto.cryptPassword();

        User user = User.builder()
        .username(userDto.getUsername())
        .email(userDto.getEmail())
        .password(userDto.getPassword())
        .registeredAt(helpers.today.apply(0))
        .emailVerified(false)
        .build();
        userRepository.persist(user);

        UserRole userRole = UserRole.builder()
        .userId(user.getId())
        .roleId(roleRepository.getRoleByName("DEVELOPER").getId())
        .build();
        userRoleRepository.persist(userRole);

        return user.getId();
    }

    @Transactional
    public TokenDto createSession(UserDto userDto) {

        RefreshToken refreshToken = refreshTokenRepository.getRefreshTokenForUser(userDto.getId())
        .or(() -> {
            RefreshToken newRefreshToken = RefreshToken.builder()
            .userId(userDto.getId())
            .token(jwtService.generateRefreshToken())
            .createdAt(helpers.today.apply(0))
            .build();
            refreshTokenRepository.persist(newRefreshToken);
            return Optional.of(newRefreshToken);
        }).get();

        return TokenDto.builder()
        .tokenType("bearer")
        .idToken(
            jwtService.generateJWT(
                userDto.getId(), userDto.getEmail(), userDto.getEmailVerified(), 
                userDto.getRoles().stream().map(role -> roleRepository.getRoleById(role.getRoleId()).getName()).collect(Collectors.toSet())
            )
        )
        .expiresIn(configuration.getExpirationTimeJwt())
        .refreshToken(refreshToken.getToken())
        .build();
    }

    @Transactional
    public TokenDto login(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user == null || !BcryptUtil.matches(userDto.getPassword(), user.getPassword())) {
            throw new NotFoundException("Invalid credentials.");
        }

        UserDto loggedUser = userMapper.userToUserDto(user);

        return createSession(loggedUser);
    }

    @Transactional
    public TokenDto refreshToken(TokenDto oldToken) {
        RefreshToken refreshToken = refreshTokenRepository.find(oldToken.getRefreshToken(), jwtService.getClaim(Claims.sub));
        if (refreshToken == null) {
            throw new NotFoundException("Invalid refresh token.");
        }

        UserDto userDto = userMapper.userToUserDto(userRepository.findById(refreshToken.getUserId()));
        refreshTokenRepository.delete(refreshToken);

        return createSession(userDto);
    }

}
