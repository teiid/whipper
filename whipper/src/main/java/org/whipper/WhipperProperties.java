package org.whipper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties holder for Whipper test.
 */
public class WhipperProperties implements Cloneable{

    private static final Logger LOG = LoggerFactory.getLogger(WhipperProperties.class);
    private static final Pattern PH_PATTERN = Pattern.compile(".*\\$\\{.+\\}.*");
    private static final String DUMP_PROPS_FILE = "initial.properties";
    static final Pattern NOTHING_PATTERN = Pattern.compile("a^");
    static final Pattern ALL_PATTERN = Pattern.compile("^(.*)$");

    /**
     * Keys.
     */
    public interface Keys{
        String CONNECTION_PROPERTY_PREFIX = "whipper.connection.property.";
        String CONNECTION_STRATEGY = "whipper.connection.strategy";
        String TIME_FOR_ONE_QUERY = "whipper.scenario.time.for.one.query";
        String PING_QUERY = "whipper.scenario.ping.query";
        String URL = "url";
        String DRIVER_CLASS = "jdbc.driver";
        String SCENARIO = "scenario.file";
        String INCLUDE_SCENARIOS = "whipper.scenario.include";
        String EXCLUDE_SCENARIOS = "whipper.scenario.exclude";
        String INCLUDE_SUITES = "whipper.suite.include";
        String EXCLUDE_SUITES = "whipper.suite.exclude";
        String ARTIFACTS_DIR = "queryset.artifacts.dir";
        String OUTPUT_DIR = "output.dir";
        String RESULT_MODE = "result.mode";
        String ALLOWED_DIVERGENCE = "allowed.divergence";
        String QUERYSET_DIR = "queryset.dirname";
        String EXPECTED_RESULTS_DIR = "expected.results.dirname";
        String TEST_QUERIES_DIR = "test.queries.dirname";
        String VALID_CONNECTION_SQL = "whipper.scenario.valid.connection.sql";
        String AFTER_QUERY = "whipper.scenario.after.query";
        String QUERY_SET_FAST_FAIL = "whipper.scenario.fastfail";
    }

    private final Properties props;

    /**
     * Creates new empty properties.
     */
    public WhipperProperties(){
        this(null, null);
    }

    /**
     * Creates new properties. Initial content is loaded from file.
     *
     * @param propsFile file with initial properties
     */
    public WhipperProperties(File propsFile){
        this(propsFile, null);
    }

    /**
     * Creates new properties. Initial content is loaded from file.
     * And overridden with properties {@code over}.
     *
     * @param propsFile file with initial properties
     * @param over overrides for properties loaded from file
     */
    public WhipperProperties(File propsFile, Properties over){
        props = new Properties();
        if(propsFile == null){
            LOG.info("Properties file is null. Ignoring it.");
        } else if(!propsFile.exists()){
            LOG.warn("Properties file does not exist [{}]. Ignoring it.", propsFile);
        } else if(!propsFile.isFile()){
            LOG.warn("Properties file is not a file [{}]. Ignoring it.", propsFile);
        } else {
            try(FileReader fr = new FileReader(propsFile)){
                props.load(fr);
            } catch (IOException ex){
                LOG.error("Cannot load properties from file " + propsFile + ". Ignoring it.", ex);
                props.clear();
            }
        }
        if(over != null && !over.isEmpty()){
            props.putAll(over);
        }
    }

    /**
     * Creates new properties. Initial content is loaded from properties.
     *
     * @param props initial content
     */
    public WhipperProperties(Properties props){
        this.props = props == null ? new Properties() : props;
    }

    /**
     * Returns this properties as a JSON object.
     * @return JSON representation of this object
     */
    public JSONObject asJson(){
        return new JSONObject(props);
    }

    /**
     * Return property value.
     *
     * @param key property key
     * @return value or {@code null} if property does not exist or key is {@code null}
     */
    public String getProperty(String key){
        return key == null ? null : props.getProperty(key);
    }

    /**
     * Returns property as an object of specified class.
     *
     * @param key property key
     * @param as required class
     * @param <T> class
     * @return property as a specified class or {@code null} if property does not
     *      exist or type is not supported
     * @see #getProperty(String, Class, Object)
     */
    public <T> T getProperty(String key, Class<T> as){
        return getProperty(key, as, null);
    }

