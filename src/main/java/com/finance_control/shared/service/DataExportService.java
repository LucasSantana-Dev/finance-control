package com.finance_control.shared.service;

import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.goals.repository.FinancialGoalRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting user data in various formats.
 * Provides data portability and backup capabilities.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialGoalRepository financialGoalRepository;

    /**
     * Export user data as CSV format.
     */
    public byte[] exportUserDataAsCsv() {
        Long userId = UserContext.getCurrentUserId();
        log.info("Exporting user data as CSV for user: {}", userId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // Write CSV header
            writer.println("Finance Control Data Export");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.println("User ID: " + userId);
            writer.println();

            // Export user profile
            exportUserProfile(writer, userId);

            // Export transactions
            exportTransactions(writer, userId);

            // Export financial goals
            exportFinancialGoals(writer, userId);

            writer.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting user data as CSV for user: {}", userId, e);
            throw new RuntimeException("Failed to export user data", e);
        }
    }

    /**
     * Export user data as JSON format.
     */
    public String exportUserDataAsJson() {
        Long userId = UserContext.getCurrentUserId();
        log.info("Exporting user data as JSON for user: {}", userId);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"exportInfo\": {\n");
        json.append("    \"generatedAt\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
        json.append("    \"userId\": ").append(userId).append(",\n");
        json.append("    \"version\": \"1.0\"\n");
        json.append("  },\n");

        // Export user profile
        json.append("  \"userProfile\": ");
        json.append(exportUserProfileAsJson(userId));
        json.append(",\n");

        // Export transactions
        json.append("  \"transactions\": ");
        json.append(exportTransactionsAsJson(userId));
        json.append(",\n");

        // Export financial goals
        json.append("  \"financialGoals\": ");
        json.append(exportFinancialGoalsAsJson(userId));
        json.append("\n");

        json.append("}\n");
        return json.toString();
    }

    /**
     * Export transactions as CSV.
     */
    public byte[] exportTransactionsAsCsv() {
        Long userId = UserContext.getCurrentUserId();
        log.info("Exporting transactions as CSV for user: {}", userId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // Write CSV header
            writer.println("Transaction Export");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.println("User ID: " + userId);
            writer.println();

            exportTransactions(writer, userId);
            writer.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting transactions as CSV for user: {}", userId, e);
            throw new RuntimeException("Failed to export transactions", e);
        }
    }

    /**
     * Export financial goals as CSV.
     */
    public byte[] exportFinancialGoalsAsCsv() {
        Long userId = UserContext.getCurrentUserId();
        log.info("Exporting financial goals as CSV for user: {}", userId);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // Write CSV header
            writer.println("Financial Goals Export");
            writer.println("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.println("User ID: " + userId);
            writer.println();

            exportFinancialGoals(writer, userId);
            writer.flush();
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting financial goals as CSV for user: {}", userId, e);
            throw new RuntimeException("Failed to export financial goals", e);
        }
    }

    private void exportUserProfile(PrintWriter writer, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        writer.println("=== USER PROFILE ===");
        writer.println("ID,Email,Name,Created At,Is Active");
        writer.printf("%d,%s,%s,%s,%s%n",
                user.getId(),
                user.getEmail(),
                user.getProfile() != null && user.getProfile().getFullName() != null ? user.getProfile().getFullName() : "",
                user.getCreatedAt() != null ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                user.getIsActive());
        writer.println();
    }

    private String exportUserProfileAsJson(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fullName = user.getProfile() != null && user.getProfile().getFullName() != null
                ? user.getProfile().getFullName() : "";
        String createdAt = user.getCreatedAt() != null
                ? user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("    \"id\": ").append(user.getId()).append(",\n");
        json.append("    \"email\": \"").append(user.getEmail()).append("\",\n");
        json.append("    \"name\": \"").append(fullName).append("\",\n");
        json.append("    \"createdAt\": \"").append(createdAt).append("\",\n");
        json.append("    \"isActive\": ").append(user.getIsActive()).append("\n");
        json.append("  }");
        return json.toString();
    }

    private void exportTransactions(PrintWriter writer, Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(userId);

        writer.println("=== TRANSACTIONS ===");
        writer.println("ID,Description,Amount,Type,Date,Category,Subcategory,Source Entity,Reconciled,Created At");

        for (Transaction transaction : transactions) {
            writer.printf("%d,\"%s\",%s,%s,%s,\"%s\",\"%s\",\"%s\",%s,%s%n",
                    transaction.getId(),
                    transaction.getDescription() != null ? transaction.getDescription().replace("\"", "\"\"") : "",
                    transaction.getAmount(),
                    transaction.getType(),
                    transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    transaction.getCategory() != null ? transaction.getCategory().getName().replace("\"", "\"\"") : "",
                    transaction.getSubcategory() != null ? transaction.getSubcategory().getName().replace("\"", "\"\"") : "",
                    transaction.getSourceEntity() != null ? transaction.getSourceEntity().getName().replace("\"", "\"\"") : "",
                    transaction.getReconciled(),
                    transaction.getCreatedAt() != null ? transaction.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
        }
        writer.println();
    }

    /**
     * Formats a LocalDateTime timestamp to ISO format, handling null values.
     *
     * @param timestamp the timestamp to format
     * @return formatted timestamp string or empty string if null
     */
    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp != null ? timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
    }

    private String exportTransactionsAsJson(Long userId) {
        List<Transaction> transactions = transactionRepository.findByUserIdWithResponsibilities(userId);

        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);

            String description = transaction.getDescription() != null
                    ? transaction.getDescription().replace("\"", "\\\"") : "";
            String categoryName = transaction.getCategory() != null
                    ? transaction.getCategory().getName().replace("\"", "\\\"") : "";
            String subcategoryName = transaction.getSubcategory() != null
                    ? transaction.getSubcategory().getName().replace("\"", "\\\"") : "";
            String sourceEntityName = transaction.getSourceEntity() != null
                    ? transaction.getSourceEntity().getName().replace("\"", "\\\"") : "";

            json.append("    {\n");
            json.append("      \"id\": ").append(transaction.getId()).append(",\n");
            json.append("      \"description\": \"").append(description).append("\",\n");
            json.append("      \"amount\": ").append(transaction.getAmount()).append(",\n");
            json.append("      \"type\": \"").append(transaction.getType()).append("\",\n");
            json.append("      \"date\": \"").append(transaction.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
            json.append("      \"category\": \"").append(categoryName).append("\",\n");
            json.append("      \"subcategory\": \"").append(subcategoryName).append("\",\n");
            json.append("      \"sourceEntity\": \"").append(sourceEntityName).append("\",\n");
            json.append("      \"reconciled\": ").append(transaction.getReconciled()).append(",\n");
            json.append("      \"createdAt\": \"").append(formatTimestamp(transaction.getCreatedAt())).append("\"\n");
            json.append("    }");
            if (i < transactions.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private void exportFinancialGoals(PrintWriter writer, Long userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<FinancialGoal> goals = financialGoalRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).getContent();

        writer.println("=== FINANCIAL GOALS ===");
        writer.println("ID,Name,Description,Target Amount,Current Amount,Progress Percentage,Goal Type,Deadline,Is Active,Created At");

        for (FinancialGoal goal : goals) {
            writer.printf("%d,\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,%s%n",
                    goal.getId(),
                    goal.getName() != null ? goal.getName().replace("\"", "\"\"") : "",
                    goal.getDescription() != null ? goal.getDescription().replace("\"", "\"\"") : "",
                    goal.getTargetAmount(),
                    goal.getCurrentAmount(),
                    goal.getProgressPercentage(),
                    goal.getGoalType(),
                    goal.getDeadline() != null ? goal.getDeadline().format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                    goal.getIsActive(),
                    goal.getCreatedAt() != null ? goal.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
        }
        writer.println();
    }

    private String exportFinancialGoalsAsJson(Long userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<FinancialGoal> goals = financialGoalRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .getContent();

        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < goals.size(); i++) {
            FinancialGoal goal = goals.get(i);

            String name = goal.getName() != null ? goal.getName().replace("\"", "\\\"") : "";
            String description = goal.getDescription() != null ? goal.getDescription().replace("\"", "\\\"") : "";
            String deadline = goal.getDeadline() != null ? goal.getDeadline().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";

            json.append("    {\n");
            json.append("      \"id\": ").append(goal.getId()).append(",\n");
            json.append("      \"name\": \"").append(name).append("\",\n");
            json.append("      \"description\": \"").append(description).append("\",\n");
            json.append("      \"targetAmount\": ").append(goal.getTargetAmount()).append(",\n");
            json.append("      \"currentAmount\": ").append(goal.getCurrentAmount()).append(",\n");
            json.append("      \"progressPercentage\": ").append(goal.getProgressPercentage()).append(",\n");
            json.append("      \"goalType\": \"").append(goal.getGoalType()).append("\",\n");
            json.append("      \"deadline\": \"").append(deadline).append("\",\n");
            json.append("      \"isActive\": ").append(goal.getIsActive()).append(",\n");
            json.append("      \"createdAt\": \"").append(formatTimestamp(goal.getCreatedAt())).append("\"\n");
            json.append("    }");
            if (i < goals.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }
}
