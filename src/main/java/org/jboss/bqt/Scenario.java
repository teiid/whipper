package org.jboss.bqt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;

import org.jboss.bqt.BqtTool.Keys;
import org.jboss.bqt.Query.QueryResult;
import org.jboss.bqt.connection.ConnectionFactory;
import org.jboss.bqt.exceptions.ExecutionInterruptedException;
import org.jboss.bqt.exceptions.MaxTimeExceededException;
import org.jboss.bqt.exceptions.ServerNotAvailableException;
import org.jboss.bqt.exceptions.DbNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which represents scenario. Scenario consists of one or more suites.
 *
 * @author Juraj Duráni
 */
public class Scenario {

    private static final Logger LOG = LoggerFactory.getLogger(Scenario.class);
    public static final DateFormat TS_FORMAT = new SimpleDateFormat("_yyyyMMdd_HHmmss");

    private final String id;
    private final List<Suite> suites = new LinkedList<Suite>();
    private final List<TestResult> suiteResults = new LinkedList<TestResult>();
    private final TestResult result = new ScenarioResult();

    private long startTime;
    private long endTime;

    private Connection connection;
    private ConnectionFactory connectionFactory;

    private String pingQuery;
    private String afterQuery;
    private long timeForOneQuery;
    private boolean fastFail;

    private File expectedResultsDir;
    private String expectedResultsDirName;
    private String querysetDirName;
    private File outputDir;

    /**
     * Creates a new scenario.
     *
     * @param id ID of the scenario
     */
    public Scenario(String id) {
        this.id = id;
    }

    /**
     * Initializes scenario from passed properties.
     *
     * @param props test properties
     */
    public void init(Properties props){
        pingQuery = props.getProperty(Keys.PING_QUERY);
        afterQuery = props.getProperty(Keys.AFTER_QUERY);
        expectedResultsDirName = props.getProperty(Keys.EXPECTED_RESULTS_DIR);
        querysetDirName = props.getProperty(Keys.QUERYSET_DIR);
        expectedResultsDir = new File(props.getProperty(Keys.ARTEFACTS_DIR),
                querysetDirName + File.separator + expectedResultsDirName);
        if(!expectedResultsDir.exists() || !expectedResultsDir.isDirectory()){
            throw new IllegalArgumentException("Expected results directory " + expectedResultsDir +
                    " either does not exist or is not a directory.");
        }

        outputDir = new File(props.getProperty(Keys.OUTPUT_DIR), id);
        if(!outputDir.exists() && !outputDir.mkdirs()){
            throw new RuntimeException("Cannot create output directory " + outputDir.getAbsolutePath());
        } else if (outputDir.exists() && outputDir.isFile()){
            throw new RuntimeException("Cannot create output directory. " + outputDir.getAbsolutePath() + " is file.");
        }

        String conFacName = props.getProperty(Keys.CONNECTION_STRATEGY);
        if(conFacName == null){
            LOG.warn("Connection strategy no set. Setting to driver.");
            conFacName = "DRIVER";
        }
        for(ConnectionFactory fac : ServiceLoader.load(ConnectionFactory.class)){
            if(conFacName.equalsIgnoreCase(fac.getName())){
                connectionFactory = fac;
                break;
            }
        }
        if(connectionFactory == null){
            throw new IllegalArgumentException("Unknown connection sgtrategy " + conFacName);
        }

        connectionFactory.init(props);
        String timeStr = props.getProperty(Keys.TIME_FOR_ONE_QUERY);
        if(timeStr == null){
            LOG.warn("Time for one query is not set.");
            timeForOneQuery = -1l;
        } else {
            try{
                timeForOneQuery = Long.parseLong(timeStr);
            } catch (NumberFormatException ex){
                LOG.warn("Time for one query is not a number.", ex);
                timeForOneQuery = -1;
            }
        }
        String ffStr = props.getProperty(Keys.QUERY_SET_FAST_FAIL, "").trim(); // avoid NPE
        fastFail =  "false".equalsIgnoreCase(ffStr) ? false : true; // default value is true
    }

    /**
     * Returns {@code true} if query sets should be set to fast-fail.
     *
     * @return fast-fail
     */
    public boolean isFastFail() {
        return fastFail;
    }

    /**
     * Returns file with expected results.
     *
     * @return file with expected results
     */
    public File getExpectedResultsDir() {
        return expectedResultsDir;
    }

