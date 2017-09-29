package org.whipper.web.job;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jdurani on 28.3.2017.
 */
class WhipperJobInfo{

    private static final Logger LOG = LoggerFactory.getLogger(WhipperJobInfo.class);
    private static final String JOB_NAME = "job_name";
    private static final String STARTED = "started";
    private static final String FINISHED = "finished";

    private String jobName;
    private long started = -1;
    private long finished = -1;

    WhipperJobInfo(String jobName){
        this.jobName = jobName;
    }

    void jobStarted(){
        started = System.currentTimeMillis();
    }

    void jobFinished(){
        finished = System.currentTimeMillis();
    }

    long getStarted(){
        return started;
    }

    long getFinished(){
        return finished;
    }

    String getJobName(){
        return jobName;
    }

    void setJobName(String jobName){
        this.jobName = jobName;
    }

    JSONObject asJson(){
        return new JSONObject()
                .append(JOB_NAME, jobName)
                .append(STARTED, started)
                .append(FINISHED, finished);
    }

    void dumpToDir(File dir){
        try(FileWriter fw = new FileWriter(getDumpFile(dir))){
            asJson().write(fw, 2, 0);
        } catch (IOException e){
            LOG.error("Cannot write job info to file.", e);
        }
    }

    static WhipperJobInfo fromDir(File dir){
        try(FileReader fr = new FileReader(getDumpFile(dir))){
            JSONObject o = new JSONObject(new JSONTokener(fr));
            WhipperJobInfo out = new WhipperJobInfo(o.getString(JOB_NAME));
            out.started = o.getLong(STARTED);
            out.finished = o.getLong(FINISHED);
            return out;
        } catch (IOException | JSONException ex){
            LOG.error("Cannot read result from output directory - {}", ex.getMessage(), ex);
            return null;
        }
    }

    private static File getDumpFile(File dir){
        return new File(dir, "job-info.json");
    }
}
