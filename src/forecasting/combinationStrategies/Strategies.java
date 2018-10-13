package forecasting.combinationStrategies;

import forecasting.combinationStrategies.ann.ANNWeighting;
import forecasting.combinationStrategies.dlc.DLCWeighting;
import forecasting.combinationStrategies.xcsf.XCSF;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Enumeration for all combination strategies.
 * Factory for the creation of a strategy.
 */
public enum Strategies {
    ANN_WEIGHTING {
        @NotNull
        public CombinationStrategy create() {
            return new ANNWeighting();
        }
    },
    SIMPLE_AVERAGE {
        @NotNull
        public CombinationStrategy create() {
            return new SimpleAverage();
        }
    },
    FORECAST_ERROR {
        @NotNull
        public CombinationStrategy create() {
            return new ForecastError();
        }
    },
    OUTPERFORMANCE {
        @NotNull
        public CombinationStrategy create() {
            return new Outperformance();
        }
    },
    OPTIMALWEIGHTS {
        @NotNull
        public CombinationStrategy create() {
            return new OptimalWeights();
        }
    },
    MEDIAN {
        @NotNull
        public CombinationStrategy create() {
            return new Median();
        }
    },
    XCSF {
        @NotNull
        public CombinationStrategy create() {
            return new XCSF();
        }
    };

    public abstract CombinationStrategy create();
}
