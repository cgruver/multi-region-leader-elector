package org.labmonkeys.elector.mapper;

import java.util.Map;

import org.labmonkeys.elector.dto.VoteDto;
import org.labmonkeys.elector.model.Vote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface VoteMapper {
    
    Vote dtoToVote(VoteDto vote);
    VoteDto VoteToDto(Vote dto);

    Map<String,VoteDto> votesToDtos(Map<String, Vote> votes);
    Map<String,Vote> dtoToVotes(Map<String, VoteDto> dtos);

}
