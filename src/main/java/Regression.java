


import java.text.DecimalFormat;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * {@link Regression} Linear Model (Regression) of input data
 * 
 * @author irfan
 * 
 */

public class Regression {

  public RConnection cIn;
  /**
   * {@link RConnection} A valid connection to a running {@link Rserve} instance
   */

  public String dataFrameName;
  /**
   * {@link String} Name of the data frame for {@link Regression}
   */

  public String[] dependentVarNames;
  /**
   * {@link String} array for the response terms for regression
   */

  public String[] independentVarNames;
  /**
   * {@link String} array for the dependent terms for regression
   */

  private REXP worker;
  /**
   * The result of {@link Regression} as an {@link REXP} object containing
   * all of the results from R
   */

  public String jsonResult;
  /**
   * Regression result as a JSON object for consumption by Visualisation
   * services
   */

  public String textResult;

  /** The regression result terms of interest */
  private static final String[] REG_TERMS = { "coefficients", "sigma", "r.squared", "adj.r.squared", "fstatistic",
    "correlation", "residuals", "model" };

  /** Terms within the computed F-statistic in a regression model 
   * F Statistic
   * Degree of Freedom for Regression
   * Degree of Freedom Error
   * Sum of Square Error
   * Sum of Square Residual
   */
  private static enum FSTATISTIC {
    F, DFR, DFE, SSE, SSR
  }

  private static final Logger LOG = LoggerFactory.getLogger(Regression.class);
  
  /**
   * Initialise the default values for the {@link Regression} computation
   */
  public Regression() {

    this.cIn = null;
    this.jsonResult = null;
    this.worker = null;
  }

  /**
   * <p>
   * Computes the {@link Regression} statistical analysis. The resulting data
   * is a {@link REXP} containing an {@link RList} object.
   * </p>
   * 
   * <p>
   * The length of the resulting {@link RList} is 2. The first {@link RList}
   * within the result contains the regression result and the size of this
   * {@link RList} depends on the length of the dependentVarNames. The second
   * {@link RList} in the result contains the result in JSON format from R.
   * </p>
  */
  public void compute() throws StatisticsException {

    LOG.info("Compute");

      // setup the script to execute
      // 1. load the required script
      try {
        this.cIn.assign("script", Rscript.load("newRegression.r"));
      this.cIn.assign("dataFrameName", new REXPString(this.dataFrameName));

      // 2. setup the inputs
      this.cIn.assign("depVars", this.dependentVarNames);
      this.cIn.assign("indepVars", this.independentVarNames);

      // 2.5 Setup R logging
      RList op = new RList();
      op.put("LOG_LEVEL", new REXPString("INFO"));
      op.put("LOG_DIRECTORY", new REXPString("/tmp"));
      this.cIn.assign("optionsLogging", REXP.createDataFrame(op));

      // 3. call the function defined in the script
      this.worker = this.cIn.eval("try(eval(parse(text=script)),silent=FALSE)");
      LOG.info("worker result: {}", this.worker.toDebugString());

      // 4. setup the output results
      this.setupOutputs();

      // 6. Setup textual result automatically
      this.textResult = this.prettyPrint();

      } catch (REngineException e) {
        throw new StatisticsException("REngine", e);
      } catch (StatisticsException e) {
        throw new StatisticsException(e);
      } catch (REXPMismatchException e) {
        e.printStackTrace();
      }

  }

  public REXP getWorker() {
    return worker;
  }

  public void setWorker(REXP worker) {
    this.worker = worker;
  }
  
  /**
   * Human readable result with inputs and the summary statistic of the results
   * @throws StatisticsException 
   */
  public String prettyPrint() throws StatisticsException {

    StringBuilder s = new StringBuilder();

    // use the REXP worker result
    try {
      if (!this.worker.isNull()) {
        LOG.info("We have content back from R");

        if (this.worker.inherits("try-error") || this.worker.isString()) {
          throw new StatisticsException(new REXPMismatchException(this.worker,
              "Try-Error from R \n" + this.worker.toString()));
          } else if (this.worker.isList()) {
          RList resultL = this.worker.asList();
          RList r1 = null;

          for (int ridx = 0; ridx < resultL.size(); ridx++) {
            // last result in list is JSON
            LOG.debug("ridx = {}", ridx);
            r1 = resultL.at(ridx).asList();

            String[] keys = r1.keys();
            for (int kidx = 0; kidx < keys.length; kidx++) {
              LOG.debug("keys[" + kidx + "] {}", keys[kidx]);
              s.append(parseResults(keys[kidx], r1));
            }
          }
        }
      } else {
        // worker is null, we did not get any results back from R
        throw new StatisticsException(new REXPMismatchException(this.worker,
            "No Result returned from R \n" + this.worker.toDebugString()));
      }
    } catch (REXPMismatchException e) {
      throw new StatisticsException("Error Parsing all Results", e);
    }

    LOG.info(s.toString());
    return s.toString();
  }

