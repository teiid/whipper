package org.jboss.bqt.resultmode;

import java.util.Properties;

import org.jboss.bqt.Query;

/**
 * The class which implements this interface should be responsible for handling result of the query.
 *
 * @author Juraj Duráni
 */
public interface ResultMode {

    /**
     * Initializes class.
     *
     * @param props test properties.
     */
    void init(Properties props);

    /**
     * Handles query result.
     *
     * @param q query
     * @return result handler with result of the query
     */
    ResultHandler handleResult(Query q);

    /**
     * Destroys class.
     */
    void destroy();

    /**
     * Returns name of this result mode.
     *
     * @return name
     */
    String getName();
}
