package org.whipper.resultmode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import org.whipper.ExpectedResultHolder;
import org.whipper.Query;
import org.whipper.Whipper;
import org.whipper.WhipperProperties;
import org.whipper.xml.XmlHelper;

/**
 * This is a compare result mode. It will compare actual result of the query with expected result.
 *
 * @author Juraj Duráni
 */
public class CompareResultMode implements ResultMode {

    private File outputDirectory;
    private BigDecimal allowedDivergence;
    private final ExpectedResultHolder holder = new ExpectedResultHolder();

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

    @Override
    public File getErrorFile(Query q){
        return new File(outputDirectory, getFileName(q) + "_error.xml");
    }

    @Override
    public ResultHolder handleResult(Query q){
        File result = new File(q.getScenario().getExpectedResultsDir(),
                q.getSuite().getId() + File.separator + q.getSuite().getId() + "_" + q.getId() + ".xml");
        ResultHolder out = new ResultHolder();
        try{
            holder.buildResult(result, q);
            boolean eq = holder.equals(q.getActualResult(), !q.getSql().toUpperCase().contains(" ORDER BY "), allowedDivergence);
            if(!eq){
                writeErrorFile(q);
                out.setErrors(holder.getErrors());
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
            XmlHelper.writeError(q, holder, errorFileXml);
        } finally {
            Whipper.close(fwTxt);
        }
    }
}
