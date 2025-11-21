package com.finance_control.goals.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalPriority {
    LOW("low", "Baixa"),
    MEDIUM("medium", "Média"),
    HIGH("high", "Alta");

    private final String code;
    private final String displayName;
}
