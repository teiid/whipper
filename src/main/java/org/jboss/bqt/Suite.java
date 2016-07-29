package org.jboss.bqt;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.jboss.bqt.exceptions.ExecutionInterruptedException;
import org.jboss.bqt.exceptions.MaxTimeExceededException;
import org.jboss.bqt.exceptions.ServerNotAvailableException;
import org.jboss.bqt.exceptions.DbNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which represents a test suite. It can contain one or more query sets.
 *
 * @author Juraj Duráni
 */
public class Suite implements Comparable<Suite>{

    private static final Logger LOG = LoggerFactory.getLogger(Suite.class);

    private final List<QuerySet> querySets = new LinkedList<QuerySet>();
    private final List<TestResult> querySetResults = new LinkedList<TestResult>();
    private final String id;
    private final TestResult result = new SuiteResult();
    private long startTime;
    private long endTime;

    /**
     * Creates a new instance.
     *
     * @param id ID
     */
    public Suite(String id) {
        this.id = id;
    }

    /**
     * Adds new query set.
     *
     * @param qs query set to be added.
     */
    public void addQuerySet(QuerySet qs){
        if(qs == null){
            throw new IllegalArgumentException("QuerySet cannot be null.");
        }
        querySets.add(qs);
    }

    /**
     * Returns number of all queries in this suite.
     *
     * @return number of all queries
     */
    int getNumberOfAllQueries(){
        int num = 0;
        for(QuerySet qs : querySets){
            num += qs.getNumberOfAllQueries();
        }
        return num;
    }

    /**
     * Returns number of failed queries in this suite.
     *
     * @return number of failed queries
     */
    int getNumberOfFailedQueries(){
        int num = 0;
        for(QuerySet qs : querySets){
            num += qs.getNumberOfFailedQueries();
        }
        return num;
    }

    /**
     * Returns number of passed queries in this suite.
     *
     * @return number of passed queries
     */
    int getNumberOfPassedQueries(){
        int num = 0;
        for(QuerySet qs : querySets){
            num += qs.getNumberOfPassedQueries();
        }
        return num;
    }

    /**
     * Returns number of executed queries int this suite.
     *
     * @return number of executed queries
     */
    int getNumberOfExecutedQueries(){
        int num = 0;
        for(QuerySet qs : querySets){
            num += qs.getNumberOfExecutedQueries();
        }
        return num;
    }

    /**
     * Returns ID.
     *
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * Runs test.
     *
     * @param maxEndTime maximum end time
     * @return result of the test
     * @throws ServerNotAvailableException if server is not available
     * @throws DbNotAvailableException if VDB is not available
     * @throws MaxTimeExceededException if {@code maxEndTime} has been reached
     * @throws ExecutionInterruptedException if thread has been interrupted
     */
    public TestResult run(long maxEndTime) throws ServerNotAvailableException, DbNotAvailableException,
                MaxTimeExceededException, ExecutionInterruptedException{
        LOG.info("Starting suite {}.", id);
        querySetResults.clear();
        startTime = System.currentTimeMillis();
        try{
            for(QuerySet qs : querySets){
                try{
                    qs.runQueries();
                } finally{
                    querySetResults.add(qs.getResult());
                }
                if(maxEndTime >= 0 && System.currentTimeMillis() >= maxEndTime){
                    throw new MaxTimeExceededException("Max time exceeded.");
                }
            }
        } finally {
            endTime = System.currentTimeMillis();
            LOG.info("Suite {} finished.", id);
        }
        return result;
    }

    /**
     * Return result of the test.
     *
     * @return test result
     */
    public TestResult getResult() {
        return result;
    }

    /**
     * Returns failed queries in this suite.
     *
     * @return failed queries
     */
    public List<Query> getFailedQueries(){
        List<Query> out = new LinkedList<Query>();
        for(QuerySet qs : querySets){
            out.addAll(qs.getFailedQueries());
        }
        return out;
    }

    @Override
    public int compareTo(Suite o) {
        return o == null ? 1 : this.id.compareTo(o.id);

    }

    /**
     * Result of the suite.
     *
     * @author Juraj Duráni
     */
    private class SuiteResult implements TestResult{
        @Override
        public void write(Writer wr) throws IOException {
            if(!querySetResults.isEmpty()){
                wr.append(BqtTool.LS);
            }
            for(TestResult res : querySetResults){
                res.write(wr);
            }
        }

        @Override
        public void writeHeader(Writer wr) throws IOException{
            BqtTool.appendLine(wr, "Suite - " + id);
            BqtTool.appendLine(wr, "============================");
            BqtTool.appendLine(wr, "Start Time:                 " + BqtTool.DATE_FORMAT.format(new java.util.Date(startTime)));
            BqtTool.appendLine(wr, "End Time:                   " + BqtTool.DATE_FORMAT.format(new java.util.Date(endTime)));
            BqtTool.appendLine(wr, "Elapsed:                    " + BqtTool.timeToString(endTime - startTime));
            int all = getNumberOfAllQueries();
            int exec = getNumberOfExecutedQueries();
            BqtTool.appendLine(wr, "Number of all queries:      " + all);
            BqtTool.appendLine(wr, "Number of skipped queries:  " + (all - exec));
            BqtTool.appendLine(wr, "Number of executed queries: " + exec);
            BqtTool.appendLine(wr, "Number of passed queries:   " + getNumberOfPassedQueries());
            BqtTool.appendLine(wr, "Number of failed queries:   " + getNumberOfFailedQueries());
            BqtTool.appendLine(wr, "============================");
        }
    }
}
