package org.jboss.bqt;

import java.io.IOException;
import java.io.Writer;

/**
 * Result of the test.
 *
 * @author Juraj Duráni
 */
public interface TestResult {

    /**
     * Writes header of the result
     *
     * @param wr writer to be used to write header
     * @throws IOException if some error occurs
     */
    void writeHeader(Writer wr) throws IOException;

    /**
     * Writes result of the test.
     *
     * @param wr writer to be used to write result
     * @throws IOException if some error occurs
     */
    void write(Writer wr) throws IOException;
}
