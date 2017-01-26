package org.whipper.resultmode;

import java.io.File;

import org.whipper.Query;
import org.whipper.WhipperProperties;

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
    void resetConfiguration(WhipperProperties props);

    /**
     * Handles query result.
     *
     * @param q query
     * @return result handler with result of the query
     */
    ResultHolder handleResult(Query q);

    /**
     * Destroys result mode.
     */
    void destroy();

    /**
     * Returns path where can be written error for query
     * or {@code null} if error file is not supported.
     *
     * @param q query
     * @return path to query error file
     */
    File getErrorFile(Query q);

    /**
     * Returns name of this result mode.
     *
     * @return name
     */
    String getName();
}
