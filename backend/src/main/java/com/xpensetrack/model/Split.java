package com.xpensetrack.model;

import lombok.Data;

@Data
public class Split {
    private String userId;
    private double amount;
    private boolean settled = false;
}
