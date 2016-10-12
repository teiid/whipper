package org.whipper;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ServerNotAvailableException;
import org.whipper.resultmode.ResultHolder;
import org.whipper.resultmode.ResultMode;

/**
 * Class which represents single query.
 *
 * @author Juraj Duráni
 */
public class Query implements TimeTracker{

    private static final Logger LOG = LoggerFactory.getLogger(Query.class);
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    private final Scenario scenario;
    private final Suite suite;
    private final QuerySet querySet;
    private final String id;
    private final String sql;
    private final ResultMode resultMode;
    private final ActualResultHolder holder = new ActualResultHolder();
    private QueryResult result;
    private long startTime = -1;
    private long endTime = -1;

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

    public ResultMode getResultMode(){
        return resultMode;
    }

    /**
     * Marks this query as before-set-failed.
     *
     * @param cause cause of the failure
     * @param type type of before (i.e. set / suite)
     */
    public void beforeSetFailed(Throwable cause, String type){
        if(cause != null){
            result = new QueryResult();
            SQLException ex = new SQLException(type + " failed [" + cause.getMessage() + "]", cause);
            result.exception = ex;
            result.pass = false;
            holder.buildResult(ex);
        }
    }

    /**
     * Runs this query.
     */
    public void run(){
        LOG.info("Running query {} - {}", suite.getId(), id);
        Statement ps = null;
        SQLException exception = null;
        boolean valid = true;
        startTime = System.currentTimeMillis();
        try{
            ps = scenario.getConnection().createStatement();
            ps.execute(sql);
        } catch (SQLException ex){
            exception = ex;
        }
        endTime = System.currentTimeMillis();
        result = new QueryResult();
        if(exception != null){
            holder.buildResult(exception);
            valid = scenario.isConnectionValid();
        } else {
            try{
                holder.buildResult(ps);
            } catch (SQLException ex){
                result.exception = new RuntimeException("Unable to build result: " + ex.toString(), ex);
                result.pass = false;
                return;
            } finally {
                Whipper.close(ps);
            }
        }
        if(exception != null){
            if(exception.getSQLState().startsWith("08")){
                result.exception = new ServerNotAvailableException(exception.getMessage(), exception);
                result.pass = false;
                return;
            } else if(!valid){
                result.exception = new DbNotAvailableException(exception.getMessage(), exception);
                result.pass = false;
                return;
            } else {
                result.exception = exception;
            }
        }
        ResultHolder rh = resultMode.handleResult(this);
        result.pass = !rh.isFail();
        if(rh.isException()){
            result.exception = rh.getException();
        } else if(rh.isError()){
            result.errors = rh.getErrors();
        }
    }

    /**
     * Returns actual result of the query.
     *
     * @return actual result
     */
    public ActualResultHolder getActualResult() {
        return holder;
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

    /**
     * Returns {@code true} if query has been executed, {@code false} otherwise.
     *
     * @return execution status
     */
    public boolean isExecuted(){
        return result != null;
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
    public class QueryResult {

        private boolean pass = false;
        private Throwable exception = null;
        private List<String> errors = null;

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

        /**
         * Returns {@code true} if query passed.
         *
         * @return {@code true} if query passed, {@code false} otherwise
         */
        public boolean pass(){
            return pass;
        }

        /**
         * Returns thrown exception.
         *
         * @return thrown exception or {@code null} if no exception has been thrown
         */
        public Throwable getException() {
            return exception;
        }

        /**
         * Return {@code true} if query threw an exception.
         *
         * @return {@code true} if query threw an exception, {@code false} otherwise
         */
        public boolean isException(){
            return exception != null;
        }

        /**
         * Returns {@code true} if query contains errors.
         *
         * @return {@code true} if query contains errors, {@code false} otherwise
         */
        public boolean isError(){
            return errors != null && !errors.isEmpty();
        }

        /**
         * Returns errors.
         *
         * @return errors.
         */
        public List<String> getErrors() {
            return errors;
        }

        /**
         * Returns {@code Query} to which this result belongs to.
         *
         * @return
         */
        public Query getQuery(){
            return Query.this;
        }
    }
}
