package com.finance_control.usercategories.dto;

import com.finance_control.shared.dto.BaseDTO;
import com.finance_control.usercategories.enums.CategoryType;
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
public class UserCategoryDTO extends BaseDTO<Long> {

    private Long userId;
    private String name;
    private CategoryType type;
    private String color;
    private String icon;
    private Boolean isDefault;
}
