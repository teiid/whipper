package org.whipper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
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
    private static final Pattern PH_PATTERN = Pattern.compile(".*\\$\\{.+\\}.*");

    /**
     * Keys of properties.
     *
     * @author Juraj Duráni
     */
    public static interface Keys{
        public static final String CONNECTION_PROPERTY_PREFIX = "whipper.connection.property.";

        public static final String CONNECTION_STRATEGY = "whipper.connection.strategy";
        public static final String TIME_FOR_ONE_QUERY = "whipper.scenario.time.for.one.query";
        public static final String PING_QUERY = "whipper.scenario.ping.query";
        public static final String URL = "url";
        public static final String DRIVER_CLASS = "jdbc.driver";
        public static final String SCENARIO = "scenario.file";
        public static final String INCLUDE_SCENARIOS = "whipper.scenario.include";
        public static final String EXCLUDE_SCENARIOS = "whipper.scenario.exclude";
        public static final String ARTEFACTS_DIR = "queryset.artifacts.dir";
        public static final String OUTPUT_DIR = "output.dir";
        public static final String RESULT_MODE = "result.mode";
        public static final String ALLOWED_DIVERGENCE = "allowed.divergence";
        public static final String QUERYSET_DIR = "queryset.dirname";
        public static final String EXPECTED_RESULTS_DIR = "expected.results.dirname";
        public static final String TEST_QUERIES_DIR = "test.queries.dirname";
        public static final String VALID_CONNECTION_SQL = "whipper.scenario.valid.connection.sql";
        public static final String AFTER_QUERY = "whipper.scenario.after.query";
        public static final String QUERY_SET_FAST_FAIL = "whipper.scenario.fastfail";
    }

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
            new Whipper().runTest(f, p);
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

    private ResultMode resultMode;

    /**
     * Runs test.
     *
     * @param propsFile file with properties.
     */
    public void runTest(File propsFile){
        runTest(propsFile, null);
    }

    /**
     * Runs test.
     *
     * @param propsFile file with properties
     * @param over {@code Properties} to override properties from {@code propsFile}
     */
    public void runTest(File propsFile, Properties over){
        final Properties props = new Properties();
        if(propsFile == null){
            LOG.info("Properties file is null. Ignoring it.");
        } else if(!propsFile.exists()){
            LOG.warn("Properties file does not exist [{}]. Ignoring it.", propsFile);
        } else if(!propsFile.isFile()){
            LOG.warn("Properties file is not a file [{}]. Ignoring it.", propsFile);
        } else {
            FileReader fr = null;
            try{
                fr = new FileReader(propsFile);
                props.load(fr);
            } catch (IOException ex){
                LOG.error("Cannot load properties from file " + propsFile + ". Ignoring it.", ex);
                props.clear();
            } finally {
                Whipper.close(fr);
            }
        }
        if(over != null && !over.isEmpty()){
            props.putAll(over);
        }
        runTest(props);
    }

    /**
     * Runs test.
     *
     * @param props test properties
     */
    public void runTest(Properties props){
        resultMode = getResultMode(props);
        List<TestResultsWriter> trws = getResultWriters(resolvePlaceHolders(props));
        ScenarioIterator iter = new ScenarioIterator(props);
        try{
            while(iter.hasNext()){
                Scenario scen = iter.next();
                if(scen == null){
                    continue;
                }
                Properties init = iter.getScenarioInitProperties();
                resultMode.resetConfiguration(init);
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
                for(TestResultsWriter trw : trws){
                    trw.writeResultOfScenario(scen);
                }
            }
        } finally {
            resultMode.destroy();
        }
    }

    /**
     * Loads all {@link TestResultsWriter} services from classpath.
     *
     * @param init properties to use to initialize writer
     * @return list of {@link TestResultsWriter}s
     */
    private List<TestResultsWriter> getResultWriters(Properties init){
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
    private ResultMode getResultMode(Properties props){
        String modeName = props.getProperty(Keys.RESULT_MODE);
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

    /**
     * Iterator over scenarios.
     *
     * @author Juraj Duráni
     */
    private class ScenarioIterator implements Iterator<Scenario>{

        private final File[] scenarios;
        private final File artefactsDir;
        private final Properties original = new Properties();
        private final Properties initProps = new Properties();
        private int idx = 0;

        private ScenarioIterator(final Properties props) {
            original.putAll(props);
            String scen = original.getProperty(Keys.SCENARIO);
            File scenFile = new File(scen == null ? "" : scen);
            if(scen == null || !scenFile.exists()){
                scenarios = new File[0];
            } else {
                if(scenFile.isFile()){
                    scenarios = new File[]{scenFile};
                } else {
                    String inclPat = original.getProperty(Keys.INCLUDE_SCENARIOS);
                    String exclPat = original.getProperty(Keys.EXCLUDE_SCENARIOS);
                    final Pattern incl = inclPat == null ? null : Pattern.compile("^(" + inclPat + ")$");
                    final Pattern excl = exclPat == null ? null : Pattern.compile("^(" + exclPat + ")$");
                    scenarios = scenFile.listFiles(new FileFilter() {

                        @Override
                        public boolean accept(File pathname) {
                            String name = pathname.getName().trim();
                            String nameNoExt = removeExtension(name);
                            boolean accept = pathname.isFile() && name.endsWith(".properties");
                            if(accept && incl != null){
                                accept = incl.matcher(nameNoExt).matches();
                            }
                            if(accept && excl != null){
                                accept = !excl.matcher(nameNoExt).matches();
                            }
                            return accept;
                        }
                    });
                }
            }
            String artefactsDirStr = original.getProperty(Keys.ARTEFACTS_DIR);
            if(artefactsDirStr == null){
                LOG.error("Property {} is not set.", Keys.ARTEFACTS_DIR);
                artefactsDir = new File("");
            } else {
                artefactsDir = new File(artefactsDirStr);
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
                Properties props = new Properties();
                props.putAll(original);
                props.load(fr);
                LOG.debug("Properties: {}.", props);
                props = resolvePlaceHolders(props);
                LOG.debug("Resolved properties: {}.", props);
                Scenario scen = new Scenario(removeExtension(scenFile.getName()));
                scen.init(props);
                initProps.clear();
                initProps.putAll(props);
                File testQueries = new File(artefactsDir,
                        props.getProperty(Keys.QUERYSET_DIR)+ File.separator + props.getProperty(Keys.TEST_QUERIES_DIR));
                File[] suites = testQueries.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.trim().endsWith(".xml");
                    }
                });
                if(suites == null){
                    throw new IllegalArgumentException("Cannot load test queries from directory " + testQueries);
                }
                for(File f : suites){
                    Suite suite = new Suite(removeExtension(f.getName()));
                    XmlHelper.loadQueries(f, scen, suite, resultMode);
                    scen.addSuite(suite);
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

        private Properties getScenarioInitProperties(){
            return initProps;
        }
    }

    /**
     * Resolves placeholders in properties.
     * <p>
     * Placeholders are declared as ${...}.
     * If some key is not present in passed properties, placeholder remains unchanged.
     *
     * @param props properties
     * @return resolved properties (new instance of {@link Properties})
     */
    static Properties resolvePlaceHolders(Properties props) {
        Properties resolved = new Properties();
        resolved.putAll(props);

        boolean changed = true;
        while(changed){
            changed = false;
            for(Entry<Object, Object> e : resolved.entrySet()){
                String value = e.getValue().toString();
                if(containsPlaceHolder(value)){
                    Set<String> phs = getPlaceHolders(value);
                    if(phs.contains(e.getKey().toString())){ // contains itself
                        throw new IllegalArgumentException("Recurcive placeholders: " + props);
                    }
                    for(String ph : phs){
                        Object toReplace = resolved.get(ph);
                        if(toReplace != null){
                            String replacement = toReplace.toString();
                            if(LOG.isTraceEnabled()){
                                LOG.trace("Replacing '{}' with '{}'.", ph, replacement);
                            }
                            e.setValue(value.replace("${" + ph + "}", replacement.toString()));
                            changed = true;
                        }
                    }
                }
            }
        }
        return resolved;
    }

    /**
     * Returns all placeholders in string.
     * 
     * @param str input string
     * @return placeholder keys
     */
    static Set<String> getPlaceHolders(String str){
        Set<String> phs = new HashSet<>();
        int start = -2;
        int end;
        boolean next = true;
        while(next){
            start = str.indexOf("${", start + 2);
            end = str.indexOf("}", start + 2);
            if(start == -1 || end == -1){
                next = false;
            } else if((end - start) != 2){
                phs.add(str.substring(start + 2, end));
            }
        }
        return phs;
    }

    /**
     * Returns true if string contains placeholder.
     *
     * @param str input string
     * @return true if {@code str} contains at least one placeholder
     */
    static boolean containsPlaceHolder(String str){
        return PH_PATTERN.matcher(str).matches();
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
