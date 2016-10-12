package org.whipper.resultmode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.ExpectedResultHolder;
import org.whipper.Query;
import org.whipper.QuerySet;
import org.whipper.Whipper.Keys;
import org.whipper.xml.XmlHelper;
import org.whipper.xml.result.ObjectFactory;
import org.whipper.xml.result.QueryResultType;

public class MetaQuerySetResultMode implements ResultMode{

    private static final ExpectedResultHolder EMPTY = new ExpectedResultHolder();
    private static final Logger LOG = LoggerFactory.getLogger(MetaQuerySetResultMode.class);

    static{
        ObjectFactory o = new ObjectFactory();
        QueryResultType qrt = o.createQueryResultType();
        qrt.setNoResult(o.createQueryResultTypeNoResult());
        EMPTY.setOriginalResult(qrt);
    }

    private File outputDir;
    private final String scenario;

    public MetaQuerySetResultMode(String scenario){
        this.scenario = scenario;
    }

    @Override
    public void resetConfiguration(Properties props){
        outputDir = new File(props.getProperty(Keys.OUTPUT_DIR), scenario);
    }

    @Override
    public File getErrorFile(Query q){
        return getErrorFile(q, q.getId());
    }

    private File getErrorFile(Query q, String origin){
        return new File(outputDir, "errors_for_" + getName() + File.separator
                + q.getSuite().getId() + "_" + q.getId() + "_" + origin + "_error.xml");
    }

    @Override
    public ResultHolder handleResult(Query q){
        ResultHolder out = new ResultHolder();
        if(q.getActualResult().isException()){
            out.setErrors(Arrays.asList("Before-set query failed - " + q.getActualResult().getOriginalExceptionMessage()));
            File errorFileXml = getErrorFile(q, q.getQuerySet().getMainId());
            try{
                // TODO add information about main query set
                if(!errorFileXml.exists()){
                    errorFileXml.getParentFile().mkdirs();
                    errorFileXml.createNewFile();
                }
                XmlHelper.writeError(q, EMPTY, errorFileXml);
            } catch (IOException ex){
                out.setException(ex);
            }
        }
        return out;
    }

    public void writeErrorsForMainQuerySet(QuerySet qs){
        for(Query q : qs.getQueries()){
            try{
                File f = q.getResultMode().getErrorFile(q);
                if(f == null){
                    LOG.warn("Result mode {} does not support error files.", q.getResultMode().getName());
                } else {
                    if(!f.exists()){
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                    }
                    XmlHelper.writeError(q, EMPTY, f);
                }
            } catch (IOException ex){
                LOG.warn("Cannot write error for query {}", q.getId(), ex);
            }
        }
    }

    @Override
    public void destroy(){}

    @Override
    public String getName(){
        return "MQSRM";
    }
}
