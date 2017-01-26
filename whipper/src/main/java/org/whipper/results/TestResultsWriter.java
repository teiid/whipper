package org.whipper.results;

import org.whipper.Scenario;
import org.whipper.WhipperProperties;

/**
 * Interface for writing results of the scenarios.
 *
 * @author Juraj Duráni
 */
public interface TestResultsWriter{

    /**
     * Initializes this writer.
     *
     * @param props properties to use to initialize writer
     * @return {@code true} if initialization completes normally
     *      and writer can be used for writing results, {@code false}
     *      otherwise
     */
    boolean init(WhipperProperties props);

    /**
     * Destroys this writer. Method is suppose to end this
     * writer gracefully.
     */
    void destroy();

    /**
     * Writes result of the scenario.
     *
     * @param scen scenario
     */
    void writeResultOfScenario(Scenario scen);
}
