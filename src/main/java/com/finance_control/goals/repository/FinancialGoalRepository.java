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
import java.util.Map;

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

    @Query("SELECT DISTINCT g.goalType FROM FinancialGoal g")
    List<String> findDistinctGoalTypes();

    @Query("SELECT " +
           "COUNT(CASE WHEN g.isActive = true THEN 1 END) as activeCount, " +
           "COUNT(CASE WHEN g.isActive = false THEN 1 END) as completedCount, " +
           "COUNT(*) as totalCount " +
           "FROM FinancialGoal g WHERE g.user.id = :userId")
    Map<String, Object> getStatusSummary(@Param("userId") Long userId);

    @Query("SELECT " +
           "AVG(CASE WHEN g.isActive = false THEN (g.currentAmount / g.targetAmount) * 100 END) as avgCompletionRate, " +
           "SUM(g.currentAmount) as totalCurrentAmount, " +
           "SUM(g.targetAmount) as totalTargetAmount " +
           "FROM FinancialGoal g WHERE g.user.id = :userId")
    Map<String, Object> getProgressSummary(@Param("userId") Long userId);

    @Query("SELECT g.id, g.name, g.deadline, g.currentAmount, g.targetAmount " +
           "FROM FinancialGoal g WHERE g.user.id = :userId " +
           "AND g.deadline IS NOT NULL AND g.deadline <= :alertDate " +
           "AND g.isActive = true AND g.currentAmount < g.targetAmount " +
           "ORDER BY g.deadline ASC")
    List<Map<String, Object>> getDeadlineAlerts(@Param("userId") Long userId, @Param("alertDate") java.time.LocalDate alertDate);

    @Query("SELECT " +
           "COUNT(CASE WHEN g.isActive = false THEN 1 END) * 100.0 / COUNT(*) as completionRate " +
           "FROM FinancialGoal g WHERE g.user.id = :userId")
    Map<String, Object> getCompletionRate(@Param("userId") Long userId);

    @Query("SELECT " +
           "AVG(TIMESTAMPDIFF(DAY, g.createdAt, g.updatedAt)) as avgCompletionDays " +
           "FROM FinancialGoal g WHERE g.user.id = :userId AND g.isActive = false")
    Map<String, Object> getAverageCompletionTime(@Param("userId") Long userId);
}
