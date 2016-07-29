package org.jboss.bqt.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Map.Entry;

import org.jboss.bqt.BqtTool;
import org.jboss.bqt.BqtTool.Keys;

/**
 * This factory will load drive and use {@link DriverManager} to obtain connection to database.
 *
 * @author Juraj Duráni
 */
public class DriverConnection implements ConnectionFactory {

    private Properties connectionProperties;
    private String url;
    private String driverClassName;
    private String validConnectionQuery;

    @Override
    public void init(Properties props) {
        url = props.getProperty(Keys.URL);
        driverClassName = props.getProperty(Keys.DRIVER_CLASS);
        connectionProperties = getConnectionProperties(props);
        validConnectionQuery = props.getProperty(Keys.VALID_CONNECTION_SQL, "SELECT 1");
    }

    /**
     * Extracts connection properties from {@code props}.
     *
     * @param props properties
     * @return connection properties
     */
    private Properties getConnectionProperties(Properties props) {
        Properties conProps = new Properties();
        for(Entry<Object, Object> e : props.entrySet()){
            String key = e.getKey().toString();
            if(key.startsWith(Keys.CONNECTION_PROPERTY_PREFIX)){
                conProps.put(key.substring(Keys.CONNECTION_PROPERTY_PREFIX.length()), e.getValue());
            }
        }
        return conProps;
    }

    @Override
    public Connection getConnection() throws Exception{
        Class.forName(driverClassName);
        return DriverManager.getConnection(url, connectionProperties);
    }

    @Override
    public boolean isConnectionValid(Connection con) {
        Statement s = null;
        try{
            if(con == null || con.isClosed() || !con.isValid(1)){
                return false;
            }
            s = con.createStatement();
            s.execute(validConnectionQuery);
            return true;
        } catch (SQLException ex){
            return false;
        } finally {
            BqtTool.close(s);
        }
    }

    @Override
    public void closeConnection(Connection con) {
        BqtTool.close(con);
    }

    @Override
    public String getName() {
        return "DRIVER";
    }

}