    /**
     * Adds new suite.
     *
     * @param s suite to be added
     */
    public void addSuite(Suite s){
        if(s == null){
            throw new IllegalArgumentException("Suite cannot be null.");
        }
        suites.add(s);
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
     * Returns name of the directory with expected results.
     *
     * @return expected results directory name
     */
    public String getExpectedResultsDirName() {
        return expectedResultsDirName;
    }

    /**
     * Returns name of the directory with with query set (expected results and test queries).
     *
     * @return query set directory name
     */
    public String getQuerysetDirName() {
        return querysetDirName;
    }

    /**
     * Returns connection to database.
     *
     * @return connection
     */
    Connection getConnection() {
        return connection;
    }

    /**
     * Returns number of all queries in this scenario.
     *
     * @return number of all queries.
     */
    public int getNumberOfAllQueries(){
        int num = 0;
        for(Suite s : suites){
            num += s.getNumberOfAllQueries();
        }
        return num;
    }

    /**
     * Returns number of passed queries in this scenario.
     *
     * @return number of passed queries
     */
    public int getNumberOfPassedQueries(){
        int num = 0;
        for(Suite s : suites){
            num += s.getNumberOfPassedQueries();
        }
        return num;
    }

    /**
     * Returns number of failed queries in this scenario.
     *
     * @return number of failed queries
     */
    public int getNumberOfFailedQueries(){
        int num = 0;
        for(Suite s : suites){
            num += s.getNumberOfFailedQueries();
        }
        return num;
    }

    /**
     * Returns failed queries in this scenario.
     *
     * @return failed queries
     */
    private List<Query> getFailedQueries(){
        List<Query> out = new LinkedList<Query>();
        for(Suite s : suites){
            out.addAll(s.getFailedQueries());
        }
        return out;
    }

    /**
     * Validates connection.
     *
     * @return {@code true} if connection provided by this scenario is valid, {@code false} otherwise
     * @see #getConnection()
     */
    boolean isConnectionValid(){
        return connectionFactory.isConnectionValid(connection);
    }

    /**
     * This method should be executed before scenario starts.
     *
     * @return {@code true} if method finished properly.
     */
    public boolean before(){
        try{
            connection = connectionFactory.getConnection();
            if(pingQuery == null){
                LOG.warn("Ping query is not set.");
            } else {
                try(Statement s = connection.createStatement()){
                    s.execute(pingQuery);
                }
            }
            return true;
        } catch (Exception ex){
            LOG.error("Scenario setup failed: " + ex.getMessage(), ex);
            writeExceptionToFile("Summary_scenario_setup_fail.txt", ex);
            BqtTool.close(connection);
            return false;
        }
    }

    /**
     * Runs test.
     *
     * @return result of the test
     * @throws ServerNotAvailableException is server is not available
     * @throws DbNotAvailableException if VDB is not available
     * @throws MaxTimeExceededException if maximum time for scenario has been reached
     * @throws ExecutionInterruptedException if thread has been interrupted
     */
    public TestResult run() throws ServerNotAvailableException, DbNotAvailableException,
                MaxTimeExceededException, ExecutionInterruptedException{
        LOG.info("Starting scenario {}.", id);
        suiteResults.clear();
        long maxEndTime = timeForOneQuery * getNumberOfAllQueries();
        maxEndTime = maxEndTime < 0 ? -1l : maxEndTime + System.currentTimeMillis();
        startTime = System.currentTimeMillis();
        try{
            Collections.sort(suites);
            for(Suite s : suites){
                try{
                    s.run(maxEndTime);
                } finally{
                    suiteResults.add(s.getResult());
                    FileWriter fw1 = null;
                    FileWriter fw2 = null;
                    try{
                        TestResult tr = s.getResult();
                        File out1 = new File(outputDir, s.getId() + ".txt");
                        if(!out1.exists()){
                            out1.createNewFile();
                        }
                        fw1 = new FileWriter(out1, true);
                        tr.writeHeader(fw1);
                        File out2 = new File(outputDir, s.getId() + TS_FORMAT.format(new java.util.Date(System.currentTimeMillis()))  + ".txt");
                        out2.createNewFile();
                        fw2 = new FileWriter(out2, false);
                        tr.writeHeader(fw2);
                        tr.write(fw1);
                        tr.write(fw2);
                        BqtTool.appendLine(fw1, BqtTool.LS); // two lines
                    } catch (IOException ex) {
                        LOG.error("Unable to write result of suite " + s.getId(), ex);
                    } finally {
                        BqtTool.close(fw1, fw2);
                    }
                }
            }
            return result;
        } finally {
            endTime = System.currentTimeMillis();
            LOG.info("Scenario {} finished.", id);
        }
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
     * Method which cleans up scenario execution.
     */
    public void after(){
        if(afterQuery != null){
            try(Statement s = connection.createStatement()){
                s.execute(afterQuery);
            } catch (SQLException ex){
                LOG.error("Scenario teardown failed: " + ex.getMessage(), ex);
                writeExceptionToFile("Summary_scenario_teardown_fail.txt", ex);
            }
        }
        BqtTool.close(connection);
        connection = null;
    }

    /**
     * Writes exception to file.
     *
     * @param fileName name of the file
     * @param sqlEx exception to be written
     */
    private void writeExceptionToFile(String fileName, Exception sqlEx) {
        File file = new File(outputDir.getParentFile(), fileName); // write to bqt-output directory
        FileWriter fw = null;
        try{
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else if(file.isDirectory()){
                LOG.error("Cannot create file {}. File already exists and is directory.", file);
            }
            fw = new FileWriter(file, true);
            BqtTool.appendLine(fw, "------------------------------");
            BqtTool.appendLine(fw, id + " - " + BqtTool.DATE_FORMAT.format(new java.util.Date(System.currentTimeMillis())));
            sqlEx.printStackTrace(new PrintWriter(fw));
        } catch (IOException ex){
            LOG.error("Cannot write exception to file {}.", file);
        } finally {
            BqtTool.close(fw);
        }
    }

    /**
     * Result of the scenario
     *
     * @author Juraj Duráni
     */
    private class ScenarioResult implements TestResult{
        private final int namePad = 50;
        private final int resultsPad = 6;

        @Override
        public void writeHeader(Writer wr) throws IOException {
            BqtTool.appendLine(wr, "Scenario - " + id);
            BqtTool.appendLine(wr, "======================");
            BqtTool.appendLine(wr, "Start Time:           " + BqtTool.DATE_FORMAT.format(new java.util.Date(startTime)));
            BqtTool.appendLine(wr, "End Time:             " + BqtTool.DATE_FORMAT.format(new java.util.Date(endTime)));
            BqtTool.appendLine(wr, "Elapsed:              " + BqtTool.timeToString(endTime - startTime));
            BqtTool.appendLine(wr, "----------------------");
            BqtTool.appendLine(wr, "Number of all suites: " + suites.size());
            BqtTool.appendLine(wr, BqtTool.pad("Name", namePad)
                    + BqtTool.pad("Pass", resultsPad) + BqtTool.pad("Fail", resultsPad) + BqtTool.pad("Total", resultsPad));
            int overallAll = 0;
            int overallPass = 0;
            int overallFail = 0;
            for(Suite s : suites){
                int all = s.getNumberOfAllQueries();
                int pass = s.getNumberOfPassedQueries();
                int fail = s.getNumberOfFailedQueries();
                BqtTool.appendLine(wr, BqtTool.pad(s.getId(), namePad) + BqtTool.pad(Integer.toString(pass), resultsPad)
                        + BqtTool.pad(Integer.toString(fail), resultsPad)
                        + BqtTool.pad(Integer.toString(all), resultsPad));
                overallAll += all;
                overallPass += pass;
                overallFail += fail;
            }
            BqtTool.appendLine(wr, "----------------------");
            BqtTool.appendLine(wr, BqtTool.pad("Totals", namePad) + BqtTool.pad(Integer.toString(overallPass), resultsPad)
                    + BqtTool.pad(Integer.toString(overallFail), resultsPad)
                    + BqtTool.pad(Integer.toString(overallAll), resultsPad));
        }

        @Override
        public void write(Writer wr) throws IOException {
            List<Query> failedQueries = getFailedQueries();
            LOG.debug("Failed queries [{}]: {}", failedQueries.size(), failedQueries);
            if(failedQueries == null || failedQueries.isEmpty()){
                return;
            }
            wr.append(BqtTool.LS);
            BqtTool.appendLine(wr, "----------------------");
            BqtTool.appendLine(wr, "Failed queries [" + id + "]");
            for(Query q : failedQueries){
                wr.append("    ")
                    .append(q.getSuite().getId())
                    .append('_')
                    .append(q.getId())
                    .append(" - ");
                QueryResult qr = q.getResult();
                if(qr.isError()){
                    BqtTool.appendLine(wr, qr.getErrors().get(0));
                } else {
                    BqtTool.appendLine(wr, qr.getException().toString());
                }
            }
        }
    }
}
