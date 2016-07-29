package org.jboss.bqt;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jboss.bqt.Query.QueryResult;
import org.jboss.bqt.exceptions.ExecutionInterruptedException;
import org.jboss.bqt.exceptions.ServerNotAvailableException;
import org.jboss.bqt.exceptions.DbNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which represents a query set. Could contains a single query or set of SQL queries.
 *
 * @author Juraj Duráni
 */
public class QuerySet {

    private static final Logger LOG = LoggerFactory.getLogger(QuerySet.class);

    private final List<Query> queries = new LinkedList<Query>();
    private final List<QueryResult> queryResults = new LinkedList<QueryResult>();
    private final String id;
    private final boolean fastFail;
    private final TestResult result = new QuerySetResult();

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
     * Runs all queries in this set.
     *
     * @return result of the test
     * @throws ServerNotAvailableException if server is not available
     * @throws DbNotAvailableException if VDB is not available
     * @throws ExecutionInterruptedException if thread has been interrupted
     */
    public TestResult runQueries() throws ServerNotAvailableException, DbNotAvailableException, ExecutionInterruptedException{
        queryResults.clear();
        if(queries.size() > 1){
            LOG.info("Starting query set {}.", id);
        }
        try{
            boolean next = true;
            Iterator<Query> iter = queries.iterator();
            while(next && iter.hasNext()){
                Query q = iter.next();
                QueryResult qr = q.run();
                queryResults.add(qr);
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
            return result;
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
    int getNumberOfAllQueries(){
        return queries.size();
    }

    /**
     * Return number of failed queries.
     *
     * @return number of failed queries in this set
     */
    int getNumberOfFailedQueries(){
        int num = 0;
        for(QueryResult qr : queryResults){
            if(!qr.pass()){
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
    int getNumberOfPassedQueries(){
        int num = 0;
        for(QueryResult qr : queryResults){
            if(qr.pass()){
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
    int getNumberOfExecutedQueries(){
        return queryResults.size();
    }

    /**
     * Returns test result.
     *
     * @return test result
     */
    public TestResult getResult() {
        return result;
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
        List<Query> out = new LinkedList<Query>();
        for(QueryResult qr : queryResults){
            if(!qr.pass()){
                out.add(qr.getQuery());
            }
        }
        return out;
    }

    /**
     * Query set test result.
     *
     * @author Juraj Duráni
     */
    private class QuerySetResult implements TestResult{
        @Override
        public void write(Writer wr) throws IOException {
            for(TestResult r : queryResults){
                r.write(wr);
                wr.append(BqtTool.LS);
            }
        }

        @Override
        public void writeHeader(Writer wr) throws IOException{
            // no header
        }
    }
}
