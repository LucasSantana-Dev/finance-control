package com.finance_control.open_finance.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for Open Finance Account Information API.
 * Handles fetching accounts, balances, and transactions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class AccountInformationClient {

    @Qualifier("openFinanceRestClient")
    private final RestClient restClient;

    private static final String ACCOUNTS_ENDPOINT = "/open-banking/accounts/v1/accounts";
    private static final String BALANCES_ENDPOINT = "/open-banking/accounts/v1/balances";
    private static final String TRANSACTIONS_ENDPOINT = "/open-banking/accounts/v1/transactions";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;

    /**
     * Retrieves all accounts for a user from an institution.
     *
     * @param accessToken the OAuth access token
     * @return list of accounts
     */
    public List<Account> getAccounts(String accessToken) {
        log.debug("Fetching accounts from Open Finance API");

        return executeWithRetry(() -> {
            JsonNode response = restClient.get()
                    .uri(ACCOUNTS_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            List<Account> accounts = parseAccountsResponse(response);
            log.info("Successfully fetched {} accounts", accounts.size());
            return accounts;
        }, "Failed to fetch accounts");
    }

    /**
     * Retrieves account balance for a specific account.
     *
     * @param accessToken the OAuth access token
     * @param accountId the account ID
     * @return account balance
     */
    public AccountBalance getAccountBalance(String accessToken, String accountId) {
        log.debug("Fetching account balance for account: {}", accountId);

        return executeWithRetry(() -> {
            JsonNode response = restClient.get()
                    .uri(BALANCES_ENDPOINT + "/{accountId}", accountId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            AccountBalance balance = parseBalanceResponse(response, accountId);
            log.info("Successfully fetched balance for account: {}", accountId);
            return balance;
        }, "Failed to fetch balance for account " + accountId);
    }

    /**
     * Retrieves transactions for a specific account.
     *
     * @param accessToken the OAuth access token
     * @param accountId the account ID
     * @param fromDate optional start date for transaction range
     * @param toDate optional end date for transaction range
     * @param page page number for pagination
     * @param pageSize page size for pagination
     * @return paginated transactions
     */
    public TransactionListResponse getAccountTransactions(String accessToken, String accountId,
                                                                LocalDateTime fromDate, LocalDateTime toDate,
                                                                Integer page, Integer pageSize) {
        log.debug("Fetching transactions for account: {}", accountId);

        return executeWithRetry(() -> {
            JsonNode response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(TRANSACTIONS_ENDPOINT + "/{accountId}")
                                .queryParam("page", page != null ? page : 1)
                                .queryParam("page-size", pageSize != null ? pageSize : 100);
                        if (fromDate != null) {
                            builder.queryParam("fromBookingDateTime", fromDate.format(DateTimeFormatter.ISO_DATE_TIME));
                        }
                        if (toDate != null) {
                            builder.queryParam("toBookingDateTime", toDate.format(DateTimeFormatter.ISO_DATE_TIME));
                        }
                        return builder.build(accountId);
                    })
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            TransactionListResponse result = parseTransactionsResponse(response, accountId);
            log.info("Successfully fetched {} transactions for account: {}", result.getTransactions().size(), accountId);
            return result;
        }, "Failed to fetch transactions for account " + accountId);
    }

    /**
     * Retrieves account details for a specific account.
     *
     * @param accessToken the OAuth access token
     * @param accountId the account ID
     * @return account details
     */
    public Account getAccountDetails(String accessToken, String accountId) {
        log.debug("Fetching account details for account: {}", accountId);

        return executeWithRetry(() -> {
            JsonNode response = restClient.get()
                    .uri(ACCOUNTS_ENDPOINT + "/{accountId}", accountId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);

            Account account = parseAccountResponse(response);
            log.info("Successfully fetched account details: {}", accountId);
            return account;
        }, "Failed to fetch account details for " + accountId);
    }

    /**
     * Executes a request with retry logic for 5xx server errors.
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation, String errorMessage) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (RestClientException e) {
                lastException = e;
                HttpStatusCode statusCode = null;

                if (e instanceof HttpServerErrorException serverEx) {
                    statusCode = serverEx.getStatusCode();
                } else if (e instanceof HttpClientErrorException clientEx) {
                    statusCode = clientEx.getStatusCode();
                }

                if (statusCode != null && statusCode.is5xxServerError() && attempts < MAX_RETRIES - 1) {
                    attempts++;
                    long delay = RETRY_DELAY_MS * attempts;
                    log.warn("Server error ({}), retrying in {}ms (attempt {}/{})", statusCode, delay, attempts, MAX_RETRIES);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    log.error("{}: {}", errorMessage, e.getMessage());
                    throw new RuntimeException(errorMessage, e);
                }
            }
        }

        log.error("{} after {} attempts", errorMessage, MAX_RETRIES);
        throw new RuntimeException(errorMessage, lastException);
    }

    private List<Account> parseAccountsResponse(JsonNode jsonNode) {
        List<Account> accounts = new ArrayList<>();
        JsonNode data = jsonNode.path("data");
        if (data.isArray()) {
            for (JsonNode accountNode : data) {
                accounts.add(parseAccount(accountNode));
            }
        }
        return accounts;
    }

    private Account parseAccountResponse(JsonNode jsonNode) {
        JsonNode data = jsonNode.path("data");
        return parseAccount(data);
    }

    private Account parseAccount(JsonNode accountNode) {
        return Account.builder()
                .accountId(accountNode.path("accountId").asText())
                .accountType(accountNode.path("accountType").asText())
                .accountNumber(accountNode.path("number").asText())
                .branch(accountNode.path("branch").asText())
                .accountHolderName(accountNode.path("name").asText())
                .currency(accountNode.path("currency").asText("BRL"))
                .build();
    }

    private AccountBalance parseBalanceResponse(JsonNode jsonNode, String accountId) {
        JsonNode data = jsonNode.path("data");
        JsonNode balanceNode = data.path("balance");

        BigDecimal amount = balanceNode.has("amount") ?
                new BigDecimal(balanceNode.path("amount").asText()) : BigDecimal.ZERO;
        String currency = balanceNode.has("currency") ?
                balanceNode.path("currency").asText() : "BRL";

        return AccountBalance.builder()
                .accountId(accountId)
                .balance(amount)
                .currency(currency)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private TransactionListResponse parseTransactionsResponse(JsonNode jsonNode, String accountId) {
        List<Transaction> transactions = new ArrayList<>();
        JsonNode data = jsonNode.path("data");

        if (data.has("transaction")) {
            JsonNode transactionArray = data.path("transaction");
            if (transactionArray.isArray()) {
                for (JsonNode txNode : transactionArray) {
                    transactions.add(parseTransaction(txNode, accountId));
                }
            }
        }

        JsonNode meta = jsonNode.path("meta");
        int totalPages = meta.has("totalPages") ? meta.path("totalPages").asInt() : 1;
        int currentPage = meta.has("page") ? meta.path("page").asInt() : 1;

        return TransactionListResponse.builder()
                .accountId(accountId)
                .transactions(transactions)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }

    private Transaction parseTransaction(JsonNode txNode, String accountId) {
        BigDecimal amount = txNode.has("amount") ?
                new BigDecimal(txNode.path("amount").asText()) : BigDecimal.ZERO;

        String transactionId = txNode.has("transactionId") ?
                txNode.path("transactionId").asText() : null;

        String description = txNode.has("transactionInformation") ?
                txNode.path("transactionInformation").asText() : "";

        LocalDateTime bookingDate = null;
        if (txNode.has("bookingDateTime")) {
            try {
                bookingDate = LocalDateTime.parse(txNode.path("bookingDateTime").asText());
            } catch (Exception e) {
                log.warn("Failed to parse booking date: {}", txNode.path("bookingDateTime").asText());
            }
        }

        String creditDebitIndicator = txNode.has("creditDebitIndicator") ?
                txNode.path("creditDebitIndicator").asText() : "DEBIT";

        return Transaction.builder()
                .transactionId(transactionId)
                .accountId(accountId)
                .amount(amount)
                .description(description)
                .bookingDate(bookingDate)
                .creditDebitIndicator(creditDebitIndicator)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class Account {
        private String accountId;
        private String accountType;
        private String accountNumber;
        private String branch;
        private String accountHolderName;
        private String currency;
    }

    @lombok.Data
    @lombok.Builder
    public static class AccountBalance {
        private String accountId;
        private BigDecimal balance;
        private String currency;
        private LocalDateTime lastUpdated;
    }

    @lombok.Data
    @lombok.Builder
    public static class Transaction {
        private String transactionId;
        private String accountId;
        private BigDecimal amount;
        private String description;
        private LocalDateTime bookingDate;
        private String creditDebitIndicator;
    }

    @lombok.Data
    @lombok.Builder
    public static class TransactionListResponse {
        private String accountId;
        private List<Transaction> transactions;
        private int totalPages;
        private int currentPage;
    }
}
