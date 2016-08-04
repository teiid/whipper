package org.whipper.results;

import java.util.Properties;

import org.whipper.Scenario;

public interface TestResultsWriter{

    boolean init(Properties props);

    void destroy();

    void writeResultOfScenario(Scenario scen);
}
