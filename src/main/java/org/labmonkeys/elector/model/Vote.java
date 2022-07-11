package org.labmonkeys.elector.model;

import java.util.UUID;

import lombok.Data;

@Data
public class Vote {
    
    private UUID voterId;
    private Long vote;
}
