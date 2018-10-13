plotActualForecast <- function(values, forecastsSeries, pdfpath)
{
  combined <- as.data.frame(cbind(values, forecastsSeries))
  
  axis <- seq.int(from = 1,
                  to = length(forecastsSeries),
                  by = 1)
  
  myPlot <- ggplot(combined) +
    geom_line(aes(x = axis, y = combined$values, colour="Actual"), lty=2) +
    geom_line(aes(x = axis, y = combined$forecastsSeries, colour="Forecast"), lty=4) +
    labs(x = "Time steps", y = "Values") +
    theme(legend.position=c(0.8,0.9), 
          legend.background = element_rect(fill="transparent"),
          legend.title=element_blank(),
          text = element_text(size=22)
    )
  
  ggsave(filename = pdfpath, plot = myPlot)
}