    /**
     * Returns property as an object of specified class
     * <p>
     * Supported types are:
     * <ul>
     *     <li>{@link String}</li>
     *     <li>{@link Boolean} or boolean</li>
     *     <li>{@link Byte} or byte</li>
     *     <li>{@link Short} or short</li>
     *     <li>{@link Integer} or int</li>
     *     <li>{@link Long} or long</li>
     *     <li>{@link Float} or float</li>
     *     <li>{@link Double} or double</li>
     *     <li>{@link File} - created as new File(&lt;value&gt;)</li>
     *     <li>{@link BigDecimal} - created as new BigDecimal(&lt;value&gt;)</li>
     *     <li>{@link BigInteger} - created as new BigInteger(&lt;value&gt;)</li>
     *     <li>{@link Pattern} - created as Pattern.compile(^(&lt;value&gt;)$)</li>
     * </ul>
     *
     * @param key property key
     * @param as required class
     * @param def default value
     * @param <T> class
     * @return property as a specified class or {@code def} if property does not
     *      exist or type is not supported
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> as, T def){
        String p = getProperty(key);
        if(p == null){
            return def;
        }
        try{
            if(String.class == as){
                return (T)p;
            } else if(Boolean.class == as || boolean.class == as){
                return (T)Boolean.valueOf(p);
            } else if(Byte.class == as || byte.class == as){
                return (T)Byte.valueOf(p);
            } else if(Short.class == as || short.class == as){
                return (T)Short.valueOf(p);
            } else if(Integer.class == as || int.class == as){
                return (T)Integer.valueOf(p);
            } else if(Long.class == as || long.class == as){
                return (T)Long.valueOf(p);
            } else if(Float.class == as || float.class == as){
                return (T)Float.valueOf(p);
            } else if(Double.class == as || double.class == as){
                return (T)Double.valueOf(p);
            } else if(File.class == as){
                return (T)new File(p);
            } else if(BigDecimal.class == as){
                return (T)new BigDecimal(p);
            } else if(BigInteger.class == as){
                return (T)new BigInteger(p);
            } else if(Pattern.class == as){
                return (T)Pattern.compile("^(" + p + ")$");
            } else {
                LOG.warn("Unsupported type {}", as);
                return def;
            }
        } catch (Exception ex){
            LOG.warn("Cannot read property {} as a {}", key, as, ex);
            return def;
        }
    }

    /**
     * Sets property.
     *
     * @param key property key
     * @param value property value
     */
    public void setProperty(String key, String value){
        if(key == null){
            return;
        }
        if(value == null){
            props.remove(key);
        } else {
            props.setProperty(key, value);
        }
    }

    /**
     * Returns additional connection properties.
     *
     * @return additional connection properties
     */
    public Properties getAdditionalConnectionProperties(){
        Properties out = new Properties();
        for(Entry<Object, Object> e : props.entrySet()){
            if(e.getKey() instanceof String && ((String)e.getKey()).startsWith(Keys.CONNECTION_PROPERTY_PREFIX)){
                if(e.getValue() instanceof String){
                    out.setProperty(((String)e.getKey()).substring(Keys.CONNECTION_PROPERTY_PREFIX.length()), ((String)e.getValue()));
                }
            }
        }
        return out;
    }

    /**
     * Adds additional connection property. If property with specified key exists,
     * it is overridden with new value.
     *
     * @param key property key
     * @param value property value
     */
    public void setAdditionalConnectionProperty(String key, String value){
        if(key != null){
            setProperty(Keys.CONNECTION_PROPERTY_PREFIX + key, value);
        }
    }

    /**
     * Returns connection strategy.
     *
     * @return connection strategy.
     */
    public String getConnectionStrategy(){
        return getProperty(Keys.CONNECTION_STRATEGY);
    }

    /**
     * Returns time for one query as a {@link String}.
     *
     * @return time for one query
     * @see #getTimeForOneQuery()
     */
    public String getTimeForOneQueryStr(){
        return getProperty(Keys.TIME_FOR_ONE_QUERY);
    }

    /**
     * Returns time for one query
     * @return
     */
    public long getTimeForOneQuery(){
        return getProperty(Keys.TIME_FOR_ONE_QUERY, long.class, -1L);
    }

    /**
     * Returns ping query.
     *
     * @return ping query
     */
    public String getPingQuery(){
        return getProperty(Keys.PING_QUERY);
    }

    /**
     * Returns URL.
     *
     * @return URL
     */
    public String getUrl(){
        return getProperty(Keys.URL);
    }

