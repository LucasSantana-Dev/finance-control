package com.finance_control.profile.model;

import com.finance_control.shared.model.BaseModel;
import com.finance_control.users.model.User;
import com.finance_control.profile.util.CountryUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Profile extends BaseModel<Long> {
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Size(max = 500)
    private String bio;
    
    @Size(max = 20)
    private String phone;
    
    @Size(max = 100)
    @Column(name = "country")
    private String country;
    
    @Size(max = 500)
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    /**
     * Gets the user's currency based on their country.
     * This method provides currency information for other modules.
     * 
     * @return the currency code for the user's country, or null if country is not set
     */
    public String getCurrency() {
        return CountryUtils.getCurrency(country);
    }
    
    /**
     * Gets the user's timezone based on their country.
     * This provides timezone information for date/time operations.
     * 
     * @return the timezone for the user's country, or null if country is not set
     */
    public String getTimezone() {
        return CountryUtils.getTimezone(country);
    }
} 