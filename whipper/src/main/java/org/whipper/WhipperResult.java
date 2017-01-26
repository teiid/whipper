package org.whipper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class WhipperResult implements Iterable<Entry<String, WhipperResult.Result>>{

    private final Result result = new Result(0, 0, 0, false);

    void collectStats(Scenario scen){
        Result h = new Result(scen.getNumberOfAllQueries(), scen.getNumberOfPassedQueries(), scen.getNumberOfFailedQueries(), false);
        result.nested.put(scen.getId(), h);
        for(Suite suite : scen.getSuites()){
            Result sh = new Result(suite.getNumberOfAllQueries(), suite.getNumberOfPassedQueries(), suite.getNumberOfFailedQueries(), false);
            h.nested.put(suite.getId(), sh);
            for(QuerySet qs : suite.getQuerySets()){
                Result qsh = new Result(qs.getNumberOfAllQueries(), qs.getNumberOfPassedQueries(), qs.getNumberOfFailedQueries(), false);
                sh.nested.put(qs.getId(), qsh);
                for(Query q : qs.getQueries()){
                    int pass;
                    int fail;
                    if(q.isExecuted()){
                        pass = q.getResult().pass() ? 1 : 0;
                        fail = 1 - pass;
                    } else {
                        pass = 0;
                        fail = 0;
                    }
                    qsh.nested.put(q.getId(), new Result(1, pass, fail, true));
                }
            }
        }
    }

    public Result get(String scenario){
        return result.get(scenario);
    }

    public Result get(String scenario, String suite){
        Result s = get(scenario);
        return s == null ? null : s.get(suite);
    }

    public Result get(String scenario, String suite, String querySet){
        Result s = get(scenario, suite);
        return s == null ? null : s.get(querySet);
    }

    public Result get(String scenario, String suite, String querySet, String query){
        Result qs = get(scenario, suite, querySet);
        return qs == null ? null : qs.get(query);
    }

    @Override
    public Iterator<Entry<String, WhipperResult.Result>> iterator(){
        return result.iterator();
    }

    public Result get(){
        return result;
    }

    public static class Result implements Iterable<Entry<String, Result>>{
        private final Map<String, Result> nested;
        private final int all;
        private final int pass;
        private final int fail;
        private final int skip;

        private Result(int all, int pass, int fail, boolean isQuery){
            this.all = all;
            this.pass = pass;
            this.fail = fail;
            this.skip = this.all - this.pass - this.fail;
            nested = isQuery ? null : new TreeMap<String, Result>();
        }

        public Result get(String key){
            return key == null ? null : nested.get(key);
        }

        public Collection<String> get(){
            return nested == null ? null : nested.keySet();
        }

        public int getAll(){
            return all;
        }

        public int getPass(){
            return pass;
        }

        public int getFail(){
            return fail;
        }

        public int getSkip(){
            return skip;
        }

        @Override
        public Iterator<Entry<String, Result>> iterator(){
            return nested == null ? null : nested.entrySet().iterator();
        }
    }
}
