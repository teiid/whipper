package org.whipper.resultmode;

import java.util.Properties;

import org.whipper.Query;

/**
 * The class which implements this interface should be responsible for handling result of the query.
 *
 * @author Juraj Duráni
 */
public interface ResultMode {

    /**
     * Sets/resets configuration of this result mode. May be called multiple times.
     *
     * @param props test properties
     */
    void resetConfiguration(Properties props);

    /**
     * Handles query result.
     *
     * @param q query
     * @return result handler with result of the query
     */
    ResultHandler handleResult(Query q);

    /**
     * Destroys result mode.
     */
    void destroy();

    /**
     * Returns name of this result mode.
     *
     * @return name
     */
    String getName();
}
