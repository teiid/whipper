package org.whipper;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.Query.QueryResult;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ExecutionInterruptedException;
import org.whipper.exceptions.ServerNotAvailableException;
import org.whipper.resultmode.MetaQuerySetResultMode;

/**
 * Class which represents a query set. Could contains a single query or set of SQL queries.
 *
 * @author Juraj Duráni
 */
public class QuerySet implements TimeTracker{

    private static final Logger LOG = LoggerFactory.getLogger(QuerySet.class);

    private final List<Query> queries = new LinkedList<>();
    private final String id;
    private final boolean fastFail;
    private final MetaQuerySetResultMode metaQuerySetResultMode;
    private final List<ProgressMonitor> monitors = new LinkedList<>();
    private long startTime = -1;
    private long endTime = -1;
    private QuerySet before;
    private QuerySet after;
    private String mainId;

    /**
     * Creates a new instance.
     *
     * @param id ID of the query set
     * @param fastFail {@code true} if query set should fail after first failed query
     * @param metaQuerySetResultMode result mode to handle failed before-set queries.
     *      If this parameter is {@code null} this query set is considered to be a
     *      meta-query-set.
     */
    public QuerySet(String id, boolean fastFail, MetaQuerySetResultMode metaQuerySetResultMode) {
        this.id = id;
        this.fastFail = fastFail;
        this.metaQuerySetResultMode = metaQuerySetResultMode;
    }

    /**
     * Sets progress monitors for this query set and all queries of this set.
     * Including before and after.
     *
     * @param monitors monitors to be set.
     */
    public void setProgressMonitors(List<ProgressMonitor> monitors){
        this.monitors.clear();
        if(monitors != null){
            this.monitors.addAll(monitors);
        }
        for(Query q : queries){
            q.setProgressMonitors(monitors);
        }
        if(after != null){ after.setProgressMonitors(monitors); }
        if(before != null){ before.setProgressMonitors(monitors); }
    }


    /**
     * Sets query set which will be run after this query set.
     *
     * @param after query set to be run after this query set
     */
    public void setAfter(QuerySet after){
        this.after = after;
    }

    /**
     * Sets query set which will be run before this query set.
     *
     * @param before query set to be run before this query set
     */
    public void setBefore(QuerySet before){
        this.before = before;
    }

    /**
     * Returns before-query-set.
     *
     * @return before-query-set
     */
    public QuerySet getBefore(){
        return before;
    }

    /**
     * Returns after-query-set.
     *
     * @return after-query-set
     */
    public QuerySet getAfter(){
        return after;
    }

    /**
     * Marks all queries in this set as failed because
     * before-set failed.
     *
     * @param cause cause of the failure
     * @param type type of before (i.e. set / suite)
     * @throws IllegalStateException if this is meta-query-set
     */
    public void beforeFailed(Throwable cause, String type) throws IllegalStateException{
        if(isMeta()){
            throw new IllegalStateException("Meta-query-set cannot contain before-set.");
        }
        runBeforeMonitors();
        try{
            for(Query q : queries){
                q.beforeSetFailed(cause, type);
            }
            metaQuerySetResultMode.writeErrorsForMainQuerySet(this);
        } finally {
            runAfterMonitors();
        }
    }

    /**
     * Sets ID of main query set for meta-query-set.
     *
     * @param mainId ID of main query set
     * @throws IllegalStateException if this is not a meta-query-set
     */
    public void setMainId(String mainId) throws IllegalStateException{
        if(!isMeta()){
            throw new IllegalStateException("This is not a meta-query-set.");
        }
        this.mainId = mainId;
    }

    /**
     * Returns ID of main query set.
     *
     * @return ID of main query set
     * @throws IllegalStateException if this is not a meta-query-set
     */
    public String getMainId() throws IllegalStateException{
        if(!isMeta()){
            throw new IllegalStateException("This is not a meta-query-set.");
        }
        return mainId;
    }

    /**
     * Determines whether this is a meta-query-set.
     *
     * @return {@code true} id this is a meta-query-set, {@code false} otherwise
     */
    boolean isMeta(){
        return metaQuerySetResultMode == null;
    }

