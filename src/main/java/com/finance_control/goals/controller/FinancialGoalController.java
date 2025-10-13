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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/unified")
    @Operation(summary = "Get financial goals with filtering",
               description = "Retrieve financial goals with flexible filtering, sorting, and pagination options, or metadata")
    public ResponseEntity<Object> getFinancialGoals(
            @Parameter(description = "User ID for filtering")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Goal type filter")
            @RequestParam(required = false) com.finance_control.shared.enums.GoalType goalType,
            @Parameter(description = "Status filter (active/completed)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Search term for name or description")
            @RequestParam(required = false) String search,
            @Parameter(description = "Sort field")
            @RequestParam(required = false, defaultValue = "deadline") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Minimum target amount filter")
            @RequestParam(required = false) BigDecimal minTargetAmount,
            @Parameter(description = "Maximum target amount filter")
            @RequestParam(required = false) BigDecimal maxTargetAmount,
            @Parameter(description = "Deadline start date filter (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate deadlineStart,
            @Parameter(description = "Deadline end date filter (yyyy-MM-dd)")
            @RequestParam(required = false) LocalDate deadlineEnd,
            @Parameter(description = "Type of data to retrieve (metadata types: types, status-summary, progress-summary, deadline-alerts, completion-rate, average-completion-time)")
            @RequestParam(required = false) String data) {

        log.debug("GET request to retrieve financial goals with filtering");

        // If data parameter is provided, return metadata
        if (data != null && !data.trim().isEmpty()) {
            return switch (data) {
                case "types" -> ResponseEntity.ok(financialGoalService.getGoalTypes());
                case "status-summary" -> ResponseEntity.ok(financialGoalService.getStatusSummary(userId));
                case "progress-summary" -> ResponseEntity.ok(financialGoalService.getProgressSummary(userId));
                case "deadline-alerts" -> ResponseEntity.ok(financialGoalService.getDeadlineAlerts(userId));
                case "completion-rate" -> ResponseEntity.ok(financialGoalService.getCompletionRate(userId));
                case "average-completion-time" -> ResponseEntity.ok(financialGoalService.getAverageCompletionTime(userId));
                default -> throw new IllegalArgumentException("Invalid data type: " + data);
            };
        }

        // Create filters map
        java.util.Map<String, Object> filters = new java.util.HashMap<>();
        if (goalType != null) {
            filters.put("goalType", goalType);
        }
        if (search != null && !search.trim().isEmpty()) {
            filters.put("name", search);
        }

        // Handle status filter
        if ("active".equalsIgnoreCase(status)) {
            filters.put("isActive", true);
        } else if ("completed".equalsIgnoreCase(status)) {
            filters.put("isActive", false);
        }

        // Handle amount range filters
        if (minTargetAmount != null) {
            filters.put("minTargetAmount", minTargetAmount);
        }
        if (maxTargetAmount != null) {
            filters.put("maxTargetAmount", maxTargetAmount);
        }

        // Handle deadline range filters
        if (deadlineStart != null) {
            filters.put("deadlineStart", deadlineStart);
        }
        if (deadlineEnd != null) {
            filters.put("deadlineEnd", deadlineEnd);
        }

        // Create pageable with sorting
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FinancialGoalDTO> goals = financialGoalService.findAll(search, filters, sortBy, sortDirection, pageable);
        return ResponseEntity.ok(goals);
    }

}
