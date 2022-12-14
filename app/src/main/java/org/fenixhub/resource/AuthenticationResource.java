package org.fenixhub.resource;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.http.HttpServerResponse;
import org.fenixhub.dto.TokenDto;
import org.fenixhub.dto.UserDto;
import org.fenixhub.dto.views.TokenView;
import org.fenixhub.dto.views.UserView;
import org.fenixhub.service.AuthenticationService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/auth")
public class AuthenticationResource {
    
    @Inject AuthenticationService authenticationService;
//    @Context HttpServerResponse response;

    @POST
    @Path("/register")
    public Uni<UserDto> register(
            @Valid @ConvertGroup(to = UserView.Registration.class) UserDto userDto
    ) {
        return authenticationService.register(userDto);
//                .call(
//                user -> {
//                    try {
//                        response.putHeader("Location", new URI("/auth/" + user.getId()).toString());
//                    } catch (URISyntaxException e) {
//                        throw new RuntimeException(e);
//                    }
//                    return Uni.createFrom().item(user);
//                }
//        );
    }
    
    @POST
    @Path("/login")
    public Uni<TokenDto> login(
        @Valid @ConvertGroup(to = UserView.Login.class) UserDto userDto
    ) {
        return authenticationService.login(userDto);
    }
    
    @Path("/logout")
    public Response logout() {
        return Response.ok().build();
    }
    
    @POST
    @Path("/refresh")
    @Authenticated
    public Uni<TokenDto> refresh(
        @Valid @ConvertGroup(to = TokenView.Refresh.class) TokenDto tokenDto
    ) {
        return authenticationService.refreshToken(tokenDto);
    }

}
