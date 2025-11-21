package com.finance_control.goals.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoalStatus {
    ACTIVE("active", "Ativa"),
    COMPLETED("completed", "Concluída"),
    PAUSED("paused", "Pausada"),
    CANCELLED("cancelled", "Cancelada");

    private final String code;
    private final String displayName;
}
