package org.whipper.resultmode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.ExpectedResultHolder;
import org.whipper.Query;
import org.whipper.Whipper;
import org.whipper.WhipperProperties;
import org.whipper.utils.OverrideFileSelector;
import org.whipper.xml.XmlHelper;
import org.whipper.xml.result.ObjectFactory;
import org.whipper.xml.result.QueryResultType;

/**
 * This is a compare result mode. It will compare actual result of the query with expected result.
 *
 * @author Juraj Duráni
 */
public class CompareResultMode implements ResultMode {

    private File outputDirectory;
    private BigDecimal allowedDivergence;
    private final ExpectedResultHolder holder = new ExpectedResultHolder();

    private static final Logger LOG = LoggerFactory.getLogger(CompareResultMode.class);

    @Override
    public void resetConfiguration(WhipperProperties props) {
        outputDirectory = props.getOutputDir();
        if(outputDirectory == null){
            throw new IllegalArgumentException("Output directory is not defined.");
        }
        BigDecimal ad = props.getAllowedDivergence();
        allowedDivergence = ad == null ? BigDecimal.ZERO : ad;
    }

    @Override
    public void destroy() {}

    @Override
    public String getName() {
        return "COMPARE";
    }

    /**
     * Returns basic name for error file.
     *
     * @param q query
     * @return error file name
     */
    private String getFileName(Query q){
        return q.getScenario().getId() + File.separator + "errors_for_" + getName() + File.separator + q.getSuite().getId() + "_" + q.getId();
    }

    private String getExpectedResultFileName(Query q){
        return q.getSuite().getId() + File.separator + q.getSuite().getId() + "_" + q.getId() + ".xml";
    }

    @Override
    public File getErrorFile(Query q){
        return new File(outputDirectory, getFileName(q) + "_error.xml");
    }

    @Override
    public ResultHolder handleResult(Query q){
        ResultHolder out = new ResultHolder();
        OverrideFileSelector selector = new OverrideFileSelector(q.getScenario().getExpectedResultsDir());
        File result = selector.getExpectedResultFile(getExpectedResultFileName(q));
        if (result != null && result.exists()) {
            String expectedResultDirectoryName = result.getParentFile().getParentFile().getName();
            try{
                holder.buildResult(result, q);
                boolean eq = holder.equals(q.getActualResult(), !q.getSql().toUpperCase().contains(" ORDER BY "), allowedDivergence);
                if(!eq){
                    writeErrorFile(q, expectedResultDirectoryName);
                    out.setErrors(holder.getErrors());
                }
            } catch (IOException ex){
                LOG.error("Error handling result", ex);
                out.setException(ex);
            }
        } else {
            // this means that no file with expected result was found
            try {
                holder.clear();
                holder.setOriginalResult(null);
                holder.getErrors().add("Unable to find expected result file for query");
                out.setErrors(holder.getErrors());
                writeErrorFile(q, "unknown");
            } catch (IOException ex) {
                LOG.error("Error handling result", ex);
                out.setException(ex);
            }
        }
        return out;
    }

    /**
     * Writes compare error to the file.
     *
     * @param q query
     * @throws IOException if some error occurs
     */
    private void writeErrorFile(Query q, String expectedResultDirectoryName) throws IOException{
        File errorFileXml = getErrorFile(q);
        File errorFileTxt = new File(outputDirectory, getFileName(q) + "_failures.txt");
        errorFileXml.getParentFile().mkdirs();
        errorFileXml.createNewFile();
        errorFileTxt.createNewFile();
        FileWriter fwTxt = null;
        try{
            fwTxt = new FileWriter(errorFileTxt, false);
            for(String err : holder.getErrors()){
                fwTxt.write(err);
                fwTxt.write(System.lineSeparator());
            }
            XmlHelper.writeError(q, holder, errorFileXml, expectedResultDirectoryName);
        } finally {
            Whipper.close(fwTxt);
        }
    }
}
