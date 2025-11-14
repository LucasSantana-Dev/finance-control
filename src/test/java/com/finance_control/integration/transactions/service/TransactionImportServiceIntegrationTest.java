package com.finance_control.integration.transactions.service;

import com.finance_control.integration.BaseIntegrationTest;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.transactions.dto.importer.TransactionImportRequest;
import com.finance_control.transactions.dto.importer.TransactionImportResponse;
import com.finance_control.transactions.importer.DuplicateHandlingStrategy;
import com.finance_control.transactions.model.Transaction;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.responsibles.TransactionResponsiblesRepository;
import com.finance_control.transactions.service.TransactionImportService;
import com.finance_control.unit.TestUtils;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionImportServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionImportService transactionImportService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionCategoryRepository categoryRepository;

    @Autowired
    private TransactionResponsiblesRepository responsiblesRepository;

    private User user;
    private TransactionCategory category;
    private TransactionResponsibles responsible;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("import@test.com");
        user.setPassword("password123");
        user.setIsActive(true);
        user = userRepository.save(user);

        category = new TransactionCategory();
        category.setName("General");
        category = categoryRepository.save(category);

        responsible = new TransactionResponsibles();
        responsible.setName("Primary");
        responsible = responsiblesRepository.save(responsible);

        TestUtils.setupUserContext(user.getId());
    }

    @AfterEach
    void tearDown() {
        TestUtils.clearUserContext();
    }

    @Test
    void shouldImportCsvStatement() {
        String csvContent = """
                date;description;amount
                2024-01-05;Supermarket;-125,75
                2024-01-06;Freelance Payment;850.50
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        TransactionImportRequest request = TransactionImportRequest.builder()
                .userId(user.getId())
                .defaultCategoryId(category.getId())
                .defaultSubtype(TransactionSubtype.FIXED)
                .defaultSource(TransactionSource.BANK_TRANSACTION)
                .responsibilities(List.of(TransactionImportRequest.ResponsibilityAllocation.builder()
                        .responsibleId(responsible.getId())
                        .percentage(BigDecimal.valueOf(100))
                        .build()))
                .csv(TransactionImportRequest.CsvConfiguration.builder()
                        .containsHeader(true)
                        .delimiter(";")
                        .dateColumn("date")
                        .descriptionColumn("description")
                        .amountColumn("amount")
                        .locale("pt-BR")
                        .build())
                .build();

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response.getTotalEntries()).isEqualTo(2);
        assertThat(response.getCreatedTransactions()).isEqualTo(2);
        assertThat(response.getIssues()).isEmpty();

        List<Transaction> persisted = transactionRepository.findAll();
        assertThat(persisted).hasSize(2);
        assertThat(persisted).anySatisfy(transaction -> {
            assertThat(transaction.getDescription()).isEqualTo("Supermarket");
            assertThat(transaction.getType()).isEqualTo(com.finance_control.shared.enums.TransactionType.EXPENSE);
        });
        assertThat(persisted).anySatisfy(transaction -> {
            assertThat(transaction.getDescription()).isEqualTo("Freelance Payment");
            assertThat(transaction.getType()).isEqualTo(com.finance_control.shared.enums.TransactionType.INCOME);
        });
    }

    @Test
    void shouldImportOfxStatement() {
        String ofxContent = """
                OFXHEADER:100
                DATA:OFXSGML
                VERSION:102
                SECURITY:NONE
                ENCODING:USASCII
                CHARSET:1252
                COMPRESSION:NONE
                OLDFILEUID:NONE
                NEWFILEUID:NONE

                <OFX>
                  <BANKMSGSRSV1>
                    <STMTTRNRS>
                      <TRNUID>1</TRNUID>
                      <STATUS>
                        <CODE>0</CODE>
                        <SEVERITY>INFO</SEVERITY>
                      </STATUS>
                      <STMTRS>
                        <CURDEF>BRL</CURDEF>
                        <BANKACCTFROM>
                          <BANKID>111</BANKID>
                          <ACCTID>999</ACCTID>
                          <ACCTTYPE>CHECKING</ACCTTYPE>
                        </BANKACCTFROM>
                        <BANKTRANLIST>
                          <DTSTART>20240101000000[-3:BRT]</DTSTART>
                          <DTEND>20240131000000[-3:BRT]</DTEND>
                          <STMTTRN>
                            <TRNTYPE>DEBIT</TRNTYPE>
                            <DTPOSTED>20240105000000[-3:BRT]</DTPOSTED>
                            <TRNAMT>-200.00</TRNAMT>
                            <FITID>OFX1</FITID>
                            <NAME>Utility Bill</NAME>
                            <MEMO>Electric company</MEMO>
                          </STMTTRN>
                          <STMTTRN>
                            <TRNTYPE>CREDIT</TRNTYPE>
                            <DTPOSTED>20240110000000[-3:BRT]</DTPOSTED>
                            <TRNAMT>1500.00</TRNAMT>
                            <FITID>OFX2</FITID>
                            <NAME>Monthly Salary</NAME>
                          </STMTTRN>
                        </BANKTRANLIST>
                      </STMTRS>
                    </STMTTRNRS>
                  </BANKMSGSRSV1>
                </OFX>
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statement.ofx",
                "application/x-ofx",
                ofxContent.getBytes(StandardCharsets.UTF_8));

        TransactionImportRequest request = TransactionImportRequest.builder()
                .userId(user.getId())
                .defaultCategoryId(category.getId())
                .defaultSubtype(TransactionSubtype.VARIABLE)
                .defaultSource(TransactionSource.BANK_TRANSACTION)
                .responsibilities(List.of(TransactionImportRequest.ResponsibilityAllocation.builder()
                        .responsibleId(responsible.getId())
                        .percentage(BigDecimal.valueOf(100))
                        .build()))
                .build();

        TransactionImportResponse response = transactionImportService.importStatements(file, request);

        assertThat(response.getCreatedTransactions()).isEqualTo(2);
        assertThat(response.getIssues()).isEmpty();

        List<Transaction> persisted = transactionRepository.findAll();
        assertThat(persisted).hasSize(2);
        assertThat(persisted).anySatisfy(transaction -> {
            assertThat(transaction.getDescription()).isEqualTo("Electric company");
            assertThat(transaction.getType()).isEqualTo(com.finance_control.shared.enums.TransactionType.EXPENSE);
        });
        assertThat(persisted).anySatisfy(transaction -> {
            assertThat(transaction.getDescription()).isEqualTo("Monthly Salary");
            assertThat(transaction.getType()).isEqualTo(com.finance_control.shared.enums.TransactionType.INCOME);
        });
    }

    @Test
    void shouldSkipDuplicatesWhenStrategyIsSkip() {
        String csvContent = """
                date;description;amount
                2024-02-01;Subscription;-30.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statement.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8));

        TransactionImportRequest request = TransactionImportRequest.builder()
                .userId(user.getId())
                .defaultCategoryId(category.getId())
                .defaultSubtype(TransactionSubtype.FIXED)
                .defaultSource(TransactionSource.BANK_TRANSACTION)
                .duplicateStrategy(DuplicateHandlingStrategy.SKIP)
                .responsibilities(List.of(TransactionImportRequest.ResponsibilityAllocation.builder()
                        .responsibleId(responsible.getId())
                        .percentage(BigDecimal.valueOf(100))
                        .build()))
                .csv(TransactionImportRequest.CsvConfiguration.builder()
                        .containsHeader(true)
                        .delimiter(";")
                        .dateColumn("date")
                        .descriptionColumn("description")
                        .amountColumn("amount")
                        .locale("en-US")
                        .build())
                .build();

        TransactionImportResponse first = transactionImportService.importStatements(file, request);
        assertThat(first.getCreatedTransactions()).isEqualTo(1);

        TransactionImportResponse second = transactionImportService.importStatements(file, request);
        assertThat(second.getCreatedTransactions()).isZero();
        assertThat(second.getDuplicateEntries()).isEqualTo(1);
        assertThat(second.getIssues()).anyMatch(issue -> issue.getType() == com.finance_control.transactions.importer.TransactionImportIssueType.DUPLICATE_SKIPPED);
    }
}
