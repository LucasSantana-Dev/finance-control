package com.finance_control.goals.controller;

import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.dto.GoalCompletionRequest;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.service.FinancialGoalService;
import com.finance_control.shared.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
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

    @Override
    @GetMapping
    @Operation(summary = "Get financial goals with filtering",
               description = "Retrieve financial goals with flexible filtering, sorting, and pagination options")
    public ResponseEntity<Page<FinancialGoalDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            Pageable pageable,
            HttpServletRequest request) {

        log.debug("GET request to retrieve financial goals with filtering");

        // Extract all query parameters as filters (excluding standard ones)
        Map<String, Object> filters = extractFiltersFromRequest(request, search, sortBy, sortDirection);

        // Add specific goal filters from request parameters
        String goalType = request.getParameter("goalType");
        String status = request.getParameter("status");
        String minTargetAmountStr = request.getParameter("minTargetAmount");
        String maxTargetAmountStr = request.getParameter("maxTargetAmount");
        String deadlineStartStr = request.getParameter("deadlineStart");
        String deadlineEndStr = request.getParameter("deadlineEnd");
        String isActiveStr = request.getParameter("isActive");

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
        if (minTargetAmountStr != null && !minTargetAmountStr.trim().isEmpty()) {
            try {
                filters.put("minTargetAmount", new BigDecimal(minTargetAmountStr));
            } catch (NumberFormatException e) {
                log.warn("Invalid minTargetAmount parameter: {}", minTargetAmountStr);
            }
        }
        if (maxTargetAmountStr != null && !maxTargetAmountStr.trim().isEmpty()) {
            try {
                filters.put("maxTargetAmount", new BigDecimal(maxTargetAmountStr));
            } catch (NumberFormatException e) {
                log.warn("Invalid maxTargetAmount parameter: {}", maxTargetAmountStr);
            }
        }
        if (deadlineStartStr != null && !deadlineStartStr.trim().isEmpty()) {
            try {
                filters.put("deadlineStart", LocalDate.parse(deadlineStartStr));
            } catch (Exception e) {
                log.warn("Invalid deadlineStart parameter: {}", deadlineStartStr);
            }
        }
        if (deadlineEndStr != null && !deadlineEndStr.trim().isEmpty()) {
            try {
                filters.put("deadlineEnd", LocalDate.parse(deadlineEndStr));
            } catch (Exception e) {
                log.warn("Invalid deadlineEnd parameter: {}", deadlineEndStr);
            }
        }
        if (isActiveStr != null && !isActiveStr.trim().isEmpty()) {
            filters.put("isActive", Boolean.valueOf(isActiveStr));
        }

        Page<FinancialGoalDTO> goals = financialGoalService.findAll(search, filters, sortBy, sortDirection, pageable);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/filtered")
    @Operation(summary = "Get financial goals with filtering",
               description = "Retrieve financial goals with flexible filtering, sorting, and pagination options")
    public ResponseEntity<Page<FinancialGoalDTO>> findAllFiltered(
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
        log.debug("PUT request to complete goal ID: {}", id);

        FinancialGoalDTO completedGoal = financialGoalService.completeGoal(id, request);
        log.info("Goal completed successfully with ID: {}", id);
        return ResponseEntity.ok(completedGoal);
    }

    @GetMapping("/metadata/types")
    @Operation(summary = "Get goal types",
               description = "Retrieve all available goal types")
    public ResponseEntity<List<String>> getGoalTypes() {
        List<String> types = financialGoalService.getGoalTypes();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/metadata/status-summary")
    @Operation(summary = "Get status summary",
               description = "Retrieve summary of goal statuses for the current user")
    public ResponseEntity<Map<String, Object>> getStatusSummary() {
        Long userId = getCurrentUserId();
        Map<String, Object> statusSummary = financialGoalService.getStatusSummary(userId);
        return ResponseEntity.ok(statusSummary);
    }

    @GetMapping("/metadata/progress-summary")
    @Operation(summary = "Get progress summary",
               description = "Retrieve summary of goal progress for the current user")
    public ResponseEntity<Map<String, Object>> getProgressSummary() {
        Long userId = getCurrentUserId();
        Map<String, Object> progressSummary = financialGoalService.getProgressSummary(userId);
        return ResponseEntity.ok(progressSummary);
    }

    @GetMapping("/metadata/deadline-alerts")
    @Operation(summary = "Get deadline alerts",
               description = "Retrieve goals with upcoming deadlines for the current user")
    public ResponseEntity<List<Map<String, Object>>> getDeadlineAlerts() {
        Long userId = getCurrentUserId();
        List<Map<String, Object>> deadlineAlerts = financialGoalService.getDeadlineAlerts(userId);
        return ResponseEntity.ok(deadlineAlerts);
    }

    @GetMapping("/metadata/completion-rate")
    @Operation(summary = "Get completion rate",
               description = "Retrieve goal completion rate for the current user")
    public ResponseEntity<Map<String, Object>> getCompletionRate() {
        Long userId = getCurrentUserId();
        Map<String, Object> completionRate = financialGoalService.getCompletionRate(userId);
        return ResponseEntity.ok(completionRate);
    }

    @GetMapping("/metadata/average-completion-time")
    @Operation(summary = "Get average completion time",
               description = "Retrieve average completion time for goals of the current user")
    public ResponseEntity<Map<String, Object>> getAverageCompletionTime() {
        Long userId = getCurrentUserId();
        Map<String, Object> avgCompletionTime = financialGoalService.getAverageCompletionTime(userId);
        return ResponseEntity.ok(avgCompletionTime);
    }


    /**
     * Gets the current user ID from the security context
     */
    private Long getCurrentUserId() {
        // This should be implemented based on your security setup
        // For now, returning a default user ID for testing
        return 1L;
    }

}
