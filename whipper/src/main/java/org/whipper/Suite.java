package org.whipper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ExecutionInterruptedException;
import org.whipper.exceptions.MaxTimeExceededException;
import org.whipper.exceptions.ServerNotAvailableException;

/**
 * Class which represents a test suite. It can contain one or more query sets.
 *
 * @author Juraj Duráni
 */
public class Suite implements Comparable<Suite>, TimeTracker{

    private static final String AFTER_SUITE = "after_suite";

    private static final String AFTER_SET = "after_set";

    private static final String BEFORE_SET = "before_set";

    private static final String BEFORE_SUITE = "before_suite";

    private static final Logger LOG = LoggerFactory.getLogger(Suite.class);

    private final List<QuerySet> querySets = new LinkedList<>();
    private final String id;
    private long startTime = -1;
    private long endTime = -1;
    private QuerySet beforeEach;
    private QuerySet afterEach;
    private QuerySet beforeSuite;
    private QuerySet afterSuite;

    /**
     * Creates a new instance.
     *
     * @param id ID
     */
    public Suite(String id) {
        this.id = id;
    }

    @Override
    public long getStartTime(){
        return startTime;
    }

    @Override
    public long getEndTime(){
        return endTime;
    }

    @Override
    public long getDuration(){
        return (startTime < 0 || endTime < 0) ? -1l : endTime - startTime;
    }

    /**
     * Sets query set which will run after each query set in
     * this suite except those which have their own after-query-set.
     *
     * @param afterEach query set to be set
     */
    public void setAfterEach(QuerySet afterEach){
        this.afterEach = afterEach;
    }

    /**
     * Sets query set which will run before each query set in
     * this suite except those which have their own before-query-set.
     * <p>
     * If query set fails (i.e. any of the query ends exceptionally) query
     * set will not be processed and marked as failed.
     *
     * @param beforeEach query set to be set
     */
    public void setBeforeEach(QuerySet beforeEach){
        this.beforeEach = beforeEach;
    }


    /**
     * Sets query set which will run after whole suite.
     *
     * @param afterSuite query set to be set
     */
    public void setAfterSuite(QuerySet afterSuite){
        this.afterSuite = afterSuite;
    }

    /**
     * Sets query which will run before whole suite.
     *
     * @param beforeSuite query set to be set
     */
    public void setBeforeSuite(QuerySet beforeSuite){
        this.beforeSuite = beforeSuite;
    }

    /**
     * Adds new query set.
     *
     * @param qs query set to be added
     */
    public void addQuerySet(QuerySet qs){
        if(qs == null){
            throw new IllegalArgumentException("QuerySet cannot be null.");
        }
        querySets.add(qs);
    }

    /**
     * Returns unmodifiable list of query sets.
     *
     * @return list of query sets
     */
    public List<QuerySet> getQuerySets(){
        return Collections.unmodifiableList(querySets);
    }

    /**
     * Returns number of all queries in this suite.
     *
     * @return number of all queries
     */
    public int getNumberOfAllQueries(){
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
    public int getNumberOfFailedQueries(){
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
    public int getNumberOfPassedQueries(){
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
    public int getNumberOfExecutedQueries(){
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
     * Runs all query sets in this suite.
     *
     * @param maxEndTime maximum end time
     * @throws ServerNotAvailableException if server is not available
     * @throws DbNotAvailableException if VDB is not available
     * @throws MaxTimeExceededException if {@code maxEndTime} has been reached
     * @throws ExecutionInterruptedException if thread has been interrupted
     */
    public void run(long maxEndTime) throws ServerNotAvailableException, DbNotAvailableException,
                MaxTimeExceededException, ExecutionInterruptedException{
        LOG.info("Starting suite {}.", id);
        startTime = System.currentTimeMillis();
        try{
            Throwable fail;
            if((fail = runMeta(beforeSuite, null, id, BEFORE_SUITE)) != null){
                LOG.error("Before-suite failed [{}]", id, fail);
                for(QuerySet qs : querySets){
                    qs.beforeFailed(fail, BEFORE_SUITE);
                }
            } else {
                for(QuerySet qs : querySets){
                    // main only if before succeed
                    if((fail = runMeta(qs.getBefore(), beforeEach, qs.getId(), BEFORE_SET)) == null){
                        qs.runQueries();
                    } else {
                        LOG.error("Before-set failed [{}]", qs.getId(), fail);
                        qs.beforeFailed(fail, BEFORE_SET);
                    }
                    // after always
                    if((fail = runMeta(qs.getAfter(), afterEach, qs.getId(), AFTER_SET)) != null){
                        LOG.error("After-set failed [{}]", qs.getId(), fail);
                    }
                    if(maxEndTime >= 0 && System.currentTimeMillis() >= maxEndTime){
                        throw new MaxTimeExceededException("Max time exceeded.");
                    }
                }
            }
            if((fail = runMeta(afterSuite, null, id, AFTER_SUITE)) != null){
                LOG.error("After-suite failed [{}]", id, fail);
            }
        } finally {
            endTime = System.currentTimeMillis();
            LOG.info("Suite {} finished.", id);
        }
    }

    /**
     * Runs meta-query-set (MQS).
     *
     * @param qs MQS to be run
     * @param def default MQS in case {@code qs} is {@code null}
     * @param origin ID of the main query set for which this MQS will be run
     * @param type type of the MQS (i.e. before* / after*)
     *
     * @return cause of MQS failure or {@code null} if MQS finished without problems
     *
     * @throws ServerNotAvailableException if server is not available
     * @throws DbNotAvailableException is database is not available
     * @throws ExecutionInterruptedException if execution has been interrupted
     */
    private Throwable runMeta(QuerySet qs, QuerySet def, String origin, String type) throws ServerNotAvailableException, DbNotAvailableException, ExecutionInterruptedException{
        if(qs == null){
            qs = def;
        }
        if(qs != null){
            LOG.info("Running {} for {}", type, origin);
            qs.setMainId(origin + "_" + type);
            qs.runQueries();
            if(qs.getNumberOfFailedQueries() != 0){
                return qs.getFailedQueries().get(0).getResult().getException();
            }
            if(qs.getNumberOfAllQueries() != qs.getNumberOfExecutedQueries()){
                return new IllegalStateException("Some meta-qery-set queries has not run - " + type);
            }
        }
        return null;
    }

    /**
     * Returns failed queries in this suite.
     *
     * @return failed queries
     */
    public List<Query> getFailedQueries(){
        List<Query> out = new LinkedList<>();
        for(QuerySet qs : querySets){
            out.addAll(qs.getFailedQueries());
        }
        return out;
    }

    /**
     * Returns all executed queries in this suite.
     *
     * @return executed queries
     */
    public List<Query> getExecutedQueries(){
        List<Query> out = new LinkedList<>();
        for(QuerySet qs : querySets){
            out.addAll(qs.getExecutedQueries());
        }
        return out;
    }

    @Override
    public int compareTo(Suite o) {
        return o == null ? 1 : this.id.compareTo(o.id);

    }
}