    /**
     * Returns driver class.
     *
     * @return driver class
     */
    public String getDriverClass(){
        return getProperty(Keys.DRIVER_CLASS);
    }

    /**
     * Returns scenario as a {@link String}
     *
     * @return scenario
     * @see #getScenario()
     */
    public String getScenarioStr(){
        return getProperty(Keys.SCENARIO);
    }

    /**
     * Returns scenario file.
     *
     * @return scenario file
     */
    public File getScenario(){
        return getProperty(Keys.SCENARIO, File.class, null);
    }

    /**
     * Returns include scenario as a String.
     *
     * @return include scenario
     * @see #getIncludeScenario()
     */
    public String getIncludeScenarioStr(){
        return getProperty(Keys.INCLUDE_SCENARIOS);
    }

    /**
     * Returns include scenario pattern. Default is pattern which matches everything.
     *
     * @return include scenario pattern
     */
    public Pattern getIncludeScenario(){
        return getProperty(Keys.INCLUDE_SCENARIOS, Pattern.class, ALL_PATTERN);
    }

    /**
     * Returns exclude scenario as a String.
     *
     * @return exclude scenario
     * @see #getExcludeScenario()
     */
    public String getExcludeScenarioStr(){
        return getProperty(Keys.EXCLUDE_SCENARIOS);
    }

    /**
     * Returns exclude scenario pattern. Default is pattern which matches nothing.
     *
     * @return exclude scenario pattern
     */
    public Pattern getExcludeScenario(){
        return getProperty(Keys.EXCLUDE_SCENARIOS, Pattern.class, NOTHING_PATTERN);
    }

    /**
     * Returns include suite as a String.
     *
     * @return include suite
     * @see #getIncludeSuite()
     */
    public String getIncludeSuiteStr(){
        return getProperty(Keys.INCLUDE_SUITES);
    }

    /**
     * Returns include suite pattern. Default is pattern which matches everything.
     *
     * @return include suite pattern
     */
    public Pattern getIncludeSuite(){
        return getProperty(Keys.INCLUDE_SUITES, Pattern.class, ALL_PATTERN);
    }

    /**
     * Returns exclude suite as a String.
     *
     * @return exclude suite
     * @see #getExcludeSuite()
     */
    public String getExcludeSuiteStr(){
        return getProperty(Keys.EXCLUDE_SUITES);
    }

    /**
     * Returns exclude suite pattern. Default is pattern which matches nothing.
     *
     * @return exclude suite pattern
     */
    public Pattern getExcludeSuite(){
        return getProperty(Keys.EXCLUDE_SUITES, Pattern.class, NOTHING_PATTERN);
    }

    /**
     * Returns artifacts directory as a {@link String}
     *
     * @return artifacts directory
     * @see #getArtifactsDir()
     */
    public String getArtifactsDirStr(){
        return getProperty(Keys.ARTIFACTS_DIR);
    }

    /**
     * Returns artifacts directory.
     *
     * @return artifacts directory file
     */
    public File getArtifactsDir(){
        return getProperty(Keys.ARTIFACTS_DIR, File.class, null);
    }

    /**
     * Returns output directory as a {@link String}
     *
     * @return output directory
     * @see #getOutputDir()
     */
    public String getOutputDirStr(){
        return getProperty(Keys.OUTPUT_DIR);
    }

    /**
     * Returns output directory.
     *
     * @return output directory file
     */
    public File getOutputDir(){
        return getProperty(Keys.OUTPUT_DIR, File.class, null);
    }

    /**
     * Returns result mode.
     *
     * @return result mode
     */
    public String getResultMode(){
        return getProperty(Keys.RESULT_MODE);
    }

    /**
     * Returns allowed divergence as a {@link String}
     *
     * @return allowed divergence
     * @see #getAllowedDivergence()
     */
    public String getAllowedDivergenceStr(){
        return getProperty(Keys.ALLOWED_DIVERGENCE);
    }

    /**
     * Returns allowed divergence.
     *
     * @return allowed divergence number
     */
    public BigDecimal getAllowedDivergence(){
        return getProperty(Keys.ALLOWED_DIVERGENCE, BigDecimal.class, null);
    }

    /**
     * Returns query set directory name.
     *
     * @return query set directory name
     */
    public String getQuerySetDir(){
        return getProperty(Keys.QUERYSET_DIR);
    }

    /**
     * Returns expected results directory name.
     *
     * @return expected results directory name
     */
    public String getExpectedResultsDir(){
        String[] trimmedExpectedResultsDirArray = getProperty(Keys.EXPECTED_RESULTS_DIR).trim().split("\\s*,\\s*");
        return String.join(",", trimmedExpectedResultsDirArray);
    }

