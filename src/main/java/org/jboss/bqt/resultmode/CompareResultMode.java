package org.jboss.bqt.resultmode;

import static org.jboss.bqt.BqtTool.LS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import org.jboss.bqt.BqtTool;
import org.jboss.bqt.ExpectedResultHandler;
import org.jboss.bqt.Query;
import org.jboss.bqt.XmlHelper;
import org.jboss.bqt.BqtTool.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a compare result mode. It will compare actual result of the query with expected result.
 *
 * @author Juraj Duráni
 */
public class CompareResultMode implements ResultMode {

    private static final Logger LOG = LoggerFactory.getLogger(CompareResultMode.class);

    private File outputDirectory;
    private BigDecimal allowedDivergence;
    private final ExpectedResultHandler handler = new ExpectedResultHandler();

    @Override
    public void init(Properties props) {
        String outDirStr = props.getProperty(Keys.OUTPUT_DIR);
        if(outDirStr == null){
            throw new IllegalArgumentException("Output directory is not set (property " + Keys.OUTPUT_DIR + ")");
        }
        outputDirectory = new File(outDirStr);
        String adStr = props.getProperty(Keys.ALLOWED_DIVERGENCE);
        BigDecimal ad = BigDecimal.ZERO;
        if(adStr != null && !adStr.isEmpty()){
            try{
                ad = new BigDecimal(adStr);
            } catch (NumberFormatException ex){
                LOG.warn("Unable to parse allowed divergence. Setting to zero.", ex);
            }
        }
        allowedDivergence = ad;
    }

    @Override
    public void destroy() {}

    @Override
    public String getName() {
        return "COMPARE";
    }

    @Override
    public ResultHandler handleResult(Query q){
        File result = new File(q.getScenario().getExpectedResultsDir(),
                q.getSuite().getId() + File.separator + q.getSuite().getId() + "_" + q.getId() + ".xml");
        ResultHandler out = new ResultHandler();
        try{
            handler.buildResult(result);
            boolean eq = handler.equals(q.getActualResult(), !q.getSql().toUpperCase().contains(" ORDER BY "), allowedDivergence);
            if(!eq){
                writeErrorFile(q);
                out.setErrors(handler.getErrors());
            }
        } catch (IOException ex){
            out.setException(ex);
        }
        return out;
    }

    /**
     * Writes compare error to the file.
     *
     * @param q query
     * @throws IOException if some error occurs
     */
    private void writeErrorFile(Query q) throws IOException{
        String fileName = q.getScenario().getId() + File.separator + "errors_for_COMPARE" + File.separator + q.getSuite().getId() + "_" + q.getId();
        File errorFileXml = new File(outputDirectory, fileName + ".err");
        File errorFileTxt = new File(outputDirectory, fileName + "_messages.txt");
        errorFileXml.getParentFile().mkdirs();
        errorFileXml.createNewFile();
        errorFileTxt.createNewFile();
        FileWriter fwTxt = null;
        try{
            fwTxt = new FileWriter(errorFileTxt, false);
            for(String err : handler.getErrors()){
                fwTxt.write(err);
                fwTxt.write(LS);
            }
            XmlHelper.writeError(q, handler, errorFileXml);
        } finally {
            BqtTool.close(fwTxt);
        }
    }
}
