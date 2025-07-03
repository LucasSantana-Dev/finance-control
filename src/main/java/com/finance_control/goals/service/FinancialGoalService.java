package com.finance_control.goals.service;

import com.finance_control.goals.dto.FinancialGoalDTO;
import com.finance_control.goals.dto.GoalCompletionRequest;
import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.service.BaseService;
import com.finance_control.shared.util.EntityMapper;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FinancialGoalService extends BaseService<FinancialGoal, Long, FinancialGoalDTO> {

    private static final String GOAL_TYPE_FIELD = "goalType";

    private final FinancialGoalRepository financialGoalRepository;
    private final UserRepository userRepository;
    private final TransactionSourceRepository transactionSourceRepository;

    public FinancialGoalService(FinancialGoalRepository financialGoalRepository,
            UserRepository userRepository,
            TransactionSourceRepository transactionSourceRepository) {
        super(financialGoalRepository);
        this.financialGoalRepository = financialGoalRepository;
        this.userRepository = userRepository;
        this.transactionSourceRepository = transactionSourceRepository;
    }

    @Override
    protected boolean isUserAware() {
        return true;
    }

    /**
     * Find active goals for the current user.
     * 
     * @return list of active financial goals
     */
    public List<FinancialGoalDTO> findActiveGoals() {
        Map<String, Object> filters = Map.of(IS_ACTIVE_FIELD, true);
        return findAll(null, filters, "deadline", "asc", Pageable.unpaged()).getContent();
    }

    /**
     * Find completed goals for the current user.
     * 
     * @return list of completed financial goals
     */
    public List<FinancialGoalDTO> findCompletedGoals() {
        Map<String, Object> filters = Map.of(IS_ACTIVE_FIELD, false);
        return findAll(null, filters, "updatedAt", "desc", Pageable.unpaged()).getContent();
    }

    /**
     * Update the current amount of a goal.
     * 
     * @param id     the goal ID
     * @param amount the amount to add to current amount
     * @return the updated goal
     */
    public FinancialGoalDTO updateProgress(Long id, BigDecimal amount) {
        FinancialGoal goal = getEntityById(id);
        BigDecimal newAmount = goal.getCurrentAmount().add(amount);
        goal.setCurrentAmount(newAmount);

        // Check if goal is completed
        if (goal.isCompleted()) {
            goal.setIsActive(false); // Mark as inactive (completed)
        }

        FinancialGoal savedGoal = financialGoalRepository.save(goal);
        return mapToResponseDTO(savedGoal);
    }

    /**
     * Mark a goal as completed.
     * 
     * @param id the goal ID
     * @return the updated goal
     */
    public FinancialGoalDTO markAsCompleted(Long id) {
        FinancialGoal goal = getEntityById(id);
        goal.setIsActive(false);
        FinancialGoal savedGoal = financialGoalRepository.save(goal);
        return mapToResponseDTO(savedGoal);
    }

    /**
     * Reactivate a goal.
     * 
     * @param id the goal ID
     * @return the updated goal
     */
    public FinancialGoalDTO reactivate(Long id) {
        FinancialGoal goal = getEntityById(id);
        goal.setIsActive(true);
        FinancialGoal savedGoal = financialGoalRepository.save(goal);
        return mapToResponseDTO(savedGoal);
    }

    @Override
    protected FinancialGoal mapToEntity(FinancialGoalDTO createDTO) {
        FinancialGoal goal = new FinancialGoal();
        goal.setName(createDTO.getName());
        goal.setDescription(createDTO.getDescription());
        goal.setGoalType(createDTO.getGoalType());
        goal.setTargetAmount(createDTO.getTargetAmount());
        goal.setCurrentAmount(createDTO.getCurrentAmount() != null ? createDTO.getCurrentAmount() : BigDecimal.ZERO);
        goal.setDeadline(createDTO.getDeadline() != null ? createDTO.getDeadline().toLocalDate() : null);
        goal.setAutoCalculate(Boolean.TRUE.equals(createDTO.getAutoCalculate()));
        goal.setIsActive(true);

        // Set account if provided
        if (createDTO.getAccountId() != null) {
            TransactionSourceEntity account = transactionSourceRepository.findById(createDTO.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            goal.setAccount(account);
        }

        return goal;
    }

    @Override
    protected void updateEntityFromDTO(FinancialGoal entity, FinancialGoalDTO updateDTO) {
        if (updateDTO.getName() != null) {
            entity.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            entity.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getGoalType() != null) {
            entity.setGoalType(updateDTO.getGoalType());
        }
        if (updateDTO.getTargetAmount() != null) {
            entity.setTargetAmount(updateDTO.getTargetAmount());
        }
        if (updateDTO.getCurrentAmount() != null) {
            entity.setCurrentAmount(updateDTO.getCurrentAmount());
        }
        if (updateDTO.getDeadline() != null) {
            entity.setDeadline(updateDTO.getDeadline().toLocalDate());
        }
        if (updateDTO.getAutoCalculate() != null) {
            entity.setAutoCalculate(updateDTO.getAutoCalculate());
        }
        if (updateDTO.getAccountId() != null) {
            TransactionSourceEntity account = transactionSourceRepository.findById(updateDTO.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            entity.setAccount(account);
        }
    }

    @Override
    protected FinancialGoalDTO mapToResponseDTO(FinancialGoal entity) {
        FinancialGoalDTO dto = new FinancialGoalDTO();
        EntityMapper.mapCommonFields(entity, dto);

        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setGoalType(entity.getGoalType());
        dto.setTargetAmount(entity.getTargetAmount());
        dto.setCurrentAmount(entity.getCurrentAmount());
        dto.setDeadline(entity.getDeadline() != null ? entity.getDeadline().atStartOfDay() : null);
        dto.setAutoCalculate(entity.getAutoCalculate());
        dto.setIsActive(entity.getIsActive());

        if (entity.getAccount() != null) {
            dto.setAccountId(entity.getAccount().getId());
        }

        return dto;
    }

    @Override
    protected void validateCreateDTO(FinancialGoalDTO createDTO) {
        createDTO.validateCreate();
    }

    @Override
    protected void validateUpdateDTO(FinancialGoalDTO updateDTO) {
        updateDTO.validateUpdate();
    }

    @Override
    protected String getEntityName() {
        return "FinancialGoal";
    }

    @Override
    protected boolean belongsToUser(FinancialGoal entity, Long userId) {
        return entity.getUser().getId().equals(userId);
    }

    @Override
    protected void setUserId(FinancialGoal entity, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        entity.setUser(user);
    }

    @Override
    protected Specification<FinancialGoal> createSpecificationFromFilters(String search, Map<String, Object> filters) {
        Specification<FinancialGoal> spec = super.createSpecificationFromFilters(search, filters);

        if (filters != null) {
            if (filters.containsKey(IS_ACTIVE_FIELD)) {
                Boolean isActive = (Boolean) filters.get(IS_ACTIVE_FIELD);
                spec = spec.and(
                        (root, query, cb) -> cb.equal(root.get(IS_ACTIVE_FIELD), isActive));
            }
            if (filters.containsKey(GOAL_TYPE_FIELD)) {
                String goalType = (String) filters.get(GOAL_TYPE_FIELD);
                spec = spec.and((root, query, cb) -> cb.equal(root.get(GOAL_TYPE_FIELD), goalType));
            }
            if (filters.containsKey("name")) {
                String name = (String) filters.get("name");
                if (name != null && !name.trim().isEmpty()) {
                    spec = spec
                            .and((root, query, cb) -> cb.like(cb.lower(root.get("name")),
                                    "%" + name.toLowerCase() + "%"));
                }
            }
        }

        return spec;
    }
    
    /**
     * Complete a financial goal with detailed completion data.
     * 
     * @param id the ID of the goal to complete
     * @param request the completion request data
     * @return the completed goal DTO
     */
    public FinancialGoalDTO completeGoal(Long id, GoalCompletionRequest request) {
        FinancialGoal goal = getEntityById(id);
        
        // Update completion fields
        goal.setCurrentAmount(request.getFinalAmount());
        goal.setIsActive(false); // Mark as completed
        goal.setCompletedDate(request.getCompletionDate());
        goal.setCompleted(request.getCompleted());
        goal.setCompletionNotes(request.getCompletionNotes());
        goal.setAchievementNotes(request.getAchievementNotes());
        goal.setActualSavings(request.getActualSavings());
        goal.setActualInvestment(request.getActualInvestment());
        
        FinancialGoal savedGoal = financialGoalRepository.save(goal);
        return mapToResponseDTO(savedGoal);
    }
}