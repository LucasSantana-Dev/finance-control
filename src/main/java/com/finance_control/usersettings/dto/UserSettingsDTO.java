package com.finance_control.usersettings.dto;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.usersettings.enums.CurrencyFormat;
import com.finance_control.usersettings.enums.DateFormat;
import com.finance_control.usersettings.enums.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSettingsDTO extends BaseDTO<Long> {

    private Long userId;
    private CurrencyFormat currencyFormat;
    private DateFormat dateFormat;
    private Boolean notificationEmail;
    private Boolean notificationPush;
    private Boolean notificationTransactions;
    private Boolean notificationGoals;
    private Boolean notificationWeeklySummary;
    private Theme theme;
    private String language;
}
