package org.labmonkeys.elector.service;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.labmonkeys.elector.VoterApp;
import org.labmonkeys.elector.api.VoterApi;
import org.labmonkeys.elector.dto.StatusDto;
import org.labmonkeys.elector.dto.VoterDto;
import org.labmonkeys.elector.dto.VoterDto.VoterRole;
import org.labmonkeys.elector.mapper.VoterMapper;
import org.labmonkeys.elector.model.Voter;

public class VoterService implements VoterApi {

    @Inject
    VoterApp voterApp;

    @Inject
    VoterMapper voterMapper;

    @Override
    public Response heartBeat(StatusDto status) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response registerVoter(VoterDto dto) {
        voterApp.updateVoter(voterMapper.dtoToVoter(dto));
        StatusDto status = new StatusDto();
        status.setSender(this.voterMapper.voterToDto(voterApp.getVoter()));
        status.setKnownVoters(this.voterMapper.votersToDtos(voterApp.getVoters()));
        return Response.ok(status).build();
    }

    @Override
    public Response removeVoter(VoterDto dto) {
        Voter voter = voterApp.getVoters().get(dto.getVoterId());
        voter.setMissedHeartBeats(0);
        voter.setRole(VoterRole.NONE);
        voter.setOnLine(false);
        voterApp.updateVoter(voter);
        StatusDto status = new StatusDto();
        status.setSender(this.voterMapper.voterToDto(voterApp.getVoter()));
        status.setKnownVoters(this.voterMapper.votersToDtos(voterApp.getVoters()));
        return Response.ok(status).build();
    }
}
