package org.whipper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhipperResult implements Iterable<Entry<String, WhipperResult.Result>>{

    private static final Logger LOG = LoggerFactory.getLogger(WhipperResult.class);
    private final Result result;

    public WhipperResult(){
        this(new Result(null, -1, -1, -1, "scenarios", 0));
    }

    private WhipperResult(Result result){
        this.result = result;
    }

    void collectStats(Scenario scen){
        Result h = new Result(scen.getId(), scen.getNumberOfAllQueries(), scen.getNumberOfPassedQueries(), scen.getNumberOfFailedQueries(), "suites", 1);
        result.nested.put(scen.getId(), h);
        for(Suite suite : scen.getSuites()){
            Result sh = new Result(suite.getId(), suite.getNumberOfAllQueries(), suite.getNumberOfPassedQueries(), suite.getNumberOfFailedQueries(), "query-sets", 2);
            h.nested.put(suite.getId(), sh);
            for(QuerySet qs : suite.getQuerySets()){
                Result qsh = new Result(qs.getId(), qs.getNumberOfAllQueries(), qs.getNumberOfPassedQueries(), qs.getNumberOfFailedQueries(), "queries", 3);
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
                    qsh.nested.put(q.getId(), new Result(q.getId(), 1, pass, fail, null, 4));
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

    public void dumpToDir(File dir){
        try(FileWriter fw = new FileWriter(getDumpFile(dir))){
            writeAsJson(fw);
        } catch (IOException ex){
            LOG.error("Cannot dump result - {}", ex.getMessage(), ex);
        }
    }

    public void writeAsJson(Writer out) throws IOException{
        result.write(out, 2);
    }

    @Override
    public String toString(){
        return result.toJSONString();
    }

    public static WhipperResult fromDir(File dir){
        try(FileReader fr = new FileReader(getDumpFile(dir))){
            JSONObject o = new JSONObject(new JSONTokener(fr));
            return new WhipperResult(Result.fromJson(o, 0));
        } catch (IOException | JSONException ex){
            LOG.error("Cannot read result from output directory - {}", ex.getMessage(), ex);
            return null;
        }
    }

    private static File getDumpFile(File dir){
        return new File(dir, "result.json");
    }

    public static class Result implements Iterable<Entry<String, Result>>, JSONString{
        private static final String ID = "id";
        private static final String ALL = "all";
        private static final String PASS = "pass";
        private static final String FAIL = "fail";
        private static final String SKIP = "skip";
        private final int nestedLevel;
        private final Map<String, Result> nested;
        private final String id;
        private final int all;
        private final int pass;
        private final int fail;
        private final int skip;
        private final String nestedKey;

        private Result(String id, int all, int pass, int fail, String nestedKey, int nestedLevel){
            this.id = id;
            this.all = all;
            this.pass = pass;
            this.fail = fail;
            this.skip = this.all < 0 ? -1 : this.all - this.pass - this.fail;
            this.nestedKey = nestedKey;
            this.nestedLevel = nestedLevel;
            nested = this.nestedKey == null ? null : new TreeMap<String, Result>();
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

        @Override
        public String toJSONString(){
            try(StringWriter sw = new StringWriter()){
                write(sw, 2);
                return sw.toString();
            } catch (IOException ex){
                throw new InternalError("IO exception in StringWriter " + ex.toString());
            }
        }

        private void write(Writer w, int indent){
            JSONObject o = new JSONObject();
            if(id != null) {o.put(ID, id); }
            if(all >= 0){ o.put(ALL, all); }
            if(pass >= 0){ o.put(PASS, pass); }
            if(fail >= 0){ o.put(FAIL, fail); }
            if(skip >= 0){ o.put(SKIP, skip); }
            if(nested != null){ o.put(nestedKey, nested); }
            o.write(w, indent, indent * nestedLevel);
        }

        private static Result fromJson(JSONObject o, int nestedLevel){
            int a = o.optInt(ALL, -1);
            int p = o.optInt(PASS, -1);
            int f = o.optInt(FAIL, -1);
            String i = o.optString(ID, null);
            JSONObject n = null;
            String nk = null;
            for(String k : o.keySet()){
                if(!ID.equals(k) &&
                        !ALL.equals(k) &&
                        !PASS.equals(k) &&
                        !FAIL.equals(k) &&
                        !SKIP.equals(k)){
                    n = o.getJSONObject(k);
                    nk = k;
                }
            }
            Result r = new Result(i, a, p, f, nk, nestedLevel);
            if(nk != null){
                ++nestedLevel;
                for(Entry<String, Object> e : n.toMap().entrySet()){
                    if(e.getValue() instanceof Map){
                        r.nested.put(e.getKey(), Result.fromJson(new JSONObject((Map<?, ?>)e.getValue()), nestedLevel));
                    } else {
                        r.nested.put(e.getKey(), Result.fromJson((JSONObject)e.getValue(), nestedLevel));
                    }
                }
            }
            return r;
        }
    }
}
