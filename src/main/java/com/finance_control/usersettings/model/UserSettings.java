package com.finance_control.usersettings.model;

import com.finance_control.shared.model.BaseModel;
import com.finance_control.users.model.User;
import com.finance_control.usersettings.enums.CurrencyFormat;
import com.finance_control.usersettings.enums.DateFormat;
import com.finance_control.usersettings.enums.Theme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@ToString(exclude = {"user"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSettings extends BaseModel<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_format", length = 3)
    private CurrencyFormat currencyFormat = CurrencyFormat.BRL;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_format", length = 20)
    private DateFormat dateFormat = DateFormat.DD_MM_YYYY;

    @Column(name = "notification_email")
    private Boolean notificationEmail = true;

    @Column(name = "notification_push")
    private Boolean notificationPush = false;

    @Column(name = "notification_transactions")
    private Boolean notificationTransactions = true;

    @Column(name = "notification_goals")
    private Boolean notificationGoals = true;

    @Column(name = "notification_weekly_summary")
    private Boolean notificationWeeklySummary = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Theme theme = Theme.LIGHT;

    @Column(length = 10)
    private String language = "pt-BR";
}
