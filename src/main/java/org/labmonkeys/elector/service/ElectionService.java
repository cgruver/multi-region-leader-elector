package org.labmonkeys.elector.service;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

import org.labmonkeys.elector.api.LeaderElectionApi;
import org.labmonkeys.elector.dto.VoteDto;
import org.labmonkeys.elector.dto.VoterDto;

@ApplicationScoped
public class ElectionService implements LeaderElectionApi {

    @Override
    public Response startElection(VoterDto voter) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response castVote(VoteDto vote) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response getResults() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response certifyResults() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
