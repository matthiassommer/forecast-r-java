# Forecasting time series with Java and R

This project is written in Java und uses forecast methods provided by the [forecast](https://cran.r-project.org/web/packages/forecast/forecast.pdf) package of R to forecast time series. R is a free software environment for statistical computing. [Rserve](https://www.rforge.net/Rserve/) is used as a TCP/IP server to run R libraries from within Java.

- Forecast methods: ARIMA, BATS, MEAN, Random Walk, Croston, DES, ETS, ES, and many others.
- Combination strategies: ANN, Median, Optimal Weights, Outperformance, Simple Average, XCSF
- Error measures: MAE, MAPE, MASE, RMSE, SMAPE, U-statistic
- Normalisation: Decimal, Exponential, MinMax, Softmax, Median
- Plotting: Decomposition, Histogram, Curve
- Metrics: ACF, Kurtosis, LjungBox, Non-Linearity, T-test, Seasonality, Skewnews, and many more.

## Getting Started

The ForecastModule is the class you want to connect to your project. It provides all methods you need to make forecasts with one or more forecast methods. Then you pass in the actual value for each time step and calculate the forecast error.

The forecast methods and the combination strategy is defined in the forecast.properties file.

´´´
ForecastModule module = new ForecastModule();
module.addValue(1.0, 123.12);
module.addValue(2.0, 13.1);
module.addValue(3.0, 22.0);
float forecast = module.combinedForecast(3.0, 1, 4.0);

float actualValue = 10.1;
module.addValueToEvaluators(4.0, actualValue);

float mase = module.combinedForecastError();
´´´

### Prerequisites

- [Java](https://www.java.com/de/)
- [R](https://www.r-project.org/)

## Authors

* **Matthias Sommer**  - [Website](https://www.matthiassommer.it)


## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

