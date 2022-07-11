package org.labmonkeys.elector.dto;

import lombok.Data;

@Data
public class VoterDto {
    
    public enum VoterRole{LEADER,FOLLOWER,NONE}
    private String voterId;
    private String voterUrl;
    private VoterRole role;
    private boolean onLine;

}
