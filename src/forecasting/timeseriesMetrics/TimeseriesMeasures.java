package forecasting.timeseriesMetrics;

/**
 * Enum for the available time series metrics.
 */
public enum TimeseriesMeasures {
    KURTOSIS {
        public TimeseriesMetric create() {
            return new Kurtosis();
        }
    },
    LJUNGBOX {
        public TimeseriesMetric create() {
            return new LjungBoxTest();
        }
    },
    NONLINEARITY {
        public TimeseriesMetric create() {
            return new NonLinearity();
        }
    },
    SEASONALITY {
        public TimeseriesMetric create() {
            return new Seasonality();
        }
    },
    SELFSIMILARITY {
        public TimeseriesMetric create() {
            return new SelfSimilarity();
        }
    },
    SKEWNESS {
        public TimeseriesMetric create() {
            return new Skewness();
        }
    },
    STATIONARITY {
        public TimeseriesMetric create() {
            return new Stationarity();
        }
    },
    TRENDSTATIONARY {
        public TimeseriesMetric create() {
            return new TrendStationary();
        }
    };


    public abstract TimeseriesMetric create();
}
