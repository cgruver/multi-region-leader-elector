package org.labmonkeys.elector.model;

import org.labmonkeys.elector.dto.VoterDto.VoterRole;
import lombok.Data;

@Data
public class Voter {
    private String voterId;
    private String voterUrl;
    private VoterRole role;
    private Integer missedHeartBeats;
    private boolean onLine;
}
