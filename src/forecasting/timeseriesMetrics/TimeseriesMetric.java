/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.timeseriesMetrics;

import forecasting.RServeConnection;
import org.rosuda.REngine.Rserve.RConnection;

/**
 * Abstract class for time series metrics.
 */
public abstract class TimeseriesMetric {
    protected RConnection rConnection = RServeConnection.getConnection();

    /**
     * Execute the time series metric.
     *
     * @param input time series
     * @return measure value
     */
    public abstract double run(double[] input);
}
