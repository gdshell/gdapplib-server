package org.fenixhub.services;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.fenixhub.dto.TokenDto;
import org.fenixhub.dto.UserDto;
import org.fenixhub.entities.User;
import org.fenixhub.mapper.UserMapper;
import org.fenixhub.repository.UserRepository;
import org.fenixhub.utils.Configuration;
import org.fenixhub.utils.Helpers;

import io.quarkus.elytron.security.common.BcryptUtil;

@ApplicationScoped
public class AuthenticationService {
    
    @Inject
    private JWTService jwtService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private Configuration configuration;

    @Inject
    private Helpers helpers;

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

        return user.getId();
    }

    @Transactional
    public TokenDto login(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user == null || !BcryptUtil.matches(userDto.getPassword(), user.getPassword())) {
            throw new NotFoundException("Invalid credentials.");
        }

        UserDto loggedUser = UserMapper.INSTANCE.userToUserDto(user);
        
        return TokenDto.builder()
        .tokenType("bearer")
        .idToken(jwtService.generateJWT(loggedUser.getId(), loggedUser.getEmail(), loggedUser.getEmailVerified(),new String[] { "DEVELOPER" }))
        .expiresIn(configuration.getExpirationTimeJwt())
        .refreshToken(jwtService.generateRefreshToken())
        .build();
    }

}
