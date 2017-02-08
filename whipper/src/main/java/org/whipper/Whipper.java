package org.whipper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whipper.exceptions.WhipperException;
import org.whipper.resultmode.NoneResultMode;
import org.whipper.resultmode.ResultMode;
import org.whipper.results.TestResultsWriter;
import org.whipper.xml.XmlHelper;

/**
 * Main class of Whipper.
 *
 * @author Juraj Duráni
 */
public class Whipper {

    private static final Logger LOG = LoggerFactory.getLogger(Whipper.class);

    /**
     * Main method to allow to run Whipper tool from command line.
     *
     * @param args program arguments (allowed options are -h, --help, -f, --file, -P)
     */
    public static void main(String... args){
        File f = null;
        Properties p = new Properties();
        if(args != null){
            for(int i = 0; i < args.length; i++){
                checkHelp(args[i]);
                if("-f".equals(args[i]) || "--file".equals(args[i])){
                    if(++i < args.length){
                        checkHelp(args[i]);
                        f = new File(args[i]);
                    }
                } else if(args[i] != null && args[i].startsWith("-P")){
                    String prop = args[i].substring(2);
                    int idx = prop.indexOf("=");
                    if(idx > -1){
                        String key = prop.substring(0, idx);
                        String value = prop.substring(Math.min(prop.length(), idx + 1));
                        p.put(key, value);
                    }
                }
            }
        }
        try{
            new Whipper(new WhipperProperties(f, p)).start();
        } catch (Throwable t){
            LOG.warn("Exception thrown: " + t.getMessage(), t);
        }
    }

    /**
     * Prints help.
     *
     * @param arg program argument
     */
    private static void checkHelp(String arg){
        if("-h".equals(arg) || "--help".equals(arg)){
            System.out.println("Options:");
            System.out.println("    -h, --help                  show this help and exit");
            System.out.println("    -f <file>, --file <file>    path to file with default properties");
            System.out.println("    -P<key>=<value>             define property to add or override");
            System.out.println("                                property from properties file");
            System.out.println("                                (e.g -Pmy.prop=value1");
            System.exit(0);
        }
    }

    private final WhipperProperties properties;
    private final List<ProgressMonitor> monitors = new LinkedList<>();
    private ResultMode resultMode;
    private WhipperThread executionThread;
    private WhipperResult result;

    public Whipper(WhipperProperties properties){
        this.properties = properties;
    }

    public void start(){
        start(false);
    }

    public void start(boolean inNewThread){
        executionThread = new WhipperThread();
        if(inNewThread){
            executionThread.start();
        } else {
            executionThread.go();
        }
    }

    public void stop(){
        if(executionThread != null){
            // potentially dangerous - not synchronized
            executionThread.interrupt();
        }
    }

    public WhipperResult getResult(){
        return result;
    }

    public void waitFor() throws InterruptedException{
        if(executionThread != null){
            // potentially dangerous - not synchronized
            executionThread.join();
        }
    }

    public void registerProgressMonitor(ProgressMonitor monitor) throws IllegalStateException{
        if(executionThread != null){
            throw new IllegalStateException("Cannot register monitor. Whipper is already running.");
        }
        if(monitor != null){
            monitors.add(monitor);
        }
    }

    public void unregisterProgressMonitor(ProgressMonitor monitor) throws IllegalStateException{
        if(executionThread != null){
            throw new IllegalStateException("Cannot unregister monitor. Whipper is already running.");
        }
        if(monitor != null){
            monitors.remove(monitor);
        }
    }

    /**
     * Runs test.
     */
    private void runTest(){
        result = null;
        properties.resolvePlaceholders();
        properties.dumpPropertiesToOutputDir();
        resultMode = getResultMode(properties);
        List<TestResultsWriter> trws = getResultWriters(properties);
        ScenarioIterator iter = new ScenarioIterator(properties, resultMode);
        for(ProgressMonitor pm : monitors){
            pm.starting(iter.getScenarioNames());
        }
        WhipperResult tmpRes = new WhipperResult();
        try{
            while(iter.hasNext()){
                Scenario scen = iter.next();
                if(scen == null){
                    continue;
                }
                resultMode.resetConfiguration(iter.initProps);
                if(scen.before()){
                    try{
                        scen.run();
                    } catch (WhipperException ex){
                        LOG.error("Scenario has been interrupted.", ex);
                    } catch (Exception ex){
                        LOG.error("Uknown exception thrown.", ex);
                        throw ex;
                    } finally {
                        scen.after();
                    }
                } else {
                    LOG.warn("Skipping scenario {}.", scen.getId());
                }
                tmpRes.collectStats(scen);
                for(TestResultsWriter trw : trws){
                    trw.writeResultOfScenario(scen);
                }
            }
        } finally {
            result = tmpRes;
            result.dumpToDir(properties.getOutputDir());
            resultMode.destroy();
            for(ProgressMonitor pm : monitors){
                pm.finished(result);
            }
        }
    }

    /**
     * Loads all {@link TestResultsWriter} services from classpath.
     *
     * @param init properties to use to initialize writer
     * @return list of {@link TestResultsWriter}s
     */
    private List<TestResultsWriter> getResultWriters(WhipperProperties init){
        List<TestResultsWriter> out = new ArrayList<>();
        for(Iterator<TestResultsWriter> iter = ServiceLoader.load(TestResultsWriter.class).iterator(); iter.hasNext();){
            TestResultsWriter trw = iter.next();
            if(trw.init(init)){
                out.add(trw);
            }
        }
        return out;
    }

