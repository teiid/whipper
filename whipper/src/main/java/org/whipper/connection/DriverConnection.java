package org.whipper.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.whipper.Whipper;
import org.whipper.WhipperProperties;

/**
 * This factory will load driver and use {@link DriverManager} to obtain connection to database.
 *
 * @author Juraj Duráni
 */
public class DriverConnection implements ConnectionFactory {

    private Properties connectionProperties;
    private String url;
    private String driverClassName;
    private String validConnectionQuery;

    @Override
    public void init(WhipperProperties props) {
        url = props.getUrl();
        driverClassName = props.getDriverClass();
        connectionProperties = props.getAdditionalConnectionProperties();
        validConnectionQuery = props.getValidConnectionSql();
        validConnectionQuery = validConnectionQuery == null ? "SELECT 1" : validConnectionQuery;
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
            Whipper.close(s);
        }
    }

    @Override
    public void closeConnection(Connection con) {
        Whipper.close(con);
    }

    @Override
    public String getName() {
        return "DRIVER";
    }
}
