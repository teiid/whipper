package org.whipper.web;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.ProgressMonitor;
import org.whipper.Query;
import org.whipper.Query.QueryResult;
import org.whipper.QuerySet;
import org.whipper.Scenario;
import org.whipper.Suite;
import org.whipper.Whipper;
import org.whipper.WhipperProperties;
import org.whipper.WhipperResult;

/**
 * Class which represents one whipper job (i.e. one test run).
 */
public class WhipperJob implements ProgressMonitor{

    private static final Logger LOG = LoggerFactory.getLogger(WhipperJob.class);
    private static final String ID = "id";
    private static final String JOB_INFO = "job-info";
    private static final String SCENARIOS = "scenarios";
    private static final String SUITES = "suites";
    private static final String QUERY_SETS = "querySets";
    private static final String QUERIES = "queries";

    private final String id;
    private final WhipperProperties props;
    private final WhipperJobInfo jobInfo;
    private Whipper whipper;
    private volatile boolean finished;

    private final Map<String, Holder> scenarios = new TreeMap<>();
    private Holder currentScenario;
    private Holder currentSuite;
    private Holder currentQuerySet;
    private Holder currentQuery;
    private String runningMetaQS;
    private String runningMetaQ;

    /**
     * Creates new job.
     *
     * @param id ID of the job
     * @param jobName custom name of the job
     * @param props initial properties
     */
    WhipperJob(String id, String jobName, WhipperProperties props){
        this.id = id;
        this.jobInfo = new WhipperJobInfo(jobName == null ? getDefaultJobName(id) : jobName);
        this.props = props;
    }

    /**
     * Creates new job.
     *
     * @param id job ID
     * @param jobInfo job info
     * @param props properties
     */
    private WhipperJob(String id, WhipperJobInfo jobInfo, WhipperProperties props){
        this.id = id;
        this.props = props;
        this.jobInfo = jobInfo;
    }

    /**
     * Returns default job name.
     *
     * @param id id of the job
     * @return default job name
     */
    private String getDefaultJobName(String id){
        return "Job " + id;
    }

    /**
     * Returns ID of this job.
     *
     * @return ID
     */
    public String getId(){
        return id;
    }

    /**
     * Starts job.
     */
    public void start(){
        File outDir = props.getOutputDir();
        if(!outDir.exists() && !outDir.mkdirs()){
            LOG.warn("Cannot create output directory {}", outDir);
        } else if(!outDir.isDirectory()){
            LOG.warn("Output directory {} is not a directory.", outDir);
        }
        finished = false;
        whipper = new Whipper(this.props);
        whipper.registerProgressMonitor(this);
        whipper.start(true);
        jobInfo.jobStarted();
        jobInfo.dumpToDir(outDir);
    }

    /**
     * Stops job.
     */
    public void stop(){
        if(whipper != null){
            whipper.stop();
        }
    }

    /**
     * Returns result of this job as a JSON object.
     *
     * @return result as a JSON object
     */
    public synchronized JSONObject resultToJson(){
        JSONObject o = getJsonObject();
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

    /**
     * Returns brief summary of this job.
     * <p>
     * Brief summary contains only overall information about number of
     * all/passed/failed/skipped queries.
     *
     * @return brief summary
     */
    public synchronized JSONObject briefResultToJson(){
        JSONObject o = getJsonObject();
        JSONArray ar = new JSONArray();
        o.put(SCENARIOS, ar);
        for(Entry<String, Holder> e : scenarios.entrySet()){
            ar.put(e.getValue().briefSummary());
        }
        return o;
    }

    /**
     * Returns basic object. Contains ID and job name.
     *
     * @return JSON object
     */
    private JSONObject getJsonObject(){
        JSONObject o = new JSONObject();
        o.put(ID, id);
        o.put(JOB_INFO, jobInfo.asJson());
        return o;
    }

    /**
     * Returns result of this job as a ZIP input stream.
     *
     * @param context Whipper application context
     * @return result or {@code null} if job is running
     * @see #isFinished()
     */
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

    /**
     * Returns {@code true} if this job has started and finished,
     * {@code false} otherwise.
     *
     * @return whether this job finished or not
     */
    public boolean isFinished(){
        return finished;
    }

    /**
     * Returns full path to the job's output directory.
     *
     * @return output directory of this job
     */
    public File getFullPathToJob(){
        return props.getOutputDir();
    }

    /**
     * Reads job from output directory.
     *
     * @param id ID of the job
     * @param f directory with this job.
     * @return new job or {@code null} if directory does not contain
     *      proper representation of the job
     */
    static WhipperJob fromDir(String id, File f){
        WhipperProperties wp = WhipperProperties.fromOutputDir(f);
        WhipperResult wr = WhipperResult.fromDir(f);
        WhipperJobInfo wji = WhipperJobInfo.fromDir(f);
        if(wp == null || wr == null || wji == null){
            return null;
        }
        WhipperJob out = new WhipperJob(id, wji, wp);
        out.finished(wr);
        return out;
    }

      /* **************************** */
     /* ****  progress monitor  **** */
    /* **************************** */

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
        jobInfo.jobFinished();
        jobInfo.dumpToDir(props.getOutputDir());
    }

    /**
     * Fills holder.
     *
     * @param h holder to be filled
     * @param r result
     */
    private void fillHolder(Holder h, WhipperResult.Result r){
        h.all = r.getAll();
        h.pass = r.getPass();
        h.fail = r.getFail();
        h.countSkip();
    }

    /**
     * Creates new holder and puts it into map.
     *
     * @param map map
     * @param key key of the holder
     * @param nestedKey nested key for the holder's nested holders
     * @return created holder
     */
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

    /**
     * Holder for result of the job.
     */
    private static class Holder implements JSONString{
        private final String nestedKey;
        private final Map<String, Holder> nested;
        private final String id;
        private int all;
        private int pass;
        private int fail;
        private int skip;
        private String err;

        /**
         * Creates new holder.
         *
         * @param id id of the holder
         * @param nestedKey nested key for nested holders (for JSON)
         */
        private Holder(String id, String nestedKey){
            this.id = id;
            this.nestedKey = nestedKey;
            this.nested = this.nestedKey == null ? null : new TreeMap<String, Holder>();
        }

        /**
         * Calculates and sets number of skipped queries.
         */
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

        /**
         * Returns brief summary as a JSON object.
         *
         * @return brief summary (without nested holders)
         */
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
