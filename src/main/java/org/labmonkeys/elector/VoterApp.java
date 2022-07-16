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
import org.labmonkeys.elector.dto.VoterDto;
import org.labmonkeys.elector.dto.VoterDto.VoterRole;
import org.labmonkeys.elector.mapper.VoterMapper;
import org.labmonkeys.elector.model.Voter;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import lombok.Getter;

@Singleton
public class VoterApp {
    
    private final Logger LOG = Logger.getLogger(VoterApp.class);

    @Inject
    ElectionApp election;

    @Inject
    VoterMapper voterMapper;

    @ConfigProperty(name = "leader-elector.voter-list")
    private List<Voter> voterList;

    @ConfigProperty(name = "leader-elector.voter-url")
    private String voterUrl;

    @ConfigProperty(name = "leader-elector.voter-id")
    private String voterId;

    @ConfigProperty(name = "leader-elector.heartbeat-interval")
    private String heartbeatInterval;

    @ConfigProperty(name = "leader-elector.missed-heartbeat-tolerance")
    private Integer missedHbTolerance;

    @Getter
    Map<String, Voter> voters;

    @Getter
    Voter me;
    
    private Integer quorum;

    void startUp(@Observes StartupEvent startupEvent) {

        this.me = new Voter();
        this.me.setVoterId(this.voterId);
        this.me.setVoterUrl(this.voterUrl);
        this.me.setRole(VoterRole.NONE);
        this.me.setMissedHeartBeats(0);
        this.me.setOnLine(true);
        this.voters = Collections.synchronizedMap(new HashMap<String, Voter>());
        this.election.electionInProgress = false;
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
                voterApi.removeVoter(voterMapper.voterToDto(this.me));
            } catch (Exception e) {
                LOG.info("Exception Thrown: " + e.getMessage());
                LOG.info("Continue Shutdown");
            }
        }
    }
    
    @Scheduled(every = "{leader-elector.heartbeat-interval}")
    void scheduledTasks() {

        if (this.me.getRole() == VoterRole.NONE) {
            this.bootStrap();
        } else {
            for (Voter knownVoter : this.voters.values()) {
                URI uri = URI.create(knownVoter.getVoterUrl());
                VoterApi voterApi = RestClientBuilder.newBuilder().baseUri(uri).build(VoterApi.class);
                this.sendHeartBeat(voterApi, knownVoter);
            }
            this.healthCheck();
        }
    }

    private void bootStrap() {
        for (Voter voter : this.voters.values()) {
            Response response = null;
            VoterApi voterApi = RestClientBuilder.newBuilder().baseUri(URI.create(voter.getVoterUrl())).build(VoterApi.class);
            try {
                response = voterApi.registerVoter(voterMapper.voterToDto(this.me));
                if ( response.getStatus() == 200 ) {
                    VoterDto sender = response.readEntity(VoterDto.class);
                    LOG.info("Registration Response received from: " + sender.getVoterId());
                    this.updateVoter(sender);
                    this.resetHeartbeat(sender.getVoterId());
                } else {
                    LOG.info("Bootstrap: No Status Received.  Continue Bootstrap.");
                }
            } catch (Exception e) {
                LOG.error("Exception Thrown: " + e.getMessage());
                LOG.info("Continue Bootstrap.");
            }
        }
        // Check For Quorum and set my VoterRole
        if (this.checkQuorum()) {
            boolean activeLeader = false;
            if (me.getRole() != VoterRole.LEADER) {
                for (Voter voter : this.voters.values()) {
                    if (voter.getRole() == VoterRole.LEADER && voter.isOnLine()) {
                        me.setRole(VoterRole.FOLLOWER);
                        activeLeader = true;
                    }
                }
            } else {
                activeLeader = true;
            }
            if (!activeLeader) {
                this.election.callForElection();
            }
        }
    }

    private void sendHeartBeat(VoterApi voterApi, Voter knownVoter) {
        Response response = null;
        try {
            response = voterApi.heartBeat(voterMapper.voterToDto(this.me));
            if ( response.getStatus() == 200 ) {
                VoterDto sender = response.readEntity(VoterDto.class);
                LOG.info("Heartbeat Response received from: " + sender.getVoterId());
                this.updateVoter(sender);
                this.resetHeartbeat(sender.getVoterId());
            } else {
                LOG.info("Received response code: " + response.getStatus() + "From Voter: " + knownVoter.getVoterId());
                knownVoter.setMissedHeartBeats(knownVoter.getMissedHeartBeats() + 1);
                this.voters.put(knownVoter.getVoterId(), knownVoter);
            }
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage() + " On heartbeat from Voter: " + knownVoter.getVoterId());
            knownVoter.setMissedHeartBeats(knownVoter.getMissedHeartBeats() + 1);
            this.voters.put(knownVoter.getVoterId(), knownVoter);
        }
    }

    private void healthCheck() {
        for (Voter voter : this.voters.values()) {
            if (voter.getMissedHeartBeats() >= this.missedHbTolerance) {
                voter.setOnLine(false);
                this.voters.put(voter.getVoterId(), voter);
                if (voter.getRole() == VoterRole.LEADER) {
                    // The leader may be down, or I may be down.  Check to see if an election is necessary.
                    if (checkQuorum()) {
                        this.election.callForElection();
                    } else {
                        // I lost Quorum.  I may be offline or isolated, so go back to bootstrap mode.
                        me.setRole(VoterRole.NONE);
                    }
                }
            }
        }
    }

    public void updateVoter(VoterDto dto) {
        Voter voterUpdate = this.voters.get(dto.getVoterId());
        voterUpdate.setOnLine(dto.isOnLine());
        voterUpdate.setRole(dto.getRole());
        this.voters.put(voterUpdate.getVoterId(), voterUpdate);
    }

    public void resetHeartbeat(String voterId) {
        Voter voterUpdate = this.voters.get(voterId);
        voterUpdate.setMissedHeartBeats(0);
        voterUpdate.setOnLine(true);
        this.voters.put(voterId, voterUpdate);
    }

    private boolean checkQuorum() {

        Integer onLineVoters = 0;
        if (me.isOnLine()) {
            onLineVoters++;
        }
        for (Voter voter : this.voters.values()) {
            if (voter.isOnLine()) {
                onLineVoters++;
            }
        }
        if (onLineVoters >= this.quorum) {
            return true;
        } else {
            return false;
        }
    }
}
