package com.xpensetrack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private List<String> quickActions;
}
