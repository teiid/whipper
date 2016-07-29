package org.jboss.bqt.resultmode;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.jboss.bqt.BqtTool.Keys;
import org.jboss.bqt.Query;
import org.jboss.bqt.XmlHelper;

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
    public void init(Properties props) {
        String outDirStr = props.getProperty(Keys.OUTPUT_DIR);
        if(outDirStr == null){
            throw new IllegalArgumentException("Output directory is not set (property " + Keys.OUTPUT_DIR + ")");
        }
        rootOutputDir = new File(outDirStr);
    }

    @Override
    public String getName() {
        return "GENERATE";
    }

    @Override
    public ResultHandler handleResult(Query q) {
        File outputDir = new File(rootOutputDir, "GENERATE"
                + File.separator + q.getScenario().getQuerysetDirName()
                + File.separator + q.getScenario().getExpectedResultsDirName()
                + File.separator + q.getSuite().getId());
        ResultHandler out = new ResultHandler();
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
