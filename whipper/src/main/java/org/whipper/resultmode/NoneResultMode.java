package org.whipper.resultmode;

import java.io.File;

import org.whipper.Query;
import org.whipper.WhipperProperties;

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
    public void resetConfiguration(WhipperProperties props) {}

    @Override
    public File getErrorFile(Query q){
        return null;
    }

    @Override
    public String getName() {
        return "NONE";
    }
}
