package org.labmonkeys.elector.api;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.labmonkeys.elector.dto.StatusDto;
import org.labmonkeys.elector.dto.VoterDto;

@ApplicationScoped
@Path("/voter")
@RegisterRestClient
public interface VoterApi {

    @POST
    @Path("/heartbeat")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response heartBeat(StatusDto status);

    @POST
    @Path("/register")
    @Timeout(2000)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerVoter(VoterDto voter);

    @POST
    @Path("/remove")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeVoter(VoterDto voter);

}
