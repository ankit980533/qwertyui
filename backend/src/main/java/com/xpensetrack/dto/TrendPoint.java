package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrendPoint {
    private String label;
    private double spent;
    private Double budget;
    private Double saved;
}
