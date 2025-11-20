package com.finance_control.brazilian_market.client.model;

import java.util.List;

/**
 * Chart response from US Market API.
 */
public class ChartResponse {
    private ChartResult chart;

    public ChartResult getChart() {
        return chart;
    }

    public void setChart(ChartResult chart) {
        this.chart = chart;
    }

    public static class ChartResult {
        private List<ChartResultItem> result;
        private Object error;

        public List<ChartResultItem> getResult() {
            return result;
        }

        public void setResult(List<ChartResultItem> result) {
            this.result = result;
        }

        public Object getError() {
            return error;
        }

        public void setError(Object error) {
            this.error = error;
        }
    }

    public static class ChartResultItem {
        private Meta meta;
        private List<Long> timestamp;
        private Indicators indicators;

        public Meta getMeta() {
            return meta;
        }

        public void setMeta(Meta meta) {
            this.meta = meta;
        }

        public List<Long> getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(List<Long> timestamp) {
            this.timestamp = timestamp;
        }

        public Indicators getIndicators() {
            return indicators;
        }

        public void setIndicators(Indicators indicators) {
            this.indicators = indicators;
        }
    }
}

