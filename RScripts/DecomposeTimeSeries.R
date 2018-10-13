decomposeTimeseries <- function(timeseries, pdfpath)
{
  myts <- ts(timeseries)
  fit <- stl(myts, s.window="periodic", robust=TRUE)
  
  pdf(pdfpath)
  plot(fit)
  dev.off()
}
