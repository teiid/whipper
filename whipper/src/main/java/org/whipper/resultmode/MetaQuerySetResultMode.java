package org.whipper.resultmode;

import java.util.Properties;

import org.whipper.Query;

public class MetaQuerySetResultMode implements ResultMode{

    @Override
    public void resetConfiguration(Properties props){}

    @Override
    public ResultHandler handleResult(Query q){
        ResultHandler out = new ResultHandler();
        if(q.getActualResult().isException()){
            out.setException(q.getActualResult().getOriginalException());
        }
        return out;
    }

    @Override
    public void destroy(){}

    @Override
    public String getName(){
        return "meta-query-set";
    }
}
