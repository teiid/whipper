package org.whipper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.connection.ConnectionFactory;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ExecutionInterruptedException;
import org.whipper.exceptions.MaxTimeExceededException;
import org.whipper.exceptions.ServerNotAvailableException;
import org.whipper.resultmode.MetaQuerySetResultMode;

/**
 * Class which represents scenario. Scenario consists of one or more suites.
 *
 * @author Juraj Duráni
 */
public class Scenario implements TimeTracker{

    private static final Logger LOG = LoggerFactory.getLogger(Scenario.class);

    private final String id;
    private final List<Suite> suites = new LinkedList<>();
    private final List<ProgressMonitor> monitors = new LinkedList<>();
    private final WhipperProperties initialProperties = new WhipperProperties();

    private long startTime = -1;
    private long endTime = -1;

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

    private MetaQuerySetResultMode metaQuerySetResultMode;

    /**
     * Creates a new scenario.
     *
     * @param id ID of the scenario
     */
    public Scenario(String id){
        this.id = id;
    }

    /**
     * Sets progress monitors for this scenario and all suites.
     *
     * @param monitors monitors to be set
     */
    public void setProgressMonitors(List<ProgressMonitor> monitors){
        this.monitors.clear();
        if(monitors != null){
            this.monitors.addAll(monitors);
        }
        for(Suite s : suites){
            s.setProgressMonitors(monitors);
        }
    }

    /**
     * Initializes scenario from passed properties.
     *
     * @param props test properties
     */
    public void init(WhipperProperties props){
        initialProperties.copyFrom(props);
        pingQuery = initialProperties.getPingQuery();
        afterQuery = initialProperties.getAfterQuery();
        expectedResultsDirName = initialProperties.getExpectedResultsDir();
        querysetDirName = initialProperties.getQuerySetDir();
        expectedResultsDir = new File(initialProperties.getArtifactsDir(),
                querysetDirName + File.separator + expectedResultsDirName);
        if(!expectedResultsDir.exists() || !expectedResultsDir.isDirectory()){
            throw new IllegalArgumentException("Expected results directory " + expectedResultsDir +
                    " either does not exist or is not a directory.");
        }

        outputDir = new File(initialProperties.getOutputDir(), id);
        if(!outputDir.exists() && !outputDir.mkdirs()){
            throw new RuntimeException("Cannot create output directory " + outputDir.getAbsolutePath());
        } else if (outputDir.exists() && outputDir.isFile()){
            throw new RuntimeException("Cannot create output directory. " + outputDir.getAbsolutePath() + " is file.");
        }

        String conFacName = initialProperties.getConnectionStrategy();
        if(conFacName == null){
            LOG.warn("Connection strategy not set. Setting to driver.");
            conFacName = "DRIVER";
        }
        for(ConnectionFactory fac : ServiceLoader.load(ConnectionFactory.class)){
            if(conFacName.equalsIgnoreCase(fac.getName())){
                connectionFactory = fac;
                break;
            }
        }
        if(connectionFactory == null){
            throw new IllegalArgumentException("Unknown connection strategy " + conFacName);
        }

        connectionFactory.init(initialProperties);
        timeForOneQuery = initialProperties.getTimeForOneQuery();
        if(timeForOneQuery == -1L){
            LOG.warn("Time for one query is set to -1.");
        }
        fastFail =  initialProperties.getQuerySetFastFail();
        metaQuerySetResultMode = new MetaQuerySetResultMode(id);
        metaQuerySetResultMode.resetConfiguration(initialProperties);
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
     * Returns initial properties of this scenario which this scenario
     * has been initialized with.
     *
     * @return initial properties
     */
    public WhipperProperties getInitialProperties(){
        return initialProperties;
    }

    /**
     * Returns meta-query set result mode of this scenario.
     *
     * @return meta-query set result mode
     */
    public MetaQuerySetResultMode getMetaQuerySetResultMode(){
        return metaQuerySetResultMode;
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
     * Returns unmodifiable list of suites
     *
     * @return list of suites
     */
    public List<Suite> getSuites(){
        return Collections.unmodifiableList(suites);
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
     * Returns name of the directory with query set (expected results and test queries).
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
    public List<Query> getFailedQueries(){
        List<Query> out = new LinkedList<>();
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
        runMonitorsBefore();
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
            Whipper.close(connection);
            runMonitorsAfter();
            return false;
        }
    }

    /**
     * Runs all suites in this scenario.
     *
     * @throws ServerNotAvailableException is server is not available
     * @throws DbNotAvailableException if VDB is not available
     * @throws MaxTimeExceededException if maximum time for scenario has been reached
     * @throws ExecutionInterruptedException if thread has been interrupted
     */
    public void run() throws ServerNotAvailableException, DbNotAvailableException,
                MaxTimeExceededException, ExecutionInterruptedException{
        LOG.info("Starting scenario {}.", id);
        long maxEndTime = timeForOneQuery < 0 ? -1L : timeForOneQuery * getNumberOfAllQueries() + System.currentTimeMillis();
        startTime = System.currentTimeMillis();
        try{
            Collections.sort(suites);
            for(Suite s : suites){
                s.run(maxEndTime);
            }
        } finally {
            endTime = System.currentTimeMillis();
            LOG.info("Scenario {} finished.", id);
        }
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
        Whipper.close(connection);
        connection = null;
        runMonitorsAfter();
    }

    /**
     * Runs starting method of all monitors.
     */
    private void runMonitorsBefore(){
        for(ProgressMonitor pm : monitors){
            pm.startingScenario(this);
        }
    }

    /**
     * Runs ending method of all monitors.
     */
    private void runMonitorsAfter(){
        for(ProgressMonitor pm : monitors){
            pm.scenarioFinished(this);
        }
    }

    /**
     * Writes exception to file.
     *
     * @param fileName name of the file
     * @param sqlEx exception to be written
     */
    private void writeExceptionToFile(String fileName, Exception sqlEx) {
        File file = new File(outputDir.getParentFile(), fileName); // write to output directory
        FileWriter fw = null;
        try{
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else if(file.isDirectory()){
                LOG.error("Cannot create file {}. File already exists and is directory.", file);
            }
            fw = new FileWriter(file, true);
            fw.append("------------------------------");
            fw.append(System.lineSeparator());
            fw.append(id).append(" - ").append(DateFormat.getDateTimeInstance().format(new java.util.Date(System.currentTimeMillis())));
            fw.append(System.lineSeparator());
            sqlEx.printStackTrace(new PrintWriter(fw));
        } catch (IOException ex){
            LOG.error("Cannot write exception to file {}.", file);
        } finally {
            Whipper.close(fw);
        }
    }
}
