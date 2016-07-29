package org.jboss.bqt.connection;

import java.sql.Connection;
import java.util.Properties;

/**
 * Interface for providing connections to the databse.
 *
 * @author Juraj Duráni
 */
public interface ConnectionFactory {

    /**
     * Initializes class.
     *
     * @param props test properties
     */
    void init(Properties props);

    /**
     * Returns connection to the databse.
     *
     * @return connection
     * @throws Exception if some error occurs
     */
    Connection getConnection() throws Exception;

    /**
     * Validates connection.
     *
     * @param con connection to be validated.
     * @return {@code true} if connection {@code con} is valid, {@code false} otherwise
     */
    boolean isConnectionValid(Connection con);

    /**
     * Closes connection {@code con}.
     *
     * @param con connection to be closed.
     */
    void closeConnection(Connection con);

    /**
     * Returns name of this connection factory.
     *
     * @return name of this factory
     */
    String getName();
}
