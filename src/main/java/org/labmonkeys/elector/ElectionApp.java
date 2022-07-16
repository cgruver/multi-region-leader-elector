package org.labmonkeys.elector;

import java.net.URI;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.labmonkeys.elector.api.LeaderElectionApi;
import org.labmonkeys.elector.mapper.VoteMapper;
import org.labmonkeys.elector.mapper.VoterMapper;
import org.labmonkeys.elector.model.Election;
import org.labmonkeys.elector.model.Voter;

import lombok.Getter;
import lombok.Setter;

@Singleton
public class ElectionApp {

    private final Logger LOG = Logger.getLogger(ElectionApp.class);

    @Inject
    VoterApp voterApp;

    @Inject
    VoterMapper voterMapper;

    @Inject
    VoteMapper voteMapper;
    
    @Getter
    @Setter
    public boolean electionInProgress;

    @Getter
    public Election currentElection;
    
    public void startElection() {

    }

    public void callForElection() {

        this.electionInProgress = true;

        for (Voter voter : this.voterApp.getVoters().values()) {
            Response response = null;
            LeaderElectionApi leaderElectionApi = RestClientBuilder.newBuilder().baseUri(URI.create(voter.getVoterUrl())).build(LeaderElectionApi.class);
            try {
                response = leaderElectionApi.startElection();
                if ( response.getStatus() == 200 ) {
                    LOG.info("Leader Election Started");
                } else {
                    LOG.error("Failed to start Election with Voter: " + voter.getVoterId());
                }
            } catch (Exception e) {
                LOG.error("Exception Thrown: " + e.getMessage() + " Failed to start Election with Voter: " + voter.getVoterId());
            }
        }
    }
}
