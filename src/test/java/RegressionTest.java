


import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONUtils;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RegressionTest {

  public static int numDependents = 12;

  @BeforeClass
  public static void initRserve() {
    boolean rRunning = false;
    // 0. Start Rserve - This should already be running, if not we start it
    rRunning = Rserve.checkLocalRserve();
    System.out.println("Rserve running? " + rRunning);
    if (!rRunning) {
      Assert.fail("Without Rserve running we cannot proceed");
    }

    System.out.println("Starting R Connection");
    RConnection c;
    try {
      c = new RConnection();
      System.out.println("connection closed: " + c.close());
    } catch (RserveException e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  public static void terminateRserve() {
    boolean rRunning = true;
    // Stop Rserve if we started it
    rRunning = Rserve.shutdownRserve();
    System.out.println("Rserve shutdown? " + rRunning);
    if (!rRunning) {
      Assert.fail("Cannot Shutdown Rserve, Check if there are permissions "
          + "to shut it down if the process is owned by a different user");
    }
  }

  public double[] multiplier(double mult) {

    double[] content = new double[] { 11, 22, 33, 44, 55, 66 };

    for (int i = 0; i < content.length; i++) {
      content[i] = mult * content[i];
    }
    return content;
  }

  public REXP dataGenerator() {

    REXP data = null;

    double[] d1 = new double[] { 1.1, 2.2, 3.3, 11.1, 22.2, 33.3 }; // icol1
    double[] d2 = new double[] { 10.0, 20.0, 30.0, 40.0, 50.0, 60.0 }; // icol2
    double[] d3 = new double[] { 100.0, 200.0, 300.0, 400.0, 500.0, 600.0 }; // dcol1
    double[] d4 = new double[] { 100.1, 200.2, 300.3, 110.1, 220.2,
        REXPDouble.NA }; // dcol2

    RList a = new RList();
    // add each column separately
    a.put("iCol1", new REXPDouble(d1));
    a.put("iCol2", new REXPDouble(d2));

    a.put("dCol1", new REXPDouble(d3));
    a.put("dCol2", new REXPDouble(d4));

    // new test data dependents
    for (int i = 3; i <= numDependents; i++) {
      a.put(("dCol" + i).toString(),
          new REXPDouble(multiplier(i + Math.random() * 10)));
    }

    try {
      data = REXP.createDataFrame(a);
    } catch (REXPMismatchException e) {
      e.printStackTrace();
    }

    return data;
  }

  @Test
  public void test() {
    System.out.println("Test case");

    try {

      // 0. create a connection
      RConnection c = new RConnection();

      // 1. dummy R data for REXP population
      c.assign("dataF", this.dataGenerator());

      // 2. call newRegression
      Regression nr = new Regression();

      nr.cIn = c;
      nr.dataFrameName = "dataF";

      nr.independentVarNames = new String[] { "iCol1" };
      nr.dependentVarNames = new String[numDependents];
      for (int i = 0; i < numDependents; i++) {
        nr.dependentVarNames[i] = new String(("dCol" + (i + 1)).toString());
      }

      nr.compute();
      Assert.assertFalse(nr.getWorker().isString());

      System.out.println("PRETTY JSON:");
      JSON objOutputs = JSONSerializer.toJSON(nr.jsonResult);
      System.out.println(JSONUtils.valueToString(objOutputs, 1, 1));

      Assert.assertNotNull("Text Regression Result Fail", nr.textResult);
      System.out.println("TEXT RESUT = " + nr.textResult);

    } catch (RserveException e) {
      Assert.fail("Rserve: " + e.getMessage());
    } catch (StatisticsException e) {
      Assert.fail("Statistics: " + e.getMessage());
    } catch (Exception e) {
      Assert.fail("Exception: " + e.getMessage());
    }

  }

}
