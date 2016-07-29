package org.jboss.bqt;

import org.jboss.bqt.exceptions.DbNotAvailableException;
import org.jboss.bqt.exceptions.ExecutionInterruptedException;
import org.jboss.bqt.exceptions.MaxTimeExceededException;
import org.jboss.bqt.exceptions.ServerNotAvailableException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SuiteTest {

    @Test(expected = ServerNotAvailableException.class)
    public void serverNotAvailableTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 1, 2, 1, ServerNotAvailableException.class));
        s.run(-1);
    }

    @Test(expected = DbNotAvailableException.class)
    public void dbNotAvailableTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 1, 2, 1, DbNotAvailableException.class));
        s.run(-1);
    }

    @Test(expected = ExecutionInterruptedException.class)
    public void executionInterruptedTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 1, 2, 1, ExecutionInterruptedException.class));
        s.run(-1);
    }

    @Test(expected = MaxTimeExceededException.class)
    public void maxTimeExceededTest1() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.run(1000);
    }

    @Test(expected = MaxTimeExceededException.class)
    public void maxTimeExceededTest2() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.run(System.currentTimeMillis());
    }

    @Test
    public void getNumOfAllQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assert.assertEquals("All", 6, s.getNumberOfAllQueries());
    }

    @Test
    public void getNumOfExecutedQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assert.assertEquals("Executed", 4, s.getNumberOfExecutedQueries());
    }

    @Test
    public void getNumOfPassedQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assert.assertEquals("Passed", 3, s.getNumberOfPassedQueries());
    }

    @Test
    public void getNumOfFailedQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assert.assertEquals("Failed", 1, s.getNumberOfFailedQueries());
    }

    private QuerySet getQuerySet(int all, int failed, int executed, int passed, Class<? extends Exception> toThrow) throws Exception{
        QuerySet qs = Mockito.mock(QuerySet.class);
        Mockito.doReturn(all).when(qs).getNumberOfAllQueries();
        Mockito.doReturn(executed).when(qs).getNumberOfExecutedQueries();
        Mockito.doReturn(failed).when(qs).getNumberOfFailedQueries();
        Mockito.doReturn(passed).when(qs).getNumberOfPassedQueries();
        if(toThrow != null){
            Mockito.doThrow(toThrow).when(qs).runQueries();
        }
        return qs;
    }

}
