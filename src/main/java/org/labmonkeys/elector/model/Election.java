package org.labmonkeys.elector.model;

import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
public class Election {
    
    private boolean certified;
    private Set<Vote> votes;
    private UUID newLeader;

}
