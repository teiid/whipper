package org.whipper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.whipper.Query.QueryResult;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ExecutionInterruptedException;
import org.whipper.exceptions.ServerNotAvailableException;
import org.whipper.resultmode.MetaQuerySetResultMode;

public class QuerySetTest{

    private static final Map<Integer, Boolean> MAP = new HashMap<>();
    private static final AtomicInteger I = new AtomicInteger();

    @Test
    public void addNullQueryTest(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new QuerySet("", true, null).addQuery(null));
    }

    @Test
    public void numberOfAllQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assertions.assertEquals(3, qs.getNumberOfAllQueries(), "Number of all queries.");
        qs.runQueries();
        Assertions.assertEquals(3, qs.getNumberOfAllQueries(), "Number of all queries.");
    }

    @Test
    public void numberOfExecutedQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        Assertions.assertEquals(0, qs.getNumberOfExecutedQueries(), "Number of executed queries.");
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assertions.assertEquals(0, qs.getNumberOfExecutedQueries(), "Number of executed queries.");
        qs.runQueries();
        Assertions.assertEquals(3, qs.getNumberOfExecutedQueries(), "Number of executed queries.");
    }

    @Test
    public void numberOfPassedQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        Assertions.assertEquals(0, qs.getNumberOfPassedQueries(), "Number of passed queries.");
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assertions.assertEquals(0, qs.getNumberOfPassedQueries(), "Number of passed queries.");
        qs.runQueries();
        Assertions.assertEquals(3, qs.getNumberOfPassedQueries(), "Number of passed queries.");
    }

    @Test
    public void numberOfFailedQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        Assertions.assertEquals(0, qs.getNumberOfFailedQueries(), "Number of failed queries.");
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assertions.assertEquals(0, qs.getNumberOfFailedQueries(), "Number of failed queries.");
        qs.runQueries();
        Assertions.assertEquals(0, qs.getNumberOfFailedQueries(), "Number of failed queries.");
    }

    @Test
    public void fastFailTrueTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        Query q1 = getQuery(true, null);
        Query q2 = getQuery(false, null);
        Query q3 = getQuery(true, null);
        qs.addQuery(q1);
        qs.addQuery(q2);
        qs.addQuery(q3);

        qs.runQueries();
        Assertions.assertAll(() -> Assertions.assertEquals(2, qs.getNumberOfExecutedQueries(), "Executed"),
                () -> Assertions.assertEquals(1, qs.getNumberOfFailedQueries(), "Failed"),
                () -> Assertions.assertEquals(1, qs.getNumberOfPassedQueries(), "Passed"),
                () -> Assertions.assertEquals(Collections.singletonList(q2), qs.getFailedQueries(), "Failed queries"));
        Mockito.verify(q1).run();
        Mockito.verify(q2).run();
        Mockito.verify(q3, Mockito.never()).run();
    }

    @Test
    public void fastFailFalseTest() throws Exception{
        QuerySet qs = new QuerySet("", false, null);
        Query q1 = getQuery(true, null);
        Query q2 = getQuery(false, null);
        Query q3 = getQuery(true, null);
        qs.addQuery(q1);
        qs.addQuery(q2);
        qs.addQuery(q3);

        qs.runQueries();
        Assertions.assertAll(() -> Assertions.assertEquals(3, qs.getNumberOfExecutedQueries(), "Executed"),
                () -> Assertions.assertEquals(1, qs.getNumberOfFailedQueries(), "Failed"),
                () -> Assertions.assertEquals(2, qs.getNumberOfPassedQueries(), "Passed"),
                () -> Assertions.assertEquals(Collections.singletonList(q2), qs.getFailedQueries(), "Failed queries"));
        Mockito.verify(q1).run();
        Mockito.verify(q2).run();
        Mockito.verify(q3).run();
    }

    @Test
    public void serverNotAvailableTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        qs.addQuery(getQuery(false, new ServerNotAvailableException("Thrown in test.")));
        Assertions.assertThrows(ServerNotAvailableException.class, qs::runQueries);
    }

    @Test
    public void dbNotAvailableTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        qs.addQuery(getQuery(false, new DbNotAvailableException("Thrown in test.")));
        Assertions.assertThrows(DbNotAvailableException.class, qs::runQueries);
    }

    @Test
    public void executionInterruptedTest() throws Exception{
        QuerySet qs = new QuerySet("", true, null);
        qs.addQuery(getQuery(true, null));
        Thread.currentThread().interrupt();
        try{
            Assertions.assertThrows(ExecutionInterruptedException.class, qs::runQueries);
        } finally {
            // clear flag
            Thread.interrupted();
        }
    }

    @Test
    public void beforeFailedOnMetaTest(){
        Assertions.assertThrows(IllegalStateException.class, () -> new QuerySet("", true, null).beforeFailed(null, null));
    }

    @Test
    public void beforeFailedTest(){
        Query q1 = Mockito.mock(Query.class);
        Query q2 = Mockito.mock(Query.class);
        Query q3 = Mockito.mock(Query.class);
        MetaQuerySetResultMode rm = Mockito.mock(MetaQuerySetResultMode.class);
        QuerySet qs = new QuerySet("", true, rm);
        qs.addQuery(q1);
        qs.addQuery(q2);
        qs.addQuery(q3);
        Exception ex = Mockito.mock(Exception.class);
        String type = "aaa";
        qs.beforeFailed(ex, type);
        Mockito.verify(rm).writeErrorsForMainQuerySet(Mockito.same(qs));
        Mockito.verify(q1).beforeSetFailed(Mockito.same(ex), Mockito.same(type));
        Mockito.verify(q2).beforeSetFailed(Mockito.same(ex), Mockito.same(type));
        Mockito.verify(q3).beforeSetFailed(Mockito.same(ex), Mockito.same(type));
    }

    @Test
    public void setMainIdFailOnNonMetaTest(){
        Assertions.assertThrows(IllegalStateException.class, () -> new QuerySet("", true, Mockito.mock(MetaQuerySetResultMode.class)).setMainId(""));
    }

    @Test
    public void getMainIdFailOnNonMetaTest(){
        Assertions.assertThrows(IllegalStateException.class, () -> new QuerySet("", true, Mockito.mock(MetaQuerySetResultMode.class)).getMainId());
    }

    @Test
    public void setGetMainIdOkOnMetaTest(){
        QuerySet qs = new QuerySet("", true, null);
        String id = "aaa";
        qs.setMainId(id);
        Assertions.assertEquals(id, qs.getMainId(), "Main ID.");
    }

    private Query getQuery(boolean pass, Exception ex){
        final QueryResult qr = Mockito.mock(QueryResult.class);
        final int i = I.getAndIncrement();
        MAP.put(i, Boolean.FALSE);
        Mockito.doReturn(pass).when(qr).pass();
        if(ex == null){
            Mockito.doReturn(false).when(qr).isException();
            Mockito.doReturn(null).when(qr).getException();
        } else {
            Mockito.doReturn(true).when(qr).isException();
            Mockito.doReturn(ex).when(qr).getException();
        }
        Query q = Mockito.mock(Query.class);
        Mockito.doAnswer(invocation -> MAP.get(i) ? qr : null).when(q).getResult();
        Mockito.doAnswer(invocation -> MAP.get(i)).when(q).isExecuted();
        Mockito.doAnswer(invocation -> {
            MAP.put(i, Boolean.TRUE);
            return null;
        }).when(q).run();
        Mockito.doReturn(Mockito.mock(Scenario.class)).when(q).getScenario();
        Mockito.doReturn(Mockito.mock(Suite.class)).when(q).getSuite();

        Mockito.doReturn(q).when(qr).getQuery();
        return q;
    }
}
