package com.finance_control.notifications.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    INSTALLMENT_DUE("installment_due", "Parcela Vencendo"),
    GOAL_PROGRESS("goal_progress", "Progresso da Meta"),
    GOAL_ACHIEVED("goal_achieved", "Meta Alcançada"),
    BUDGET_ALERT("budget_alert", "Alerta de Orçamento");

    private final String code;
    private final String displayName;
}
