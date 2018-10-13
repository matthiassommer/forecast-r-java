/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting;

import forecasting.combinationStrategies.Strategies;
import forecasting.forecastMethods.ForecastMethod;
import forecasting.forecastMethods.arima.ARIMA;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * This class defines the settings to run the forecast module.
 */
public abstract class DefaultForecastParameters {
    /**
     * Set to <code>true</code> to active the forecast module, otherwise false.
     */
    public static boolean IS_FORECAST_MODULE_ACTIVE = true;

    /**
     * Default list of forecast methods to use.
     */
    @NotNull
    public static List<ForecastMethod> DEFAULT_FORECAST_METHODS = Arrays.asList(
            ForecastMethod.TBATS,
            ForecastMethod.ETS,
            ForecastMethod.AUTOARIMA
    );
    // ARIMA parameters
    public static List<Integer> p = new ArrayList<>();
    public static List<Integer> d = new ArrayList<>();
    public static List<Integer> q = new ArrayList<>();

    @NotNull
    public static List<Integer> FORECAST_METHOD_DATA_POINTS = Arrays.asList(50, 50, 50);
    /**
     * How should the individual forecasts be combined? The chosen combination strategy.
     */
    @NotNull
    public static Strategies FORECAST_COMBINATION_STRATEGY = Strategies.XCSF;

    public static int getP(int i) {
        return p.get(i);
    }

    public static int getD(int i) {
        return d.get(i);
    }

    public static int getQ(int i) {
        return q.get(i);
    }

    public static void readPropertyFile(String folder) {
        Properties prop = new Properties();

        try {
            File file = new File(folder + "forecast.properties");
            FileInputStream fi = new FileInputStream(file);
            prop.load(fi);

            readForecastMethods(prop);
            readForecastMethodSizes(prop);
            readCombinationStrategy(prop);
            readIsActiveFlag(prop);
            readARIMAParameters(prop);

            fi.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void readIsActiveFlag(@NotNull Properties prop) {
        IS_FORECAST_MODULE_ACTIVE = Boolean.parseBoolean(prop.getProperty("isActive"));
    }

    private static void readARIMAParameters(@NotNull Properties prop) {
        p.clear();
        String[] pString = prop.getProperty("p").split(",");
        for (String s : pString) {
            p.add(Integer.valueOf(s));
        }

        d.clear();
        String[] dString = prop.getProperty("d").split(",");
        for (String s : dString) {
            d.add(Integer.valueOf(s));
        }

        q.clear();
        String[] qString = prop.getProperty("q").split(",");
        for (String s : qString) {
            q.add(Integer.valueOf(s));
        }
    }

    private static void readCombinationStrategy(@NotNull Properties prop) {
        String strategyName = prop.getProperty("combinationStrategy").trim().toUpperCase();
        FORECAST_COMBINATION_STRATEGY = Strategies.valueOf(strategyName);
    }

    private static void readForecastMethods(@NotNull Properties prop) {
        List<ForecastMethod> methods = new ArrayList<>();
        String[] forecastMethods = prop.getProperty("forecastMethods").split(",");

        for (String forecastMethod : forecastMethods) {
            methods.add(ForecastMethod.valueOf(forecastMethod.trim().toUpperCase()));
        }
        DEFAULT_FORECAST_METHODS = methods;
    }

    private static void readForecastMethodSizes(@NotNull Properties prop) {
        List<Integer> sizes = new ArrayList<>();
        String[] sizeString = prop.getProperty("dataPoints").split(",");
        for (String size : sizeString) {
            sizes.add(Integer.valueOf(size));
        }
        FORECAST_METHOD_DATA_POINTS = sizes;
    }
}
