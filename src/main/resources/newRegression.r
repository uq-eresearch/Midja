logListLevels <- list(OFF=1, ERROR=2, WARN=3, INFO=4, DEBUG=5, TRACE=6)

libraryError <- function() {
  if( !(require("log4r")) ) {
    stop("Library Error: ", call.=print(traceback()))
  }  
}

# No logging scenario
setupNullLogger <- function() {
  
  lfile <- ""
  llvl <- 1
  rLog <- create.logger(logfile=lfile);
  level(rLog) <- verbosity(llvl) # log level 5 - FATAL or 6 - TRACE only
  
  #  info(rLog, c("str(rlogger) = ", capture.output(str(rlogger))))
  print(paste("Logging LEVEL:", llvl, "Logging DIRECTORY:", lfile))
  
  return(rLog)
}

# Logging to given directory and a log level
setupFileLogger <- function(optionsLogging) {
  
  lfile <- as.character(paste(optionsLogging$LOG_DIRECTORY, 
                              .Platform$file.sep, Sys.Date(),
                              ".rlog", sep="", collpse=""))
  tupperLvl <- as.character(toupper(optionsLogging$LOG_LEVEL))
  numLogLevel <- as.integer(logListLevels[[tupperLvl]])
  llvl <- verbosity(numLogLevel)
  print("lfile = "); print(lfile)
  print("tupperLvl = "); print(tupperLvl)
  print("numLogLevel = "); print(numLogLevel)
  print("llvl = "); print(llvl)
  
  rLog <- log4r::create.logger(logfile=lfile, level=llvl)
  #  print(paste("str(rLog) = ", capture.output(str(rLog))))
  #  info(rLog, paste("info:: str(rLog) = ", capture.output(str(rLog))))
  #  debug(rLog, paste("debug:: str(rLog) = ", capture.output(str(rLog))))
  
  return(rLog)
}

# Main logger setup function
# Supports optionsLogging Dataframe with: String LOG_LEVEL and LOG_DIRECTORY
setupLogging <- function() {
  
  libraryError()
  
  # check validity of optionsLogging
  if(exists("optionsLogging") == TRUE) {
    print("optionsLogging provided")
    # validitity check
    if( (typeof(as.character(optionsLogging$LOG_LEVEL)) == typeof("string")) && 
          (typeof(as.character(optionsLogging$LOG_DIRECTORY)) == typeof("string"))) {
      print("VALID 'optionsLogging' provided")
      rLog <- setupFileLogger(optionsLogging)
    } else {
      # No logger Scenario
      print("NO Logging. Logging to Console");
      rLog <- setupNullLogger()
    }
  } else {
    ## No logger scenario
    print("NO Logging. Logging to Console");
    rLog <- setupNullLogger()
  }
  return(rLog)
}


# Linear Model for linear single and multiple-regression

############ SUPPORT METHODS ############
## Library loading methods
libraryError1 <- function(rLog) {
  
  info(rLog, "============= New Regression libraries ============")

  if(!(require("stats") && require("RJSONIO"))) {
    stop("Library Error: ", call.=error(rLog, traceback()))
  }
  ## call the library version checker
  libraryVersionError(rLog)
}

## library version check
libraryVersionError <- function(rLog) {
  if(compareVersion(toString(packageVersion("RJSONIO")), "1.0") == -1) {
    error(rLog, paste('Error library version: ', call.=print(traceback())))
    stop("Error Library version: ", call.=error(rLog, traceback()))
  }
}

