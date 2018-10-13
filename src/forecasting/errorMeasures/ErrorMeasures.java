/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.errorMeasures;

import org.jetbrains.annotations.NotNull;

/**
 * Enum for all error measures. Factory pattern for their creation.
 *
 * @author Matthias Sommer.
 */
public enum ErrorMeasures {
    RMSE {
        @NotNull
        public AbstractErrorMeasure create() {
            return new RMSE();
        }
    },
    SMAPE {
        @NotNull
        public AbstractErrorMeasure create() {
            return new SMAPE();
        }
    },
    MAE {
        @NotNull
        public AbstractErrorMeasure create() {
            return new MAE();
        }
    },
    MASE {
        @NotNull
        public AbstractErrorMeasure create() {
            return new MASE();
        }
    },
    MAPE {
        @NotNull
        public AbstractErrorMeasure create() {
            return new MAPE();
        }
    };

    public abstract AbstractErrorMeasure create();
}
