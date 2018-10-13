plotForecastErrors <- function(forecasterrors, pdfpath)
{
  # make a histogram of the forecast errors:
  mybinsize <- IQR(forecasterrors)/4
  mysd   <- sd(forecasterrors)
  mymin  <- min(forecasterrors) - mysd*5
  mymax  <- max(forecasterrors) + mysd*3
  
  # generate normally distributed data with mean 0 and standard deviation mysd
  mynorm <- rnorm(10000, mean=0, sd=mysd)
  mymin2 <- min(mynorm)
  mymax2 <- max(mynorm)
  if (mymin2 < mymin) { mymin <- mymin2 }
  if (mymax2 > mymax) { mymax <- mymax2 }
  
  pdf(pdfpath)
  
  # make a red histogram of the forecast errors, with the normally distributed data overlaid:
  mybins <- seq(mymin, mymax, mybinsize)
  
  opar=par(cex.axis=1.6, cex.lab=1.6, mar = c(5,5,1,1)) 
  
  hist(forecasterrors, col="red", freq=FALSE, breaks=mybins, xlab="Forecast errors", main="")
  
  # freq=FALSE ensures the area under the histogram = 1
  # generate normally distributed data with mean 0 and standard deviation mysd
  myhist <- hist(mynorm, plot=FALSE, breaks=mybins)
  
  # plot the normal curve as a blue line on top of the histogram of forecast errors:
  myPoints <- points(myhist$mids, myhist$density, type="l", col="blue", lwd=2)

  dev.off()
}