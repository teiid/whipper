package org.whipper;

import java.util.List;

/**
 * Class implementing this interface can watch progress of the whipper test.
 */
public interface ProgressMonitor{

    /**
     * This method is invoked before any scenario is run.
     *
     * @param scenariosToRun name of all scenarios
     */
    void starting(List<String> scenariosToRun);

    /**
     * This method is invoked after all scenarios finish.
     *
     * @param result result of the test
     */
    void finished(WhipperResult result);

    /**
     * This method will be invoked before each scenario.
     *
     * @param scen scenario to be invoked
     */
    void startingScenario(Scenario scen);

    /**
     * This method will be invoked after each scenario.
     *
     * @param scen finished scenario
     */
    void scenarioFinished(Scenario scen);

    /**
     * This method will be invoked before each suite.
     *
     * @param suite suite to ne invoked
     */
    void startingSuite(Suite suite);

    /**
     * This method will be invoked after each suite.
     *
     * @param suite finished suite
     */
    void suiteFinished(Suite suite);

    /**
     * This method will be invoked before each query set.
     *
     * @param qs query set to be invoked
     */
    void startingQuerySet(QuerySet qs);

    /**
     * This method will be invoked after each query set.
     *
     * @param qs finished query
     */
    void querySetFinished(QuerySet qs);

    /**
     * This method will be invoked before each query.
     *
     * @param q query to be invoked
     */
    void startingQuery(Query q);

    /**
     * This method will be invoked after each query.
     *
     * @param q finished query
     */
    void queryFinished(Query q);

    /**
     * This method will be invoked before each meta-query set.
     *
     * @param qs meta-query set to be invoked
     */
    void startingMetaQuerySet(QuerySet qs);

    /**
     * This method will be invoked after each meta-query set.
     *
     * @param qs finished meta-query
     */
    void metaQuerySetFinished(QuerySet qs);

    /**
     * This method will be invoked before each meta-query.
     *
     * @param q meta-query to be invoked
     */
    void startingMetaQuery(Query q);

    /**
     * This method will be invoked after each meta-query.
     *
     * @param q finished meta-query
     */
    void metaQueryFinished(Query q);
}
