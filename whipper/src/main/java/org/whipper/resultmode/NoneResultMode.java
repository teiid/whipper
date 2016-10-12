package org.whipper.resultmode;

import java.io.File;
import java.util.Properties;

import org.whipper.Query;

/**
 * This is a none result mode. It will do nothing with the result.
 *
 * @author Juraj Duráni
 */
public class NoneResultMode implements ResultMode {

    @Override
    public ResultHolder handleResult(Query q){
        return new ResultHolder();
    }

    @Override
    public void destroy() {}

    @Override
    public void resetConfiguration(Properties props) {}

    @Override
    public File getErrorFile(Query q){
        return null;
    }

    @Override
    public String getName() {
        return "NONE";
    }
}
