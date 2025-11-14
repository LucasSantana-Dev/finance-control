package com.finance_control.open_finance.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
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

    @Qualifier("openFinanceWebClient")
    private final WebClient webClient;

    private static final String ACCOUNTS_ENDPOINT = "/open-banking/accounts/v1/accounts";
    private static final String BALANCES_ENDPOINT = "/open-banking/accounts/v1/balances";
    private static final String TRANSACTIONS_ENDPOINT = "/open-banking/accounts/v1/transactions";

    /**
     * Retrieves all accounts for a user from an institution.
     *
     * @param accessToken the OAuth access token
     * @return list of accounts
     */
    public Mono<List<Account>> getAccounts(String accessToken) {
        log.debug("Fetching accounts from Open Finance API");

        return webClient.get()
                .uri(ACCOUNTS_ENDPOINT)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseAccountsResponse)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(accounts -> log.info("Successfully fetched {} accounts", accounts.size()))
                .doOnError(error -> log.error("Failed to fetch accounts: {}", error.getMessage()));
    }

    /**
     * Retrieves account balance for a specific account.
     *
     * @param accessToken the OAuth access token
     * @param accountId the account ID
     * @return account balance
     */
    public Mono<AccountBalance> getAccountBalance(String accessToken, String accountId) {
        log.debug("Fetching account balance for account: {}", accountId);

        return webClient.get()
                .uri(BALANCES_ENDPOINT + "/{accountId}", accountId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> parseBalanceResponse(jsonNode, accountId))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(balance -> log.info("Successfully fetched balance for account: {}", accountId))
                .doOnError(error -> log.error("Failed to fetch balance for account {}: {}", accountId, error.getMessage()));
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
    public Mono<TransactionListResponse> getAccountTransactions(String accessToken, String accountId,
                                                                LocalDateTime fromDate, LocalDateTime toDate,
                                                                Integer page, Integer pageSize) {
        log.debug("Fetching transactions for account: {}", accountId);

        var uriBuilder = webClient.get()
                .uri(uriBuilder1 -> {
                    var builder = uriBuilder1.path(TRANSACTIONS_ENDPOINT + "/{accountId}")
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        return uriBuilder
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> parseTransactionsResponse(jsonNode, accountId))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(response -> log.info("Successfully fetched {} transactions for account: {}",
                                                  response.getTransactions().size(), accountId))
                .doOnError(error -> log.error("Failed to fetch transactions for account {}: {}", accountId, error.getMessage()));
    }

    /**
     * Retrieves account details for a specific account.
     *
     * @param accessToken the OAuth access token
     * @param accountId the account ID
     * @return account details
     */
    public Mono<Account> getAccountDetails(String accessToken, String accountId) {
        log.debug("Fetching account details for account: {}", accountId);

        return webClient.get()
                .uri(ACCOUNTS_ENDPOINT + "/{accountId}", accountId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> parseAccountResponse(jsonNode))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .doOnSuccess(account -> log.info("Successfully fetched account details: {}", accountId))
                .doOnError(error -> log.error("Failed to fetch account details for {}: {}", accountId, error.getMessage()));
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