    /**
     * Adds new query to query set.
     *
     * @param q query to be added
     */
    public void addQuery(Query q){
        if(q == null){
            throw new IllegalArgumentException("Query cannot be null.");
        }
        queries.add(q);
    }

    /**
     * Returns unmodifiable list of queries.
     *
     * @return list of queries.
     */
    public List<Query> getQueries(){
        return Collections.unmodifiableList(queries);
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
        return (startTime < 0 || endTime < 0) ? -1L : endTime - startTime;
    }

    /**
     * Runs all queries in this set.
     *
     * @throws ServerNotAvailableException if server is not available
     * @throws DbNotAvailableException if VDB is not available
     * @throws ExecutionInterruptedException if thread has been interrupted
     */
    public void runQueries() throws ServerNotAvailableException, DbNotAvailableException, ExecutionInterruptedException{
        runBeforeMonitors();
        if(queries.size() > 1){
            LOG.info("Starting query set {}.", id);
        }
        try{
            boolean next = true;
            Iterator<Query> iter = queries.iterator();
            while(next && iter.hasNext()){
                Query q = iter.next();
                q.run();
                QueryResult qr = q.getResult();
                if(!qr.pass()){
                    LOG.warn("Query {} failed [suite: {}, scenario {}].", q.getId(), q.getSuite().getId(), q.getScenario().getId());
                    next = !fastFail;
                }
                if(qr.isException()){
                    if(qr.getException() instanceof DbNotAvailableException){
                        throw (DbNotAvailableException)qr.getException();
                    } else if(qr.getException() instanceof ServerNotAvailableException){
                        throw (ServerNotAvailableException)qr.getException();
                    }
                }
                if(Thread.currentThread().isInterrupted()){
                    throw new ExecutionInterruptedException("Execution has been interrupted.");
                }
            }
            if(!next){
                LOG.info("Query failed. Skipping next queries in set.");
            }
        } finally {
            if(queries.size() > 1){
                LOG.info("Query set {} finished.", id);
            }
            runAfterMonitors();
        }
    }

    /**
     * Runs starting method of all monitors.
     */
    private void runAfterMonitors(){
        for(ProgressMonitor pm : monitors){
            if(isMeta()){
                pm.metaQuerySetFinished(this);
            } else {
                pm.querySetFinished(this);
            }
        }
    }

    /**
     * Runs ending method of all monitors.
     */
    private void runBeforeMonitors(){
        for(ProgressMonitor pm : monitors){
            if(isMeta()){
                pm.startingMetaQuerySet(this);
            } else {
                pm.startingQuerySet(this);
            }
        }
    }

    /**
     * Returns number of all queries.
     *
     * @return number of queries in this set
     */
    public int getNumberOfAllQueries(){
        return queries.size();
    }

    /**
     * Return number of failed queries.
     *
     * @return number of failed queries in this set
     */
    public int getNumberOfFailedQueries(){
        int num = 0;
        for(Query qr : queries){
            if(qr.isExecuted() && !qr.getResult().pass()){
                num++;
            }
        }
        return num;
    }

    /**
     * Returns number of passed queries.
     *
     * @return number of passed queries in this set
     */
    public int getNumberOfPassedQueries(){
        int num = 0;
        for(Query qr : queries){
            if(qr.isExecuted() && qr.getResult().pass()){
                num++;
            }
        }
        return num;
    }

    /**
     * Returns number of executed queries.
     *
     * @return number of executed queries in this set
     */
    public int getNumberOfExecutedQueries(){
        int num = 0;
        for(Query qr : queries){
            if(qr.isExecuted()){
                num++;
            }
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
     * Returns failed queries.
     *
     * @return failed queries in this set.
     */
    public List<Query> getFailedQueries(){
        List<Query> out = new LinkedList<>();
        for(Query qr : queries){
            if(qr.isExecuted() && !qr.getResult().pass()){
                out.add(qr);
            }
        }
        return out;
    }

    /**
     * Returns all executed queries;
     *
     * @return executed queries.
     */
    public Collection<? extends Query> getExecutedQueries(){
        List<Query> out = new LinkedList<>();
        for(Query q : queries){
            if(q.isExecuted()){
                out.add(q);
            }
        }
        return out;
    }
}
