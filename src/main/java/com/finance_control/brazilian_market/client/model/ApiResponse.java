package com.finance_control.brazilian_market.client.model;

import java.util.List;

/**
 * Response wrapper for US Market API quote requests.
 */
public class ApiResponse {
    private QuoteResponseWrapper quoteResponse;

    public QuoteResponseWrapper getQuoteResponse() {
        return quoteResponse;
    }

    public void setQuoteResponse(QuoteResponseWrapper quoteResponse) {
        this.quoteResponse = quoteResponse;
    }

    /**
     * Quote response wrapper containing list of quotes.
     */
    public static class QuoteResponseWrapper {
        private List<QuoteResponse> result;
        private Object error;

        public List<QuoteResponse> getResult() {
            return result != null ? java.util.Collections.unmodifiableList(result) : null;
        }

        public void setResult(List<QuoteResponse> result) {
            this.result = result;
        }

        public Object getError() {
            return error;
        }

        public void setError(Object error) {
            this.error = error;
        }
    }
}