    /**
     * Returns test queries directory name.
     *
     * @return test queries directory name
     */
    public String getTestQueriesDir(){
        return getProperty(Keys.TEST_QUERIES_DIR);
    }

    /**
     * Returns valid connection SQL
     *
     * @return valid connection SQL
     */
    public String getValidConnectionSql(){
        return getProperty(Keys.VALID_CONNECTION_SQL);
    }

    /**
     * Returns after query.
     *
     * @return after query
     */
    public String getAfterQuery(){
        return getProperty(Keys.AFTER_QUERY);
    }

    /**
     * Returns fast-fail as a {@link String}
     *
     * @return fast-fail
     * @see #getQuerySetFastFail()
     */
    public String getQuerySetFastFailStr(){
        return getProperty(Keys.QUERY_SET_FAST_FAIL);
    }

    /**
     * Returns fast-fail.
     *
     * @return fast-fail
     */
    public boolean getQuerySetFastFail(){
        return getProperty(Keys.QUERY_SET_FAST_FAIL, boolean.class, true);
    }

    /**
     * Sets connection strategy.
     *
     * @param value connection strategy
     */
    public void setConnectionStrategy(String value){
        setProperty(Keys.CONNECTION_STRATEGY, value);
    }

    /**
     * Sets time for one query.
     *
     * @param value time for one query
     * @see #setTimeForOneQuery(long)
     */
    public void setTimeForOneQuery(String value){
        setProperty(Keys.TIME_FOR_ONE_QUERY, value);
    }

    /**
     * Sets time for one query.
     *
     * @param value time for one query
     */
    public void setTimeForOneQuery(long value){
        setTimeForOneQuery(Long.toString(value));
    }

    /**
     * Sets ping query.
     *
     * @param value ping query
     */
    public void setPingQuery(String value){
        setProperty(Keys.PING_QUERY, value);
    }

    /**
     * Sets URL.
     *
     * @param value URL
     */
    public void setUrl(String value){
        setProperty(Keys.URL, value);
    }

    /**
     * Sets driver class.
     *
     * @param value driver class
     */
    public void setDriverClass(String value){
        setProperty(Keys.DRIVER_CLASS, value);
    }

    /**
     * Sets scenario.
     *
     * @param value scenario
     */
    public void setScenario(String value){
        setProperty(Keys.SCENARIO, value);
    }

    /**
     * Sets include scenario pattern.
     *
     * @param value include scenario pattern
     */
    public void setIncludeScenario(String value){
        setProperty(Keys.INCLUDE_SCENARIOS, value);
    }

    /**
     * Sets exclude scenario pattern.
     *
     * @param value exclude scenario pattern
     */
    public void setExcludeScenario(String value){
        setProperty(Keys.EXCLUDE_SCENARIOS, value);
    }

    /**
     * Sets include suite pattern.
     *
     * @param value include suite pattern
     */
    public void setIncludeSuite(String value){
        setProperty(Keys.INCLUDE_SUITES, value);
    }

    /**
     * Sets exclude suite pattern.
     *
     * @param value exclude suite pattern
     */
    public void setExcludeSuite(String value){
        setProperty(Keys.EXCLUDE_SUITES, value);
    }

    /**
     * Sets artifacts directory.
     *
     * @param value artifacts directory
     */
    public void setArtifactsDir(String value){
        setProperty(Keys.ARTIFACTS_DIR, value);
    }

    /**
     * Sets output directory.
     *
     * @param value output directory
     */
    public void setOutputDir(String value){
        setProperty(Keys.OUTPUT_DIR, value);
    }

    /**
     * Sets result mode.
     * @param value result mode
     */
    public void setResultMode(String value){
        setProperty(Keys.RESULT_MODE, value);
    }

    /**
     * Sets allowed divergence.
     *
     * @param value allowed divergence
     */
    public void setAllowedDivergence(String value){
        setProperty(Keys.ALLOWED_DIVERGENCE, value);
    }

    /**
     * Sets query set directory name.
     *
     * @param value query set directory name
     */
    public void setQuerySetDir(String value){
        setProperty(Keys.QUERYSET_DIR, value);
    }

    /**
     * Sets expected results directory name.
     *
     * @param value expected results directory name
     */
    public void setExpectedResultsDir(String value){
        setProperty(Keys.EXPECTED_RESULTS_DIR, value);
    }