    /**
     * Returns required implementation of {@link ResultMode}.
     *<p>
     * Uses {@link ServiceLoader}
     *
     * @param props test properties
     * @return implementation of {@link ResultMode}
     */
    private ResultMode getResultMode(WhipperProperties props){
        String modeName = props.getResultMode();
        if(modeName == null){
            LOG.warn("No ResultMode set. Setting to 'NONE'.");
            modeName = "NONE";
        }
        for(ResultMode rm : ServiceLoader.load(ResultMode.class)){
            if(modeName.equalsIgnoreCase(rm.getName())){
                return rm;
            }
        }
        LOG.warn("Unknown result mode {}. Result mode set to 'NONE'", modeName);
        return new NoneResultMode();
    }

    private class WhipperThread extends Thread{
        @Override
        public void run(){
            go();
        }

        private void go(){
            try{
                runTest();
            } finally {
                executionThread = null;
            }
        }
    }

    /**
     * Iterator over scenarios.
     *
     * @author Juraj Duráni
     */
    private static class ScenarioIterator implements Iterator<Scenario>{

        private final File[] scenarios;
        private final File artifactsDir;
        private final WhipperProperties original;
        private final WhipperProperties initProps = new WhipperProperties();
        private final ResultMode rm;
        private int idx = 0;

        private ScenarioIterator(final WhipperProperties props, ResultMode rm) {
            this.rm = rm;
            original = props.copy();
            File scen = original.getScenario();
            if(scen == null || !scen.exists()){
                scenarios = new File[0];
            } else if(scen.isFile()){
                scenarios = new File[]{scen};
            } else {
                final Pattern incl = original.getIncludeScenario();
                final Pattern excl = original.getExcludeScenario();
                scenarios = scen.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        String name = pathname.getName().trim();
                        String nameNoExt = removeExtension(name);
                        return pathname.isFile() && name.endsWith(".properties")
                                && incl.matcher(nameNoExt).matches()
                                && !excl.matcher(nameNoExt).matches();
                    }
                });
            }
            File f = original.getArtifacstDir();
            if(f == null){
                LOG.warn("Artifacts directory is not set.");
                artifactsDir = new File("");
            } else {
                artifactsDir = f;
            }
            if(scenarios.length == 0){
                LOG.warn("No scenarios to run [{}].", scen);
            } else {
                Arrays.sort(scenarios, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if(o1 == o2){
                            return 0;
                        }
                        if(o1 == null){
                            return -1;
                        }
                        if(o2 == null){
                            return 1;
                        }
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
        }

        private List<String> getScenarioNames(){
            List<String> out = new ArrayList<>(scenarios.length);
            for(File f : scenarios){
                out.add(removeExtension(f.getName()));
            }
            return out;
        }

        @Override
        public boolean hasNext() {
            return idx < scenarios.length;
        }

        @Override
        public Scenario next() {
            if(!hasNext()){
                throw new NoSuchElementException();
            }
            return createScenario(scenarios[idx++]);
        }

        /**
         * Create a new scenario from scenario.properties file {@code scenFile}
         *
         * @param scenFile scenario file
         * @return new scenario
         */
        private Scenario createScenario(File scenFile){
            try(FileReader fr = new FileReader(scenFile)){
                WhipperProperties props = original.copy();
                Properties scenProps = new Properties();
                scenProps.load(fr);
                props.addAll(scenProps);
                LOG.debug("Properties: {}.", props);
                props.resolvePlaceholders();
                LOG.debug("Resolved properties: {}.", props);
                Scenario scen = new Scenario(removeExtension(scenFile.getName()));
                scen.init(props);
                initProps.copyFrom(props);
                String qsd = props.getQuerySetDir();
                String tqd = props.getTestQueriesDir();
                if(qsd == null){
                    LOG.warn("Query set directory is not defined. Setting to empty string.");
                    qsd = "";
                }
                if(tqd == null){
                    LOG.warn("Test queries directory is not defined. Setting to empty string.");
                    tqd = "";
                }
                File testQueries = new File(artifactsDir, qsd + File.separator + tqd);
                File[] suites = testQueries.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.trim().endsWith(".xml");
                    }
                });
                if(suites == null){
                    throw new IllegalArgumentException("Cannot load test queries from directory " + testQueries);
                }
                Pattern includePattern = props.getIncludeSuite();
                Pattern excludePattern = props.getExcludeSuite();
                LOG.debug("suite include pattern: {}", includePattern);
                LOG.debug("suite exclude pattern: {}", excludePattern);
                for(File f : suites){
                    String suiteName = removeExtension(f.getName());
                    if(includePattern.matcher(suiteName).matches() && ! excludePattern.matcher(suiteName).matches()){
                        Suite suite = new Suite(suiteName);
                        XmlHelper.loadQueries(f, scen, suite, rm);
                        scen.addSuite(suite);
                    }else{
                        LOG.info("Skipping suite {}", suiteName);
                    }
                }
                return scen;
            } catch (Exception ex){
                LOG.error("Unable to create scenario from file " + scenFile, ex);
                initProps.clear();
                return null;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    /**
     * Removes extension of file (i.e. *.txt, *.properties, ...).
     *
     * @param fileName name of the file
     * @return name without extension
     */
    static String removeExtension(String fileName){
        int extIdx = fileName.lastIndexOf(".");
        if(extIdx <= 0){ // ignore hidden files without extension
            return fileName;
        } else {
            return fileName.substring(0, extIdx);
        }
    }

    /**
     * Safely closes all passed {@link AutoCloseable}.
     *
     * @param cls list of object which should be closed.
     */
    public static void close(AutoCloseable... cls) {
        if(cls != null){
            for(AutoCloseable cl : cls){
                if(cl != null){
                    try{
                        cl.close();
                    } catch(Exception ex){
                        LOG.warn("Error closing AutoCloseable " + cl, ex);
                    }
                }
            }
        }
    }
}
