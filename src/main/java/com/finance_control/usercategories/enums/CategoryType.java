package com.finance_control.usercategories.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryType {
    INCOME("income", "Receita"),
    EXPENSE("expense", "Despesa");

    private final String code;
    private final String displayName;
}
