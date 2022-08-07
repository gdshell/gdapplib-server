package org.fenixhub.resources;

import java.net.URI;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.fenixhub.dto.UserDto;
import org.fenixhub.dto.views.UserView;
import org.fenixhub.services.AuthenticationService;

@Path("/auth")
public class AuthenticationResource {
    
    @Inject
    private AuthenticationService authenticationService;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    public Response register(
        @Valid @ConvertGroup(to = UserView.Registration.class) UserDto userDto
    ) {
        String uuid = authenticationService.register(userDto);
        return Response.created(URI.create("/user/" + uuid)).build();
    }
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
        @Valid @ConvertGroup(to = UserView.Login.class) UserDto userDto
    ) {
        return Response.ok(authenticationService.login(userDto)).build();
    }
    
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() {
        return Response.ok().build();
    }
    
    @Path("/refresh")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh() {
        return Response.ok().build();
    }

}