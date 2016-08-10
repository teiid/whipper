package org.whipper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.whipper.Query.QueryResult;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ExecutionInterruptedException;
import org.whipper.exceptions.ServerNotAvailableException;

public class QuerySetTest {

    private static final Map<Integer, Boolean> MAP = new HashMap<>();
    private static final AtomicInteger I = new AtomicInteger();

    @Test(expected = IllegalArgumentException.class)
    public void addNullQueryTest(){
        new QuerySet("", true).addQuery(null);
    }

    @Test
    public void numberOfAllQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assert.assertEquals("Number of all queries.", 3, qs.getNumberOfAllQueries());
        qs.runQueries();
        Assert.assertEquals("Number of all queries.", 3, qs.getNumberOfAllQueries());
    }

    @Test
    public void numberOfExecutedQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        Assert.assertEquals("Number of executed queries.", 0, qs.getNumberOfExecutedQueries());
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assert.assertEquals("Number of executed queries.", 0, qs.getNumberOfExecutedQueries());
        qs.runQueries();
        Assert.assertEquals("Number of executed queries.", 3, qs.getNumberOfExecutedQueries());
    }

    @Test
    public void numberOfPassedQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        Assert.assertEquals("Number of passed queries.", 0, qs.getNumberOfPassedQueries());
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assert.assertEquals("Number of passed queries.", 0, qs.getNumberOfPassedQueries());
        qs.runQueries();
        Assert.assertEquals("Number of passed queries.", 3, qs.getNumberOfPassedQueries());
    }

    @Test
    public void numberOfFailedQueriesTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        Assert.assertEquals("Number of failed queries.", 0, qs.getNumberOfFailedQueries());
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        qs.addQuery(getQuery(true, null));
        Assert.assertEquals("Number of failed queries.", 0, qs.getNumberOfFailedQueries());
        qs.runQueries();
        Assert.assertEquals("Number of failed queries.", 0, qs.getNumberOfFailedQueries());
    }

    @Test
    public void fastFailTrueTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        Query q1 = getQuery(true, null);
        Query q2 = getQuery(false, null);
        Query q3 = getQuery(true, null);
        qs.addQuery(q1);
        qs.addQuery(q2);
        qs.addQuery(q3);

        qs.runQueries();
        Assert.assertEquals("Executed", 2, qs.getNumberOfExecutedQueries());
        Assert.assertEquals("Failed", 1, qs.getNumberOfFailedQueries());
        Assert.assertEquals("Passed", 1, qs.getNumberOfPassedQueries());
        Assert.assertEquals("Failed queries", Arrays.asList(q2), qs.getFailedQueries());
        Mockito.verify(q1).run();
        Mockito.verify(q2).run();
        Mockito.verify(q3, Mockito.never()).run();
    }

    @Test
    public void fastFailFalseTest() throws Exception{
        QuerySet qs = new QuerySet("", false);
        Query q1 = getQuery(true, null);
        Query q2 = getQuery(false, null);
        Query q3 = getQuery(true, null);
        qs.addQuery(q1);
        qs.addQuery(q2);
        qs.addQuery(q3);

        qs.runQueries();
        Assert.assertEquals("Executed", 3, qs.getNumberOfExecutedQueries());
        Assert.assertEquals("Failed", 1, qs.getNumberOfFailedQueries());
        Assert.assertEquals("Passed", 2, qs.getNumberOfPassedQueries());
        Assert.assertEquals("Failed queries", Arrays.asList(q2), qs.getFailedQueries());
        Mockito.verify(q1).run();
        Mockito.verify(q2).run();
        Mockito.verify(q3).run();
    }

    @Test(expected = ServerNotAvailableException.class)
    public void serverNotAvailableTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        qs.addQuery(getQuery(false, new ServerNotAvailableException("Thrown in test.")));
        qs.runQueries();
    }

    @Test(expected = DbNotAvailableException.class)
    public void dbNotAvailableTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        qs.addQuery(getQuery(false, new DbNotAvailableException("Thrown in test.")));
        qs.runQueries();
    }

    @Test(expected = ExecutionInterruptedException.class)
    public void executionInterruptedTest() throws Exception{
        QuerySet qs = new QuerySet("", true);
        qs.addQuery(getQuery(true, null));
        Thread.currentThread().interrupt();
        try{
            qs.runQueries();
        } finally {
            // clear flag
            Thread.interrupted();
        }
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
        Mockito.doAnswer(new Answer<QueryResult>(){
            @Override
            public QueryResult answer(InvocationOnMock invocation) throws Throwable{
                return MAP.get(i) ? qr : null;
            }
        }).when(q).getResult();
        Mockito.doAnswer(new Answer<Boolean>(){
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable{
                return MAP.get(i);
            }
        }).when(q).isExecuted();
        Mockito.doAnswer(new Answer<Void>(){
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable{
                MAP.put(i, Boolean.TRUE);
                return null;
            }
        }).when(q).run();
        Mockito.doReturn(Mockito.mock(Scenario.class)).when(q).getScenario();
        Mockito.doReturn(Mockito.mock(Suite.class)).when(q).getSuite();

        Mockito.doReturn(q).when(qr).getQuery();
        return q;
    }
}