    /**
     * Sets test queries directory name.
     *
     * @param value test queries directory name
     */
    public void setTestQueriesDir(String value){
        setProperty(Keys.TEST_QUERIES_DIR, value);
    }

    /**
     * Sets valid connection SQL.
     *
     * @param value valid connection SQL
     */
    public void setValidConnectionSql(String value){
        setProperty(Keys.VALID_CONNECTION_SQL, value);
    }

    /**
     * Sets after query.
     *
     * @param value after query
     */
    public void setAfterQuery(String value){
        setProperty(Keys.AFTER_QUERY, value);
    }

    /**
     * Sets fast-fail.
     *
     * @param value fast-fail
     * @see #setQuerySetFastFail(boolean)
     */
    public void setQuerySetFastFail(String value){
        setProperty(Keys.QUERY_SET_FAST_FAIL, value);
    }

    /**
     * Sets fast-fail.
     *
     * @param value fast-fail
     */
    public void setQuerySetFastFail(boolean value){
        setProperty(Keys.QUERY_SET_FAST_FAIL, Boolean.toString(value));
    }

    /**
     * Resolves placeholders in properties.
     * <p>
     * Placeholders are declared as ${...}.
     * If some key is not present in passed properties, placeholder remains unchanged.
     */
    public void resolvePlaceholders(){
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
                        throw new IllegalArgumentException("Recursive placeholders: " + props);
                    }
                    for(String ph : phs){
                        Object toReplace = resolved.get(ph);
                        if(toReplace != null){
                            String replacement = toReplace.toString();
                            if(LOG.isTraceEnabled()){
                                LOG.trace("Replacing '{}' with '{}'.", ph, replacement);
                            }
                            e.setValue(value.replace("${" + ph + "}", replacement));
                            changed = true;
                        }
                    }
                }
            }
        }
        props.clear();
        props.putAll(resolved);
    }

    /**
     * Creates copy of this properties.
     *
     * @return copy of this properties.
     */
    public WhipperProperties copy(){
        Properties p = new Properties();
        p.putAll(props);
        return new WhipperProperties(p);
    }

    /**
     * Adds all properties.
     *
     * @param p properties to be added
     */
    public void addAll(WhipperProperties p){
        addAll(p.props);
    }

    /**
     * Adds all properties.
     *
     * @param p properties to be added
     */
    public void addAll(Properties p){
        props.putAll(p);
    }

    /**
     * Creates copy of the properties by discarding old values
     * and adding all properties from {@code p} to this properties.
     *
     * @param p properties
     */
    public void copyFrom(WhipperProperties p){
        copyFrom(p.props);
    }

    /**
     * Creates copy of the properties by discarding old values
     * and adding all properties from {@code p} to this properties.
     *
     * @param p properties
     */
    public void copyFrom(Properties p){
        props.clear();
        props.putAll(p);
    }

    /**
     * Clear all properties
     */
    public void clear(){
        props.clear();
    }

    /**
     * Testing purpose.
     *
     * @return number of properties
     */
    int size(){
        return props.size();
    }

    /**
     * Dumps properties to output directory.
     */
    public void dumpPropertiesToOutputDir(){
        File f = getOutputDir();
        if(f == null){
            LOG.error("Output directory not set.");
        } else if (!f.exists() && !f.mkdirs()){
            LOG.error("Cannot create output directory {}", f.getAbsolutePath());
        } else if (f.exists() && f.isFile()){
            LOG.error("Cannot create output directory. {} is file.", f.getAbsolutePath());
        } else {
            try(FileWriter fw = new FileWriter(getDumpPropertiesFile(f))){
                props.store(fw, "Automatic dump.");
            } catch (IOException ex){
                LOG.error("Cannot dump properties - {}.", ex.getMessage(), ex);
            }
        }
    }

    @Override
    public String toString(){
        return props.toString();
    }

    /**
     * Returns file for properties file.
     *
     * @param dir directory with files.
     * @return properties file
     */
    private static File getDumpPropertiesFile(File dir){
        return new File(dir, DUMP_PROPS_FILE);
    }

    /**
     * Loads properties from directory.
     *
     * @param dir directory with properties file
     * @return loaded properties or {@code null} if file does not exist
     */
    public static WhipperProperties fromOutputDir(File dir){
        File f = getDumpPropertiesFile(dir);
        return f.exists() ? new WhipperProperties(f) : null;
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
}