## Is enough input provided to compute regression?
inputError <- function(rLog, dataFrameName, indepVars, depVars, optionsLogging) {

  if(!(exists("dataFrameName") && exists("indepVars") && exists("depVars"))) {
    warning(paste("ERROR: Invalid inputs for: dataFrameName, indepVars, depVars = ",
                  dataFrameName, indepVars, depVars))
    error(rLog, "Invalid inputs: ", paste(dataFrameName, indepVars, depVars))
    stop("ERROR: Invalid inputs: ", call.=print(paste(dataFrameName, indepVars, depVars)))
  }

  if(!(is.character(dataFrameName) && length(dataFrameName) >= 1)) {
    error(rLog, paste("Invalid input, length: dataFrameName = ", dataFrameName))
    stop(paste("ERROR: Invalid input, length: dataFrameName = ", dataFrameName), call.=error(rLog, traceback()))
  }
  dataF <- get(dataFrameName)
  info(rLog, paste("dataF = ", dataF))
  ## check input dataframe
  if(!(is.data.frame(dataF) && length(dataF) >= 1)) {
    error(rLog, paste("Invalid input, length: dataF = ", dataF))
    stop(paste("ERROR: Invalid input, length: dataF = ", dataF), call.=error(rLog, traceback()))
  }
  
  ## Input column names from the dataF (dataframe) to Classify
  if(!(is.character(indepVars) && length(indepVars) >= 1 )) {
    error(rLog, paste("ERROR: Invalid input, string array of Independent column names: indepVars =", indepVars))
    stop(paste("ERROR: Invalid input, length: string array of Independent column names: indepVars =", indepVars), 
         call.=error(rLog, traceback()))
  }
  if(!(is.character(depVars) && length(depVars) >= 1 )) {
    error(rLog, paste("ERROR: Invalid input, string array of Dependent column names: depVars =", depVars))
    stop(paste("ERROR: Invalid input, length: string array of Dependent column names: depVars =", depVars), 
         call.=error(rLog, traceback()))
  }

  if(!(is.data.frame(optionsLogging) && length(optionsLogging) >= 1)) {
    warning("ERROR: Invalid input, length: optionsLogging = ", optionsLogging)
  }

  debug(rLog, paste("str(dataF) = ", capture.output(str(dataF))))
  debug(rLog, paste("dataF = ", capture.output(dataF)))
  debug(rLog, paste("depVars = ", depVars))
  debug(rLog, paste("indepVars = ", indepVars))
  debug(rLog, paste("paste(indepVars, collapse='+') = ", paste(indepVars, collapse="+"))) 

}

## Custom summary method for the lm statistic
summary.lm.custom <- function(object, correlation = TRUE, symbolic.cor = FALSE) {
    z <- object
        p <- z$rank
        rdf <- z$df.residual
        if (p == 0) {
            r <- z$residuals
                n <- length(r)
                w <- z$weights
                if (is.null(w)) {
                    rss <- sum(r^2)
                }
                else {
                    rss <- sum(w * r^2)
                        r <- sqrt(w) * r
                }
            resvar <- rss/rdf
                ans <- z[c("call", "terms", "model", if (!is.null(z$weights)) "weights")]
                class(ans) <- "summary.custom.lm"
                ans$aliased <- is.na(coef(object))
                ans$residuals <- r
                ans$df <- c(0L, n, length(ans$aliased))
                ans$coefficients <- matrix(NA, 0L, 4L)
                dimnames(ans$coefficients) <- list(NULL, c("Estimate", 
                            "Std. Error", "t value", "Pr(>|t|)"))
                ans$sigma <- sqrt(resvar)
                ans$r.squared <- ans$adj.r.squared <- 0
                return(ans)
        }
    if (is.null(z$terms)) 
        stop("invalid 'lm' object:  no 'terms' component")
            if (!inherits(object, "lm")) 
                warning("calling summary.lm(<fake-lm-object>) ...")
                    Qr <- stats:::qr.lm(object)
                    n <- NROW(Qr$qr)
                    if (is.na(z$df.residual) || n - p != z$df.residual) 
                        warning("residual degrees of freedom in object suggest this is not an \"lm\" fit")
                            p1 <- 1L:p
                            r <- z$residuals
                            f <- z$fitted.values
                            w <- z$weights
                            if (is.null(w)) {
                                mss <- if (attr(z$terms, "intercept")) 
                                    sum((f - mean(f))^2)
                                    else sum(f^2)
                                        rss <- sum(r^2)
                            }
                            else {
                                mss <- if (attr(z$terms, "intercept")) {
                                    m <- sum(w * f/sum(w))
                                        sum(w * (f - m)^2)
                                }
                                else sum(w * f^2)
                                    rss <- sum(w * r^2)
                                        r <- sqrt(w) * r
                            }
    resvar <- rss/rdf
        R <- chol2inv(Qr$qr[p1, p1, drop = FALSE])
        se <- sqrt(diag(R) * resvar)
        est <- z$coefficients[Qr$pivot[p1]]
        tval <- est/se
        ans <- z[c("call", "terms", "model", if (!is.null(z$weights)) "weights")]
        ans$residuals <- r
        ans$coefficients <- cbind(est, se, tval, 2 * pt(abs(tval), 
                    rdf, lower.tail = FALSE))
        dimnames(ans$coefficients) <- list(names(z$coefficients)[Qr$pivot[p1]], 
                c("Estimate", "Std. Error", "t value", "Pr(>|t|)"))
        ans$aliased <- is.na(coef(object))
        ans$sigma <- sqrt(resvar)
        ans$df <- c(p, rdf, NCOL(Qr$qr))
        if (p != attr(z$terms, "intercept")) {
            df.int <- if (attr(z$terms, "intercept")) 
                1L
                else 0L
                    ans$r.squared <- mss/(mss + rss)
                        ans$adj.r.squared <- 1 - (1 - ans$r.squared) * ((n - df.int)/rdf)
                        ans$fstatistic <- c(value = (mss/(p - df.int))/resvar,  numdf = p - df.int, dendf = rdf)
        }
        else ans$r.squared <- ans$adj.r.squared <- 0
            ans$cov.unscaled <- R
                dimnames(ans$cov.unscaled) <- dimnames(ans$coefficients)[c(1, 1)]
                if (correlation) {
                    ans$correlation <- (R * resvar)/outer(se, se)
                        dimnames(ans$correlation) <- dimnames(ans$cov.unscaled)
                        ans$symbolic.cor <- symbolic.cor
                }
    if (!is.null(z$na.action)) 
        ans$na.action <- z$na.action
            class(ans) <- "summary.custom.lm"
            ans
}

