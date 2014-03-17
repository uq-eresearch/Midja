Important R dependencies:

1. Install R library 'log4r, RJSONIO, Rserve' globally
sudo R
> install.packages("log4r")
> install.packages("RJSONIO")
> install.packages("Rserve")
> q() #quit

2. Add the listed packages above to the default R package loader
sudo vim /etc/R/Renviron.site
R_DEFAULT_PACKAGES='utils,grDevices,graphics,stats,RJSONIO,log4r'

3. Building, from the root (AnalysisTool) directory:
mvn clean compile -U

4. Running: 
pkill Rserve; Rserve && mvn test -Dtest=RegressionTest
