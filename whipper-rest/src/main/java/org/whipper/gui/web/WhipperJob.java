package org.whipper.gui.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.whipper.ProgressMonitor;
import org.whipper.Query;
import org.whipper.Query.QueryResult;
import org.whipper.QuerySet;
import org.whipper.Scenario;
import org.whipper.Suite;
import org.whipper.Whipper;
import org.whipper.WhipperProperties;
import org.whipper.WhipperResult;

public class WhipperJob implements ProgressMonitor{

    private static final String ID = "id";
    private static final String SCENARIOS = "scenarios";
    private static final String SUITES = "suites";
    private static final String QUERY_SETS = "querySets";
    private static final String QUERIES = "queries";

    private final String id;
    private final WhipperProperties props;
    private Whipper whipper;
    private volatile boolean finished;

    private final Map<String, Holder> scenarios = new TreeMap<>();
    private Holder currentScenario;
    private Holder currentSuite;
    private Holder currentQuerySet;
    private Holder currentQuery;
    private String runningMetaQS;
    private String runningMetaQ;

    WhipperJob(String id, WhipperProperties props){
        this.id = id;
        this.props = props;
    }

    public String getId(){
        return id;
    }

    public void start(){
        finished = false;
        whipper = new Whipper(this.props);
        whipper.registerProgressMonitor(this);
        whipper.start(true);
    }

    public void stop(){
        if(whipper != null){
            whipper.stop();
        }
    }

    public synchronized JSONObject resultToJson(){
        JSONObject o = new JSONObject();
        o.put(ID, id);
        JSONArray ar = new JSONArray(scenarios.values());
        o.put(SCENARIOS, ar);
        if(runningMetaQS != null){
            o.put("runningMetaQuerySet", runningMetaQS);
            if(runningMetaQ != null){
                o.put("runningMetaQuery", runningMetaQ);
            }
        }
        return o;
    }

    public synchronized JSONObject briefResultToJson(){
        JSONObject o = new JSONObject();
        o.put(ID, id);
        JSONArray ar = new JSONArray();
        o.put(SCENARIOS, ar);
        for(Entry<String, Holder> e : scenarios.entrySet()){
            ar.put(e.getValue().briefSummary());
        }
        return o;
    }

    public InputStream resultToZip(Context context){
        if(finished){
            return null;
        }
        // we assume there is exactly one instance per each job id in the application
        // otherwise we need a better synchronization
        synchronized (this) {
            File f = context.getTmpDir("job_result_ " + id + ".zip");
            try{
                if(!f.exists()){
                    Utils.zip(getFullPathToJob(), f);
                }
                return new FileInputStream(f);
            } catch (IOException ex){
                Utils.LOG.error("Cannot create ZIP archive.", ex);
                f.delete();
                return null;
            }
        }
    }

    public boolean isFinished(){
        return finished;
    }

    public File getFullPathToJob(){
        return props.getOutputDir();
    }

    static WhipperJob fromDir(String id, File f){
        WhipperProperties wp = WhipperProperties.fromOutputDir(f);
        WhipperResult wr = WhipperResult.fromDir(f);
        if(wp == null || wr == null){
            return null;
        }
        WhipperJob out = new WhipperJob(id, wp);
        out.finished(wr);
        return out;
    }

      /******************************/
     /*****  progress monitor  *****/
    /******************************/

    @Override
    public synchronized void starting(List<String> scenariosToRun){
        scenarios.clear();
        for(String s : scenariosToRun){
            scenarios.put(s, null);
        }
    }

    @Override
    public synchronized void finished(WhipperResult result){
        scenarios.clear();
        for(Entry<String, WhipperResult.Result> sc : result){
            WhipperResult.Result scR = sc.getValue();
            Holder scH = createAndPut(scenarios, sc.getKey(), SUITES);
            fillHolder(scH, scR);
            for(Entry<String, WhipperResult.Result> su : scR){
                WhipperResult.Result suR = su.getValue();
                Holder suH = createAndPut(scH.nested, su.getKey(), QUERY_SETS);
                fillHolder(suH, suR);
                for(Entry<String, WhipperResult.Result> qs : suR){
                    WhipperResult.Result qsR = qs.getValue();
                    Holder qsH = createAndPut(suH.nested, qs.getKey(), QUERIES);
                    fillHolder(qsH, qsR);
                    for(Entry<String, WhipperResult.Result> q : qsR){
                        WhipperResult.Result qR = q.getValue();
                        Holder qH = createAndPut(qsH.nested, q.getKey(), null);
                        fillHolder(qH, qR);
                    }
                }
            }
        }
        finished = true;
    }

