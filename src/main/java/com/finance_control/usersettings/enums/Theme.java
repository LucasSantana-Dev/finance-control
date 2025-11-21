package com.finance_control.usersettings.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Theme {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    private final String code;
}
