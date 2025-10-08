package com.finance_control.goals.repository;

import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.shared.enums.GoalType;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialGoalRepository extends BaseRepository<FinancialGoal, Long> {

    @Query("SELECT g FROM FinancialGoal g WHERE g.user.id = :userId " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<FinancialGoal> findAll(@Param("search") String search, @Param("userId") Long userId, Pageable pageable);

       Page<FinancialGoal> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

       List<FinancialGoal> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

       List<FinancialGoal> findByUserIdAndGoalTypeOrderByCreatedAtDesc(Long userId, GoalType goalType);

       @Query("SELECT g FROM FinancialGoal g WHERE g.user.id = :userId " +
                     "AND g.deadline IS NOT NULL AND g.deadline <= :maxDate " +
                     "AND g.isActive = true AND g.currentAmount < g.targetAmount " +
                     "ORDER BY g.deadline ASC")
       List<FinancialGoal> findGoalsNearDeadline(@Param("userId") Long userId,
                     @Param("maxDate") java.time.LocalDate maxDate);

       @Query("SELECT g FROM FinancialGoal g WHERE g.user.id = :userId " +
                     "AND g.currentAmount >= g.targetAmount " +
                     "ORDER BY g.updatedAt DESC")
       List<FinancialGoal> findCompletedGoals(@Param("userId") Long userId);
}