  private void setupOutputs() throws StatisticsException {

    RList resultL = null;

    try {
      if (!this.worker.isNull()) {
        LOG.info("We have results back from R");

        if (this.worker.inherits("try-error") || this.worker.isString()) {
          throw new REXPMismatchException(this.worker, "Try-Error from R \n" + this.worker.toDebugString());
        } else if (this.worker.isList()) {
          // result list reply
          resultL = this.worker.asList();
          LOG.debug("resultL.size() = {}", resultL.size());

          this.jsonResult = resultL.at(resultL.size() - 1).asList().at("JSON").asString();
          LOG.info("JSON R-RESULT = {}", this.jsonResult);
        }
      }
    } catch (REXPMismatchException me) {
      throw new StatisticsException("REXP Mismatch", me);
    }
    LOG.info("Done setupOutputs()");
  }

  private String parseResults(String key, RList r1) throws REXPMismatchException {

    StringBuilder s = new StringBuilder();
    String lineSep = System.getProperty("line.separator");

    DecimalFormat df = new DecimalFormat("####.####");

    if (key.compareTo(REG_TERMS[0]) == 0) {
      // regression coefficients

      RList rAl = r1.at(key)._attr().asList();
      String[] colNames = rAl.at(1).asList().at(1).asStrings();
      String[] rowNames = rAl.at(1).asList().at(0).asStrings();

      double[][] coeffs = r1.at(key).asDoubleMatrix();

      s.append(lineSep + "Regression Coefficients " + lineSep);
      s.append("\t\t\t" + colNames[0] + "\t" + colNames[1] + "\t" + colNames[2] + "\t\t" + colNames[3] + lineSep);

      for (int i = 0; i < coeffs.length; i++) {
        s.append(rowNames[i] + "\t\t");
        for (int j = 0; j < coeffs[i].length; j++) {
          s.append(df.format(coeffs[i][j]));
          if (j == 0) {
            s.append("\t\t");
          } else {
            s.append("\t");
          }
        }
        s.append(lineSep);
      }
      s.append(lineSep);
    } else if (key.compareTo(REG_TERMS[1]) == 0) {
      // sigma
      s.append(REG_TERMS[1] + " = " + df.format(r1.at(key).asDouble()) + lineSep);
    } else if (key.compareTo(REG_TERMS[2]) == 0) {
      // r.squared
      s.append(REG_TERMS[2] + " = " + df.format(r1.at(key).asDouble()) + lineSep);

    } else if (key.compareTo(REG_TERMS[3]) == 0) {
      // adj.r.squared
      s.append(REG_TERMS[3] + " = " + df.format(r1.at(key).asDouble()) + lineSep);

    } else if (key.compareTo(REG_TERMS[4]) == 0) {
      // fstatistic
      double[] fvals = r1.at(key).asDoubles();
      for (int i = 0; i < fvals.length; i++) {
        s.append(REG_TERMS[4] + " : " + FSTATISTIC.values()[i].name() + " = " + df.format(fvals[i]) + lineSep);
      }

    } else if (key.compareTo(REG_TERMS[5]) == 0) {
      // correlation coefficients

      RList rAl = r1.at(key)._attr().asList();
      String[] rowNames = rAl.at(1).asList().at(0).asStrings();

      double[][] corCoeff = r1.at(key).asDoubleMatrix();

      s.append(lineSep + REG_TERMS[5] + " Coefficients " + lineSep);
      for (int i = 0; i < rowNames.length; i++) {
        if (i == 0) {
          s.append("\t\t");
        }
        s.append(rowNames[i] + "\t");
      }
      s.append(lineSep);

      for (int i = 0; i < corCoeff.length; i++) {
        s.append(rowNames[i] + "\t\t");
        for (int j = 0; j < corCoeff[i].length; j++) {
          s.append(df.format(corCoeff[i][j]));
          s.append("\t\t");
        }
        s.append(lineSep);
      }
    } else if (key.compareTo(REG_TERMS[6]) == 0) {
      // residuals
    } else if (key.compareTo(REG_TERMS[7]) == 0) {
      // model input data
    }
    LOG.debug("parseResults String: {}", s.toString());
    return s.toString();
  }

}
