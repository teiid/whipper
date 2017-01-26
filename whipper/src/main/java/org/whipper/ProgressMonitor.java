package org.whipper;

import java.util.List;

public interface ProgressMonitor{

    void starting(List<String> scenariosToRun);

    void finished(WhipperResult result);

    void startingScenario(Scenario scen);

    void scenarioFinished(Scenario scen);

    void startingSuite(Suite suite);

    void suiteFinished(Suite suite);

    void startingQuerySet(QuerySet qs);

    void querySetFinished(QuerySet qs);

    void startingQuery(Query q);

    void queryFinished(Query q);

    void startingMetaQuerySet(QuerySet qs);

    void metaQuerySetFinished(QuerySet qs);

    void startingMetaQuery(Query q);

    void metaQueryFinished(Query q);
}
