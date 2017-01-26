package org.whipper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhipperProperties implements Cloneable{
    private static final Logger LOG = LoggerFactory.getLogger(WhipperProperties.class);
    private static final Pattern PH_PATTERN = Pattern.compile(".*\\$\\{.+\\}.*");

    /**
     * Keys.
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
        public static final String INCLUDE_SUITES = "whipper.suite.include";
        public static final String EXCLUDE_SUITES = "whipper.suite.exclude";
        public static final String ARTIFACTS_DIR = "queryset.artifacts.dir";
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

    private final Properties props;

    public WhipperProperties(){
        this(null, null);
    }

    public WhipperProperties(File propsFile){
        this(propsFile, null);
    }

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

    public WhipperProperties(Properties props){
        this.props = props == null ? new Properties() : props;
    }

    public String getProperty(String key){
        return key == null ? null : props.getProperty(key);
    }

    public <T> T getProperty(String key, Class<T> as){
        return getProperty(key, as, null);
    }

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

    public void setAdditionalConnectionProperty(String key, String value){
        if(key != null){
            setProperty(Keys.CONNECTION_PROPERTY_PREFIX + key, value);
        }
    }

    public String getConnectionStrategy(){
        return getProperty(Keys.CONNECTION_STRATEGY);
    }

    public String getTimeForOneQueryStr(){
        return getProperty(Keys.TIME_FOR_ONE_QUERY);
    }

    public long getTimeForOneQuery(){
        return getProperty(Keys.TIME_FOR_ONE_QUERY, long.class, -1l);
    }

    public String getPingQuery(){
        return getProperty(Keys.PING_QUERY);
    }

    public String getUrl(){
        return getProperty(Keys.URL);
    }

    public String getDriverClass(){
        return getProperty(Keys.DRIVER_CLASS);
    }

    public String getScenarioStr(){
        return getProperty(Keys.SCENARIO);
    }

    public File getScenario(){
        return getProperty(Keys.SCENARIO, File.class, null);
    }

    public String getIncludeScenarioStr(){
        return getProperty(Keys.INCLUDE_SCENARIOS);
    }

    public Pattern getIncludeScenario(){
        return getProperty(Keys.INCLUDE_SCENARIOS, Pattern.class, null);
    }

    public String getExcludeScenarioStr(){
        return getProperty(Keys.EXCLUDE_SCENARIOS);
    }

    public Pattern getExcludeScenario(){
        return getProperty(Keys.EXCLUDE_SCENARIOS, Pattern.class, null);
    }

    public String getIncludeSuiteStr(){
        return getProperty(Keys.INCLUDE_SUITES);
    }

    public Pattern getIncludeSuite(){
        return getProperty(Keys.INCLUDE_SUITES, Pattern.class, null);
    }

    public String getExcludeSuiteStr(){
        return getProperty(Keys.EXCLUDE_SUITES);
    }

    public Pattern getExcludeSuite(){
        return getProperty(Keys.EXCLUDE_SUITES, Pattern.class, null);
    }

    public String getArtifacstDirStr(){
        return getProperty(Keys.ARTIFACTS_DIR);
    }

    public File getArtifacstDir(){
        return getProperty(Keys.ARTIFACTS_DIR, File.class, null);
    }

    public String getOutputDirStr(){
        return getProperty(Keys.OUTPUT_DIR);
    }

    public File getOutputDir(){
        return getProperty(Keys.OUTPUT_DIR, File.class, null);
    }

    public String getResultMode(){
        return getProperty(Keys.RESULT_MODE);
    }

    public String getAllowedDivergenceString(){
        return getProperty(Keys.ALLOWED_DIVERGENCE);
    }

    public BigDecimal getAllowedDivergence(){
        return getProperty(Keys.ALLOWED_DIVERGENCE, BigDecimal.class, null);
    }

    public String getQuerySetDir(){
        return getProperty(Keys.QUERYSET_DIR);
    }

    public String getExpectedResultsDir(){
        return getProperty(Keys.EXPECTED_RESULTS_DIR);
    }

    public String getTestQueriesDir(){
        return getProperty(Keys.TEST_QUERIES_DIR);
    }

    public String getValidConnectionSql(){
        return getProperty(Keys.VALID_CONNECTION_SQL);
    }

    public String getAfterQuery(){
        return getProperty(Keys.AFTER_QUERY);
    }

    public String getQuerySetFastFailStr(){
        return getProperty(Keys.QUERY_SET_FAST_FAIL);
    }

    public boolean getQuerySetFastFail(){
        return getProperty(Keys.QUERY_SET_FAST_FAIL, boolean.class, true);
    }

    public void setConnectionStrategy(String value){
        setProperty(Keys.CONNECTION_STRATEGY, value);
    }

    public void setTimeForOneQuery(String value){
        setProperty(Keys.TIME_FOR_ONE_QUERY, value);
    }

    public void setTimeForOneQuery(long value){
        setTimeForOneQuery(Long.toString(value));
    }

    public void setPingQuery(String value){
        setProperty(Keys.PING_QUERY, value);
    }

    public void setUrl(String value){
        setProperty(Keys.URL, value);
    }

    public void setDriverClass(String value){
        setProperty(Keys.DRIVER_CLASS, value);
    }

    public void setScenario(String value){
        setProperty(Keys.SCENARIO, value);
    }

    public void setIncludeScenario(String value){
        setProperty(Keys.INCLUDE_SCENARIOS, value);
    }

    public void setExcludeScenario(String value){
        setProperty(Keys.EXCLUDE_SCENARIOS, value);
    }

    public void setArtifacstDir(String value){
        setProperty(Keys.ARTIFACTS_DIR, value);
    }

    public void setOutputDir(String value){
        setProperty(Keys.OUTPUT_DIR, value);
    }

    public void setResultMode(String value){
        setProperty(Keys.RESULT_MODE, value);
    }

    public void setAllowedDivergence(String value){
        setProperty(Keys.ALLOWED_DIVERGENCE, value);
    }

    public void setQuerySetDir(String value){
        setProperty(Keys.QUERYSET_DIR, value);
    }

    public void setExpectedResultsDir(String value){
        setProperty(Keys.EXPECTED_RESULTS_DIR, value);
    }

    public void setTestQueriesDir(String value){
        setProperty(Keys.TEST_QUERIES_DIR, value);
    }

    public void setValidConnectionSql(String value){
        setProperty(Keys.VALID_CONNECTION_SQL, value);
    }

    public void setAfterQuery(String value){
        setProperty(Keys.AFTER_QUERY, value);
    }

    public void setQuerySetFastFail(String value){
        setProperty(Keys.QUERY_SET_FAST_FAIL, value);
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
        props.clear();
        props.putAll(resolved);
    }

    public WhipperProperties copy(){
        Properties p = new Properties();
        p.putAll(props);
        return new WhipperProperties(p);
    }

    public void addAll(WhipperProperties p){
        addAll(p.props);
    }

    public void addAll(Properties p){
        props.putAll(p);
    }

    public void copyFrom(WhipperProperties p){
        copyFrom(p.props);
    }

    public void copyFrom(Properties p){
        props.clear();
        props.putAll(p);
    }

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

    @Override
    public String toString(){
        return props.toString();
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
