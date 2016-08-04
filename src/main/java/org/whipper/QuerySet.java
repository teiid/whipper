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
    private long startTime = -1;
    private long endTime = -1;

    /**
     * Creates a new instance.
     *
     * @param id ID of the query set
     * @param fastFail {@code true} if query set should fail after first failed query
     */
    public QuerySet(String id, boolean fastFail) {
        this.id = id;
        this.fastFail = fastFail;
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
        return (startTime < 0 || endTime < 0) ? -1l : endTime - startTime;
    }

    /**
     * Runs all queries in this set.
     *
     * @throws ServerNotAvailableException if server is not available
     * @throws DbNotAvailableException if VDB is not available
     * @throws ExecutionInterruptedException if thread has been interrupted
     */
    public void runQueries() throws ServerNotAvailableException, DbNotAvailableException, ExecutionInterruptedException{
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
