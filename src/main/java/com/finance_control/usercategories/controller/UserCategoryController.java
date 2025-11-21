package com.finance_control.usercategories.controller;

import com.finance_control.usercategories.dto.UserCategoryDTO;
import com.finance_control.usercategories.enums.CategoryType;
import com.finance_control.usercategories.model.UserCategory;
import com.finance_control.usercategories.service.UserCategoryService;
import com.finance_control.shared.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user-categories")
@Tag(name = "User Categories", description = "User-specific transaction category management endpoints")
public class UserCategoryController extends BaseController<UserCategory, Long, UserCategoryDTO> {

    private final UserCategoryService userCategoryService;

    public UserCategoryController(UserCategoryService userCategoryService) {
        super(userCategoryService);
        this.userCategoryService = userCategoryService;
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type", description = "Retrieve user categories filtered by type (income or expense)")
    public ResponseEntity<Page<UserCategoryDTO>> getCategoriesByType(
            @Parameter(description = "Category type") @PathVariable CategoryType type,
            Pageable pageable) {
        log.debug("GET request to retrieve categories by type: {}", type);
        Page<UserCategoryDTO> categories = userCategoryService.findByUserAndType(type, pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/defaults")
    @Operation(summary = "Get default categories", description = "Retrieve all default categories for the current user")
    public ResponseEntity<List<UserCategoryDTO>> getDefaultCategories() {
        log.debug("GET request to retrieve default categories");
        List<UserCategoryDTO> categories = userCategoryService.findDefaultCategories();
        return ResponseEntity.ok(categories);
    }
}
