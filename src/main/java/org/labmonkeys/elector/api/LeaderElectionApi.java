package org.labmonkeys.elector.api;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.labmonkeys.elector.dto.VoteDto;

@ApplicationScoped
@ApplicationPath("/voter/election")
@RegisterRestClient
public interface LeaderElectionApi {
    
    @POST
    @Path("/start")
    @Produces(MediaType.APPLICATION_JSON)
    public Response startElection();

    @POST
    @Path("/cast")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response castVote(VoteDto vote);

    @GET
    @Path("/results")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResults();

    @POST
    @Path("/certify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response certifyResults();
}