## Main method for Regression
# options Logging needs to be setup before calling this method
# optionsLogging <<- data.frame(LOG_LEVEL="DEBUG", LOG_DIRECTORY="/tmp")
newRegression <- function() {

  oData <- NULL
  
  rLog <- setupLogging()
  
  libraryError1(rLog)
  inputError(rLog, dataFrameName, indepVars, depVars, optionsLogging)
  dataF <- get(dataFrameName)
  # Remove na
  dataF <- na.omit(dataF)
  
#   print("----- OLD DATAF -----"); print(head(dataF))
#   dataF <- as.data.frame(scale(x=dataF, center=T, scale=T))
#   print("----- NEW DATAF -----"); print(head(dataF))
  
  inVars <- paste(indepVars, collapse="+")
  info(rLog, paste("inVars = ", inVars))
  info(rLog, paste("depVars = ", depVars))
  info(rLog, paste("length(depVars) = ", length(depVars)))
  
  # Create Storage for results, +1 for JSON, +1 for prettyPrint
  oData <- vector("list", length(depVars)+1)
  for(d in 1:length(depVars))
  {
    fmla <- as.formula(paste(depVars[d], "~", inVars, sep=" "))
    info(rLog, paste("fmla = ", fmla))
    print("fmla = "); print(fmla)
    o <- lm(fmla, data=dataF)
    info(rLog, capture.output(summary(o)))
    oData[[d]] <- summary.lm.custom(o) # results in REXP
    
    print("oData = "); print(oData[[d]])
    
    debug(rLog, paste("Individual json = ", paste(gsub(pattern="\\n", replacement="", x=noquote(RJSONIO::toJSON(oData[[d]]))))))
  }
  
  oData[[d+1]] <- list(JSON=gsub(pattern="\n", replacement="", x=noquote(RJSONIO::toJSON(oData[1:length(depVars)])))) #JSON results
  info(rLog, paste("oData[[d+1]] = ", capture.output(oData[[d+1]])))
  debug(rLog, paste("str(oData) = ", capture.output(str(oData[[d+1]]))))
  info(rLog, paste("length(oData) = ", length(oData)))
  
  return(oData)
}

## RUNIT test case
## Copy paste the code within the method in R console before running newRegression()
## otherwise it will result in failure for testing
TestNewRegression <- function() {

    indepVars <<- c("iCol1", "iCol2", "iCol3")
    depVars <<- "dCol1"

    dataF <<- data.frame(
                dCol1=c(10.0, 20.0, 30.0, 40.0, 50.0, 60.0), 
                iCol1=c(1.1, 2.2, 3.3, 11.1, 22.2, 33.3),
                iCol2=c(4.5, 5.6, 6.7, 7.8, NA, 9.10),
                iCol3=c(10.11, 11.12, 12.13, 13.14, 14.15, NA)
              )
    dataFrameName <<- "dataF"
    dataF <<- get(dataFrameName)
    
    optionsLogging <<- data.frame(LOG_LEVEL="DEBUG", LOG_DIRECTORY="/tmp")

    ## Call the main method
    newRegression()
}

## main method call
newRegression()
