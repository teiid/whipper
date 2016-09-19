package org.whipper.resultmode;

import java.util.Properties;

import org.whipper.Query;

/**
 * This is a none result mode. It will do nothing with the result.
 *
 * @author Juraj Duráni
 */
public class NoneResultMode implements ResultMode {

    @Override
    public ResultHandler handleResult(Query q){
        return new ResultHandler();
    }

    @Override
    public void destroy() {}

    @Override
    public void resetConfiguration(Properties props) {}

    @Override
    public String getName() {
        return "NONE";
    }
}
