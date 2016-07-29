package org.jboss.bqt;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.jboss.bqt.exceptions.ServerNotAvailableException;
import org.jboss.bqt.exceptions.DbNotAvailableException;
import org.jboss.bqt.resultmode.ResultHandler;
import org.jboss.bqt.resultmode.ResultMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which represents single query.
 *
 * @author Juraj Duráni
 */
public class Query {

    private static final Logger LOG = LoggerFactory.getLogger(Query.class);
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    private final Scenario scenario;
    private final Suite suite;
    private final QuerySet querySet;
    private final String id;
    private final String sql;
    private final ResultMode resultMode;
    private final ActualResultHandler handler = new ActualResultHandler();
    private final QueryResult result = new QueryResult();

    /**
     * Create a new instance.
     *
     * @param scenario scenario which this query belongs to
     * @param suite query set which this query belongs to
     * @param querySet query set which this query belongs to
     * @param id if of the query
     * @param sql SQL query
     * @param resultMode result mode
     */
    public Query(Scenario scenario, Suite suite, QuerySet querySet, String id,
            String sql, ResultMode resultMode) {
        this.scenario = scenario;
        this.suite = suite;
        this.querySet = querySet;
        this.id = id;
        this.sql = sql;
        this.resultMode = resultMode;
    }

    /**
     * Runs a query
     *
     * @return result of the query
     */
    public QueryResult run(){
        LOG.info("Running query {} - {}", suite.getId(), id);
        Statement ps = null;
        SQLException exception = null;
        boolean valid = true;
        result.startTime = System.currentTimeMillis();
        try{
            ps = scenario.getConnection().createStatement();
            ps.execute(sql);
        } catch (SQLException ex){
            exception = ex;
        }
        result.endTime = System.currentTimeMillis();
        if(exception != null){
            handler.buildResult(exception);
            valid = scenario.isConnectionValid();
        } else {
            try{
                handler.buildResult(ps);
            } catch (SQLException ex){
                result.exception = new RuntimeException("Unable to build result: " + ex.toString(), ex);
                result.pass = false;
                return result;
            } finally {
                BqtTool.close(ps);
            }
        }
        if(exception != null){
            if(exception.getSQLState().startsWith("08")){
                result.exception = new ServerNotAvailableException(exception);
                result.pass = false;
                return result;
            } else if(!valid){
                result.exception = new DbNotAvailableException(exception);
                result.pass = false;
                return result;
            } else {
                result.exception = exception;
            }
        }
        ResultHandler rh = resultMode.handleResult(this);
        result.pass = !rh.isFail();
        if(rh.isException()){
            result.exception = rh.getException();
        } else if(rh.isError()){
            result.errors = rh.getErrors();
        }
        return result;
    }

    /**
     * Returns actual result of the query.
     *
     * @return actual result
     */
    public ActualResultHandler getActualResult() {
        return handler;
    }

    /**
     * Returns scenario of the query.
     *
     * @return scenario
     */
    public Scenario getScenario() {
        return scenario;
    }

    /**
     * Returns suite of the query.
     *
     * @return suite
     */
    public Suite getSuite() {
        return suite;
    }

    /**
     * Returns query set of the query.
     *
     * @return query set
     */
    public QuerySet getQuerySet() {
        return querySet;
    }

    /**
     * Returns ID of the query.
     *
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns SQL query.
     *
     * @return SQL query.
     */
    public String getSql() {
        return sql;
    }

    /**
     * Returns result of the query.
     *
     * @return result
     */
    public QueryResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return id + " - " + sql;
    }

    /**
     * Class which represents result of the query.
     *
     * @author Juraj Duráni
     */
    class QueryResult implements TestResult {

        private boolean pass = false;
        private long startTime;
        private long endTime;
        private Throwable exception = null;
        private List<String> errors = null;

        @Override
        public void write(Writer wr) throws IOException {
            wr.append(toString());
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append(id)
                    .append(',')
                    .append(pass ? "pass" : "fail")
                    .append(',')
                    .append(TIME_FORMAT.format(new java.util.Date(startTime)))
                    .append(',')
                    .append(TIME_FORMAT.format(new java.util.Date(endTime)))
                    .append(',')
                    .append(Long.toString(endTime - startTime));
            if(isException()){
                sb.append(',').append(exception.toString());
            } else if(isError()){
                sb.append(',').append(errors.get(0));
            }
            return sb.toString();
        }

        @Override
        public void writeHeader(Writer wr) throws IOException{
            // no header
        }

        /**
         * Returns {@code true} if query passed.
         *
         * @return {@code true} if query passed, {@code false} otherwise
         */
        boolean pass(){
            return pass;
        }

        /**
         * Returns thrown exception.
         *
         * @return thrown exception or {@code null} if no exception has been thrown
         */
        Throwable getException() {
            return exception;
        }

        /**
         * Return {@code true} if query threw an exception.
         *
         * @return {@code true} if query threw an exception, {@code false} otherwise
         */
        boolean isException(){
            return exception != null;
        }

        /**
         * Returns {@code true} if query contains errors.
         *
         * @return {@code true} if query contains errors, {@code false} otherwise
         */
        boolean isError(){
            return errors != null && !errors.isEmpty();
        }

        /**
         * Returns errors.
         *
         * @return errors.
         */
        List<String> getErrors() {
            return errors;
        }

        /**
         * Returns {@code Query} to which this result belongs to.
         *
         * @return
         */
        Query getQuery(){
            return Query.this;
        }
    }
}
