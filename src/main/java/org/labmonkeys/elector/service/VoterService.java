package org.labmonkeys.elector.service;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.labmonkeys.elector.VoterApp;
import org.labmonkeys.elector.api.VoterApi;
import org.labmonkeys.elector.dto.VoterDto;
import org.labmonkeys.elector.dto.VoterDto.VoterRole;
import org.labmonkeys.elector.mapper.VoterMapper;

public class VoterService implements VoterApi {

    @Inject
    VoterApp voterApp;

    @Inject
    VoterMapper voterMapper;

    @Override
    public Response heartBeat(VoterDto dto) {
        return Response.ok(this.voterMapper.voterToDto(voterApp.getMe())).build();
    }

    @Override
    public Response registerVoter(VoterDto dto) {
        voterApp.updateVoter(dto);
        return Response.ok(this.voterMapper.voterToDto(voterApp.getMe())).build();
    }

    @Override
    public Response removeVoter(VoterDto dto) {
        dto.setOnLine(false);
        dto.setRole(VoterRole.NONE);
        voterApp.updateVoter(dto);
        voterApp.resetHeartbeat(dto.getVoterId());
        return Response.ok(this.voterMapper.voterToDto(voterApp.getMe())).build();
    }

}
