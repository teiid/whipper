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

/**
 * Class which holds result of the test.
 */
public class WhipperResult implements Iterable<Entry<String, WhipperResult.Result>>{

    private static final Logger LOG = LoggerFactory.getLogger(WhipperResult.class);
    private final Result result;

    /**
     * Creates new result.
     */
    public WhipperResult(){
        this(new Result(null, -1, -1, -1, "scenarios", 0));
    }

    /**
     * Creates new result.
     *
     * @param result result of the test.
     */
    private WhipperResult(Result result){
        this.result = result;
    }

    /**
     * Collects results of the scenario and appends them to this result.
     *
     * @param scen scenario
     */
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

    /**
     * Returns result of the scenario.
     *
     * @param scenario scenario ID
     * @return result of specified scenario or {@code null}
     *      if result does not exist
     */
    public Result get(String scenario){
        return result.get(scenario);
    }

    /**
     * Returns result of the suite.
     *
     * @param scenario scenario ID
     * @param suite suite ID
     * @return result of specified suite or {@code null}
     *      if result does not exist
     */
    public Result get(String scenario, String suite){
        Result s = get(scenario);
        return s == null ? null : s.get(suite);
    }

    /**
     * Returns result of the query set.
     *
     * @param scenario scenario ID
     * @param suite suite ID
     * @param querySet query set ID
     * @return result of specified query set or {@code null}
     *      if result does not exist
     */
    public Result get(String scenario, String suite, String querySet){
        Result s = get(scenario, suite);
        return s == null ? null : s.get(querySet);
    }

    /**
     * Returns result of the query.
     *
     * @param scenario scenario ID
     * @param suite suite ID
     * @param querySet query set ID
     * @param query query ID
     * @return result of specified query or {@code null}
     *      if result does not exist
     */
    public Result get(String scenario, String suite, String querySet, String query){
        Result qs = get(scenario, suite, querySet);
        return qs == null ? null : qs.get(query);
    }

    @Override
    public Iterator<Entry<String, WhipperResult.Result>> iterator(){
        return result.iterator();
    }

    /**
     * Returns result of all scenarios.
     *
     * @return result of all scenarios (never {@code null})
     */
    public Result get(){
        return result;
    }

    /**
     * Writes this result to file in specified directory.
     *
     * @param dir output directory
     */
    public void dumpToDir(File dir){
        try(FileWriter fw = new FileWriter(getDumpFile(dir))){
            writeAsJson(fw);
        } catch (IOException ex){
            LOG.error("Cannot dump result - {}", ex.getMessage(), ex);
        }
    }

    /**
     * Writes this result to {@link Writer} in JSON format.
     *
     * @param out output {@link Writer}
     * @throws IOException if an I/O error occurs
     */
    public void writeAsJson(Writer out) throws IOException{
        result.write(out, 2);
    }

    @Override
    public String toString(){
        return result.toJSONString();
    }

    /**
     * Loads result from file in specified directory.
     *
     * @param dir directory where file with result is located
     * @return returns result loaded from the directory or {@code null}
     *      in case of I/O error
     */
    public static WhipperResult fromDir(File dir){
        try(FileReader fr = new FileReader(getDumpFile(dir))){
            JSONObject o = new JSONObject(new JSONTokener(fr));
            return new WhipperResult(Result.fromJson(o, 0));
        } catch (IOException | JSONException ex){
            LOG.error("Cannot read result from output directory - {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Returns file of result.
     *
     * @param dir directory with the file
     * @return result file
     */
    private static File getDumpFile(File dir){
        return new File(dir, "result.json");
    }

    /**
     * Class which holds pass/fail/skip/all result statistics. Can contain also nested
     * results.
     */
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

        /**
         * Creates new instance.
         *
         * @param id id of the result
         * @param all all queries
         * @param pass passed queries
         * @param fail failed queries
         * @param nestedKey key to use for nested results (in JSON object)
         * @param nestedLevel nested level of this result in deeper JSON object
         *      hierarchy (for pretty print)
         */
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

        /**
         * Returns nested result with specified key.
         *
         * @param key key of nested result
         * @return nested resultor {@code null}
         *      if result does not exist
         */
        public Result get(String key){
            return key == null ? null : nested.get(key);
        }

        /**
         * Returns all nested keys of this result.
         *
         * @return nested result keys or {@code null}
         *      if this result does not have nested results
         */
        public Collection<String> get(){
            return nested == null ? null : nested.keySet();
        }

        /**
         * Return number of all queries.
         *
         * @return number of all queries
         */
        public int getAll(){
            return all;
        }

        /**
         * Return number of passed queries.
         *
         * @return number of passed queries
         */
        public int getPass(){
            return pass;
        }

        /**
         * Return number of failed queries.
         *
         * @return number of failed queries
         */
        public int getFail(){
            return fail;
        }

        /**
         * Return number of skipped queries.
         *
         * @return number of skipped queries
         */
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

        /**
         * Writes this result in JSON format to {@link Writer}.
         *
         * @param w writer
         * @param indent indent
         */
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

        /**
         * Reads result from JSON format.
         *
         * @param o JSON object
         * @param nestedLevel nested level of the result
         * @return result read from JSON object
         */
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
