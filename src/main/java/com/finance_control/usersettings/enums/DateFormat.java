package com.finance_control.usersettings.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DateFormat {
    DD_MM_YYYY("DD/MM/YYYY", "dd/MM/yyyy"),
    MM_DD_YYYY("MM/DD/YYYY", "MM/dd/yyyy"),
    YYYY_MM_DD("YYYY-MM-DD", "yyyy-MM-dd");

    private final String displayFormat;
    private final String javaFormat;
}
