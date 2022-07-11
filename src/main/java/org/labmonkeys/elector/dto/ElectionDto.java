package org.labmonkeys.elector.dto;

import java.util.Set;
import java.util.UUID;
import lombok.Data;

@Data
public class ElectionDto {
    private boolean certified;
    private Set<VoteDto> votes;
    private UUID newLeader;
}
