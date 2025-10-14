package com.finance_control.goals.controller;

import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.dto.GoalCompletionRequest;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.shared.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/financial-goals")
@Slf4j
@Tag(name = "Financial Goals", description = "Endpoints for managing financial goals")
public class FinancialGoalController extends BaseController<FinancialGoal, Long, FinancialGoalDTO> {

    private final FinancialGoalService financialGoalService;

    public FinancialGoalController(FinancialGoalService financialGoalService) {
        super(financialGoalService);
        this.financialGoalService = financialGoalService;
    }

    @GetMapping("/filtered")
    @Operation(summary = "Get financial goals with filtering",
               description = "Retrieve financial goals with flexible filtering, sorting, and pagination options")
    public ResponseEntity<Page<FinancialGoalDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String goalType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minTargetAmount,
            @RequestParam(required = false) BigDecimal maxTargetAmount,
            @RequestParam(required = false) LocalDate deadlineStart,
            @RequestParam(required = false) LocalDate deadlineEnd,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            Pageable pageable,
            HttpServletRequest request) {

        log.debug("GET request to retrieve financial goals with filtering");

        // Extract all query parameters as filters (excluding standard ones)
        Map<String, Object> filters = extractFiltersFromRequest(request, search, sortBy, sortDirection);

        // Add specific goal filters
        if (goalType != null && !goalType.trim().isEmpty()) {
            filters.put("goalType", goalType);
        }
        if (status != null && !status.trim().isEmpty()) {
            if ("active".equalsIgnoreCase(status)) {
                filters.put("isActive", true);
            } else if ("completed".equalsIgnoreCase(status)) {
                filters.put("isActive", false);
            }
        }
        if (minTargetAmount != null) {
            filters.put("minTargetAmount", minTargetAmount);
        }
        if (maxTargetAmount != null) {
            filters.put("maxTargetAmount", maxTargetAmount);
        }
        if (deadlineStart != null) {
            filters.put("deadlineStart", deadlineStart);
        }
        if (deadlineEnd != null) {
            filters.put("deadlineEnd", deadlineEnd);
        }
        if (isActive != null) {
            filters.put("isActive", isActive);
        }

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy != null ? sortBy : "createdAt");
        Pageable finalPageable = PageRequest.of(page, size, sort);

        Page<FinancialGoalDTO> goals = financialGoalService.findAll(search, filters, sortBy, sortDirection, finalPageable);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active goals",
               description = "Retrieve all active financial goals for the current user.")
    public ResponseEntity<List<FinancialGoalDTO>> getActiveGoals() {
        List<FinancialGoalDTO> goals = financialGoalService.findActiveGoals();
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed goals",
               description = "Retrieve all completed financial goals for the current user.")
    public ResponseEntity<List<FinancialGoalDTO>> getCompletedGoals() {
        List<FinancialGoalDTO> goals = financialGoalService.findCompletedGoals();
        return ResponseEntity.ok(goals);
    }

    @PostMapping("/{id}/progress")
    @Operation(summary = "Update goal progress",
               description = "Add an amount to the current progress of a goal.")
    public ResponseEntity<FinancialGoalDTO> updateProgress(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        FinancialGoalDTO updatedGoal = financialGoalService.updateProgress(id, amount);
        return ResponseEntity.ok(updatedGoal);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark goal as completed",
               description = "Mark a financial goal as completed.")
    public ResponseEntity<FinancialGoalDTO> markAsCompleted(@PathVariable Long id) {
        FinancialGoalDTO completedGoal = financialGoalService.markAsCompleted(id);
        return ResponseEntity.ok(completedGoal);
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate goal",
               description = "Reactivate a previously completed goal.")
    public ResponseEntity<FinancialGoalDTO> reactivate(@PathVariable Long id) {
        FinancialGoalDTO reactivatedGoal = financialGoalService.reactivate(id);
        return ResponseEntity.ok(reactivatedGoal);
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete goal with details",
               description = "Complete a financial goal with final completion data")
    public ResponseEntity<FinancialGoalDTO> completeGoal(@PathVariable Long id,
                                                         @Valid @RequestBody GoalCompletionRequest request) {
        log.debug("PUT request to complete goal ID: {} with data: {}", id, request);

        FinancialGoalDTO completedGoal = financialGoalService.completeGoal(id, request);
        log.info("Goal completed successfully with ID: {}", id);
        return ResponseEntity.ok(completedGoal);
    }


}
