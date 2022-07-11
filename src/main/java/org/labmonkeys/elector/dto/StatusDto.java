package org.labmonkeys.elector.dto;

import java.util.Map;
import lombok.Data;

@Data
public class StatusDto {
    
    private Map<String, VoterDto> knownVoters;
    private VoterDto sender;

}