    private void fillHolder(Holder h, WhipperResult.Result r){
        h.all = r.getAll();
        h.pass = r.getPass();
        h.fail = r.getFail();
        h.countSkip();
    }

    private Holder createAndPut(Map<String, Holder> map, String key, String nestedKey){
        Holder h = new Holder(key, nestedKey);
        map.put(key, h);
        return h;
    }

    @Override
    public synchronized void startingScenario(Scenario scen){
        currentScenario = createAndPut(scenarios, scen.getId(), SUITES);
        currentScenario.all = scen.getNumberOfAllQueries();
        for(Suite s : scen.getSuites()){
            currentScenario.nested.put(s.getId(), null);
        }
    }

    @Override
    public synchronized void scenarioFinished(Scenario scen){
        currentScenario.countSkip();
        currentScenario = null;
    }

    @Override
    public synchronized void startingSuite(Suite suite){
        currentSuite = createAndPut(currentScenario.nested, suite.getId(), QUERY_SETS);
        currentSuite.all = suite.getNumberOfAllQueries();
        for(QuerySet qs : suite.getQuerySets()){
            currentSuite.nested.put(qs.getId(), null);
        }
    }

    @Override
    public synchronized void suiteFinished(Suite suite){
        currentSuite.countSkip();
        currentSuite = null;
    }

    @Override
    public synchronized void startingQuerySet(QuerySet qs){
        currentQuerySet = createAndPut(currentSuite.nested, qs.getId(), QUERIES);
        currentQuerySet.all = qs.getNumberOfAllQueries();
        for(Query q : qs.getQueries()){
            currentQuerySet.nested.put(q.getId(), null);
        }
    }

    @Override
    public synchronized void querySetFinished(QuerySet qs){
        currentQuerySet.countSkip();
        currentQuerySet = null;
    }

    @Override
    public synchronized void startingQuery(Query q){
        currentQuery = createAndPut(currentQuerySet.nested, q.getId(), null);
        currentQuery.all = 1;
    }

    @Override
    public synchronized void queryFinished(Query q){
        if(!q.isExecuted()){
            currentQuery.skip++;
            currentQuerySet.skip++;
            currentSuite.skip++;
            currentScenario.skip++;
        } else if(q.getResult().pass()){
            currentQuery.pass++;
            currentQuerySet.pass++;
            currentSuite.pass++;
            currentScenario.pass++;
        } else {
            QueryResult qr = q.getResult();
            currentQuery.fail++;
            currentQuerySet.fail++;
            currentSuite.fail++;
            currentScenario.fail++;
            currentQuery.err = qr.isError() ? qr.getErrors().get(0) : qr.getException().toString();
        }
        currentQuery = null;
    }

    @Override
    public synchronized void startingMetaQuerySet(QuerySet qs){
        runningMetaQS = qs.getId();
    }

    @Override
    public synchronized void metaQuerySetFinished(QuerySet qs){
        runningMetaQS = null;
    }

    @Override
    public synchronized void startingMetaQuery(Query q){
        runningMetaQ = q.getId();
    }

    @Override
    public synchronized void metaQueryFinished(Query q){
        runningMetaQ = null;
    }

    private static class Holder implements JSONString{
        private final String nestedKey;
        private final Map<String, Holder> nested;
        private final String id;
        private int all;
        private int pass;
        private int fail;
        private int skip;
        private String err;

        private Holder(String id, String nestedKey){
            this.id = id;
            this.nestedKey = nestedKey;
            this.nested = this.nestedKey == null ? null : new TreeMap<String, Holder>();
        }

        private void countSkip(){
            skip = all - fail - pass;
        }

        @Override
        public String toJSONString(){
            JSONObject o = briefSummary();
            if(nested != null){
                o.put(nestedKey, new JSONObject(nested));
            }
            if(err != null){
                o.put("error", err);
            }
            return o.toString();
        }

        public JSONObject briefSummary(){
            JSONObject o = new JSONObject();
            o.put(ID, id);
            o.put("all", all);
            o.put("pass", pass);
            o.put("fail", fail);
            o.put("skip", skip);
            return o;
        }
    }
}
