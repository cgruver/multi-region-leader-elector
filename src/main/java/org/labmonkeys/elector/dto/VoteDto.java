package org.labmonkeys.elector.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class VoteDto {
    
    private UUID voterId;
    private Long vote;
}
