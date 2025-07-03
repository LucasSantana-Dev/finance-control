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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
} 