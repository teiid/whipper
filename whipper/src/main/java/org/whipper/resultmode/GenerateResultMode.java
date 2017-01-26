package org.whipper.resultmode;

import java.io.File;
import java.io.IOException;

import org.whipper.Query;
import org.whipper.WhipperProperties;
import org.whipper.xml.XmlHelper;

/**
 * This is a generate result mode. It will generate expected result of the query based on its actual result.
 *
 * @author Juraj Duráni
 */
public class GenerateResultMode implements ResultMode {

    private File rootOutputDir;

    @Override
    public void destroy() {}

    @Override
    public void resetConfiguration(WhipperProperties props) {
        rootOutputDir = props.getOutputDir();
        if(rootOutputDir == null){
            throw new IllegalArgumentException("Output directory is not defined.");
        }
    }

    @Override
    public String getName() {
        return "GENERATE";
    }

    @Override
    public File getErrorFile(Query q){
        return new File(rootOutputDir, q.getScenario().getId() + File.separator
                + "errors_for_" + getName() + File.separator + q.getSuite().getId() + "_" + q.getId() + "_error.xml");
    }

    @Override
    public ResultHolder handleResult(Query q) {
        File outputDir = new File(rootOutputDir, getName()
                + File.separator + q.getScenario().getQuerysetDirName()
                + File.separator + q.getScenario().getExpectedResultsDirName()
                + File.separator + q.getSuite().getId());
        ResultHolder out = new ResultHolder();
        if(outputDir.exists() && outputDir.isFile()){
            out.setException(new IOException("Cannot generate result: " + outputDir.getAbsolutePath() + " is file."));
        } else if(!outputDir.exists() && !outputDir.mkdirs()){
            out.setException(new IOException("Cannot generate result: cannot create directory " + outputDir.getAbsolutePath()));
        } else {
            File outputFile = new File(outputDir, q.getSuite().getId() + "_" + q.getId() + ".xml");
            try{
                outputFile.createNewFile();
                XmlHelper.writeResult(q, outputFile);
            } catch (IOException ex){
                out.setException(ex);
            }
        }
        return out;
    }
}
