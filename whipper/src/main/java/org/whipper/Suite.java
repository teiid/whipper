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

    private static final Logger LOG = LoggerFactory.getLogger(Suite.class);

    private final List<QuerySet> querySets = new LinkedList<>();
    private final String id;
    private long startTime = -1;
    private long endTime = -1;

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
            for(QuerySet qs : querySets){
                // TODO add support for meta-queries
                qs.runQueries();
                if(maxEndTime >= 0 && System.currentTimeMillis() >= maxEndTime){
                    throw new MaxTimeExceededException("Max time exceeded.");
                }
            }
        } finally {
            endTime = System.currentTimeMillis();
            LOG.info("Suite {} finished.", id);
        }
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
