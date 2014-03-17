

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load the R analysis script file as a String
 * @author irfan
 *
 */
public class Rscript {

  private static final Logger LOG = LoggerFactory.getLogger(Rscript.class);

  private Rscript() {
  }

  /**
   * Loads the given R script and returns a String representation
   * 
   * @param scriptName
   *          path and the name of R script. For example:
   *          "/scripts/scriptName.r"
   * @return {@link String} representation of the loaded R script
   * @throws StatisticsException
   *           If unable to load or parse the given scriptName
   */
  public static String load(String scriptName) throws StatisticsException {

    String script = null;

    try {
      InputStream is = Rscript.class.getResourceAsStream(scriptName);
      script = IOUtils.toString(is).replaceAll(
          System.getProperty("line.separator"), IOUtils.LINE_SEPARATOR_UNIX);
      is.close();
      LOG.info("R Script Size: " + script.length() + " Bytes for " + scriptName);
    } catch (IOException ioe) {
      throw new StatisticsException("Unable to load R Script: " + scriptName, ioe);
    } catch (PatternSyntaxException pse) {
      throw new StatisticsException("Unable to Parse loaded R Script", pse);
    }

    return script;
  }

}
