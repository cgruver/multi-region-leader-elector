package org.labmonkeys.elector;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.labmonkeys.elector.api.VoterApi;
import org.labmonkeys.elector.dto.StatusDto;
import org.labmonkeys.elector.dto.VoterDto;
import org.labmonkeys.elector.dto.VoterDto.VoterRole;
import org.labmonkeys.elector.mapper.VoteMapper;
import org.labmonkeys.elector.mapper.VoterMapper;
import org.labmonkeys.elector.model.Election;
import org.labmonkeys.elector.model.Voter;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import lombok.Getter;

@Singleton
public class VoterApp {
    
    private final Logger LOG = Logger.getLogger(VoterApp.class);

    @Inject VoterMapper voterMapper;

    @Inject VoteMapper voteMapper;

    @ConfigProperty(name = "leader-elector.other-voters")
    private List<Voter> voterList;

    @ConfigProperty(name = "leader-elector.voter-url")
    private String voterUrl;

    @ConfigProperty(name = "leader-elector.voter-id")
    private String voterId;

    @ConfigProperty(name = "leader-elector.heartbeat")
    private String heartbeatInterval;

    @Getter
    Map<String, Voter> voters;

    @Getter
    Voter voter;
    
    private boolean electionInProgress;

    private Election currentElection;

    private Integer quorum;

    void startUp(@Observes StartupEvent startupEvent) {

        this.voter = new Voter();
        this.voter.setVoterId(this.voterId);
        this.voter.setVoterUrl(this.voterUrl);
        this.voter.setRole(VoterRole.NONE);
        this.voter.setMissedHeartBeats(0);
        this.voters = Collections.synchronizedMap(new HashMap<String, Voter>());
        this.electionInProgress = false;
        this.currentElection = new Election();
        for (Voter voter : this.voterList) {
            voter.setMissedHeartBeats(0);
            voter.setRole(VoterRole.NONE);
            voter.setOnLine(false);
            this.voters.put(voter.getVoterId(), voter);
        }
        this.quorum = this.voters.size()/2 + 1;
    }

    void shutDown(@Observes ShutdownEvent shutdownEvent) {
        LOG.info("Shutting Down");
        for (Voter voter : this.voters.values()) {
            VoterApi voterApi = RestClientBuilder.newBuilder().baseUri(URI.create(voter.getVoterUrl())).build(VoterApi.class);
            try {
                voterApi.removeVoter(voterMapper.voterToDto(this.voter));
            } catch (Exception e) {
                LOG.info("Exception Thrown: " + e.getMessage());
                LOG.info("Continue Shutdown");
            }
        }
    }
    
    @Scheduled(every = "{leader-elector.heartbeat}")
    void scheduledTasks() {

        if (this.voter.getRole() == VoterRole.NONE) {
            this.bootStrap();
        } else {
            this.sendHeartBeat();
            this.healthCheck();
        }
    }

    private void bootStrap() {
        for (Voter voter : this.voters.values()) {
            Response response = null;
            VoterApi voterApi = RestClientBuilder.newBuilder().baseUri(URI.create(voter.getVoterUrl())).build(VoterApi.class);
            try {
                response = voterApi.registerVoter(voterMapper.voterToDto(this.voter));
                if ( response.getStatus() == 200 ) {
                    StatusDto status = response.readEntity(StatusDto.class);
                    LOG.info("Registration Response received from: " + status.getSender().getVoterId());
                    this.processStatus(status);
                } else {
                    LOG.info("Bootstrap: No Status Received.  Continue Bootstrap.");
                }
            } catch (Exception e) {
                LOG.info("Exception Thrown: " + e.getMessage());
                LOG.info("Continue Bootstrap.");
            }
        }
    }

    private void sendHeartBeat() {
        StatusDto status = new StatusDto();
        status.setSender(voterMapper.voterToDto(this.voter));
        status.setKnownVoters(voterMapper.votersToDtos(this.voters));

        for (Voter knownVoter : this.voters.values()) {
            Response response = null;
            URI uri = URI.create(knownVoter.getVoterUrl());
            VoterApi voterApi = RestClientBuilder.newBuilder().baseUri(uri).build(VoterApi.class);
            try {
                response = voterApi.heartBeat(status);
                if ( response.getStatus() == 200 ) {
                    StatusDto hbStatus = response.readEntity(StatusDto.class);

                } else {
                    
                }
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
    }

    private void healthCheck() {


    }

    private void processStatus(StatusDto status) {
        for (VoterDto dto : status.getKnownVoters().values()) {
            
        }
    }

    public void updateVoter(Voter voter) {
        voter.setMissedHeartBeats(this.voters.get(voter.getVoterId()).getMissedHeartBeats());
        this.voters.put(voter.getVoterId(), voter);
    }

}
