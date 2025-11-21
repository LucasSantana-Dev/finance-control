package com.finance_control.usersettings.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurrencyFormat {
    BRL("BRL", "R$"),
    USD("USD", "$"),
    EUR("EUR", "€");

    private final String code;
    private final String symbol;
}
