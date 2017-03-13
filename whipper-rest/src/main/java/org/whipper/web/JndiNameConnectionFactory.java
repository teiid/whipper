package org.whipper.web;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.whipper.Whipper;
import org.whipper.WhipperProperties;
import org.whipper.connection.ConnectionFactory;

/**
 * JNDI name connection strategy. Creates connection from data source
 * with defined JNDI name.
 */
public class JndiNameConnectionFactory implements ConnectionFactory{

    private static final String JNDI_PROPERTY = "jndi.name";
    private String jndiName;

    @Override
    public void init(WhipperProperties props){
        jndiName = props.getProperty(JNDI_PROPERTY);
    }

    @Override
    public Connection getConnection() throws Exception{
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource)ic.lookup(jndiName);
        return ds.getConnection();
    }

    @Override
    public boolean isConnectionValid(Connection con){
        try{
            return con != null && !con.isClosed() && con.isValid(1);
        } catch (SQLException ex){
            return false;
        }
    }

    @Override
    public void closeConnection(Connection con){
        Whipper.close(con);
    }

    @Override
    public String getName(){
        return "JNDI";
    }
}
