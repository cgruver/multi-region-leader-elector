package org.labmonkeys.elector.mapper;

import java.util.Map;

import org.labmonkeys.elector.dto.VoterDto;
import org.labmonkeys.elector.model.Voter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface VoterMapper {
    
    VoterDto voterToDto(Voter voter);
    Voter dtoToVoter(VoterDto dto);

    Map<String,VoterDto> votersToDtos(Map<String, Voter> voters);
    Map<String,Voter> dtoToVoters(Map<String, VoterDto> dtos);
}
