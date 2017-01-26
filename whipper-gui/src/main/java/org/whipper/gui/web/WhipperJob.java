package org.whipper.gui.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
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

    public synchronized String resultToJson(){
        JSONObject o = new JSONObject(scenarios);
        if(runningMetaQS != null){
            o.put("runningMetaQuerySet", runningMetaQS);
            if(runningMetaQ != null){
                o.put("runningMetaQuery", runningMetaQ);
            }
        }
        return o.toJSONString();
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

    public boolean finished(){
        return finished;
    }

    public File getFullPathToJob(){
        return props.getOutputDir();
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
            scH.all = scR.getAll();
            scH.pass = scR.getPass();
            scH.fail = scR.getFail();
            scH.countSkip();
            for(Entry<String, WhipperResult.Result> su : scR){
                WhipperResult.Result suR = su.getValue();
                Holder suH = createAndPut(scH.nested, su.getKey(), QUERY_SETS);
                suH.all = suR.getAll();
                suH.pass = suR.getPass();
                suH.fail = suR.getFail();
                suH.countSkip();
                for(Entry<String, WhipperResult.Result> qs : suR){
                    WhipperResult.Result qsR = qs.getValue();
                    Holder qsH = createAndPut(suH.nested, qs.getKey(), QUERIES);
                    qsH.all = qsR.getAll();
                    qsH.pass = qsR.getPass();
                    qsH.fail = qsR.getFail();
                    qsH.countSkip();
                    for(Entry<String, WhipperResult.Result> q : qsR){
                        WhipperResult.Result qR = q.getValue();
                        Holder qH = createAndPut(qsH.nested, q.getKey(), null);
                        qH.all = 1;
                        qH.pass = qR.getPass();
                        qH.fail = qR.getFail();
                        qH.countSkip();
                    }
                }
            }
        }
        finished = true;
    }

    private Holder createAndPut(Map<String, Holder> map, String key, String nestedKey){
        Holder h = new Holder(nestedKey);
        map.put(key, h);
        return h;
    }

    @Override
    public synchronized void startingScenario(Scenario scen){
        currentScenario = createAndPut(scenarios, scen.getId(), SUITES);
        currentScenario.all = scen.getNumberOfAllQueries();
        currentScenario.nested = new TreeMap<>();
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
        currentSuite.nested = new TreeMap<>();
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
        currentQuery.nested = new TreeMap<>();
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

    private static class Holder implements JSONAware{
        private final String nestedKey;
        private Map<String, Holder> nested;
        private int all;
        private int pass;
        private int fail;
        private int skip;
        private String err;

        private Holder(String nestedKey){
            this.nestedKey = nestedKey;
        }

        private void countSkip(){
            skip = all - fail - pass;
        }

        @Override
        public String toJSONString(){
            JSONObject o = new JSONObject();
            if(nested != null){
                o.put(nestedKey, new JSONObject(nested));
            }
            o.put("all", all);
            o.put("pass", pass);
            o.put("fail", fail);
            o.put("skip", skip);
            if(err != null){
                o.put("error", err);
            }
            return o.toJSONString();
        }
    }
}
