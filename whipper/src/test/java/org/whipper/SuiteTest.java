package org.whipper;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.whipper.Query.QueryResult;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ExecutionInterruptedException;
import org.whipper.exceptions.MaxTimeExceededException;
import org.whipper.exceptions.ServerNotAvailableException;

public class SuiteTest{

    @Test
    public void serverNotAvailableTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 1, 2, 1, ServerNotAvailableException.class));
        Assertions.assertThrows(ServerNotAvailableException.class, () -> s.run(-1));
    }

    @Test
    public void dbNotAvailableTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 1, 2, 1, DbNotAvailableException.class));
        Assertions.assertThrows(DbNotAvailableException.class, () -> s.run(-1));
    }

    @Test
    public void executionInterruptedTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 1, 2, 1, ExecutionInterruptedException.class));
        Assertions.assertThrows(ExecutionInterruptedException.class, () -> s.run(-1));
    }

    @Test
    public void maxTimeExceededTest1() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        Assertions.assertThrows(MaxTimeExceededException.class, () -> s.run(1000));
    }

    @Test
    public void maxTimeExceededTest2() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        Assertions.assertThrows(MaxTimeExceededException.class, () -> s.run(System.currentTimeMillis()));
    }

    @Test
    public void getNumOfAllQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assertions.assertEquals(6, s.getNumberOfAllQueries(), "All");
    }

    @Test
    public void getNumOfExecutedQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assertions.assertEquals(4, s.getNumberOfExecutedQueries(), "Executed");
    }

    @Test
    public void getNumOfPassedQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assertions.assertEquals(3, s.getNumberOfPassedQueries(), "Passed");
    }

    @Test
    public void getNumOfFailedQueriesTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 1, 2, 1, null));
        s.addQuerySet(getQuerySet(1, 0, 0, 0, null));

        Assertions.assertEquals(1, s.getNumberOfFailedQueries(), "Failed");
    }

    @Test
    public void runDefaultMetaTest() throws Exception{
        Suite s = new Suite("");
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(getQuerySet(3, 0, 3, 3, null));
        QuerySet beforeE = getQuerySet(1, 0, 1, 1, null);
        QuerySet afterE = getQuerySet(1, 0, 1, 1, null);
        QuerySet beforeS = getQuerySet(1, 0, 1, 1, null);
        QuerySet afterS = getQuerySet(1, 0, 1, 1, null);
        s.setBeforeEach(beforeE);
        s.setAfterEach(afterE);
        s.setBeforeSuite(beforeS);
        s.setAfterSuite(afterS);
        s.run(-1);
        Mockito.verify(afterE, Mockito.times(2)).runQueries();
        Mockito.verify(beforeE, Mockito.times(2)).runQueries();
        Mockito.verify(afterS).runQueries();
        Mockito.verify(beforeS).runQueries();
    }

    @Test
    public void runDefaultMetaWithMetaInQSTest() throws Exception{
        Suite s = new Suite("");
        QuerySet q1 = getQuerySet(2, 0, 2, 2, null);
        QuerySet q2 = getQuerySet(2, 0, 2, 2, null);
        QuerySet beforeQ1 = getQuerySet(2, 0, 2, 2, null);
        QuerySet afterQ2 = getQuerySet(2, 0, 2, 2, null);
        Mockito.doReturn(beforeQ1).when(q1).getBefore();
        Mockito.doReturn(afterQ2).when(q2).getAfter();
        s.addQuerySet(getQuerySet(2, 0, 2, 2, null));
        s.addQuerySet(q1);
        s.addQuerySet(q2);
        s.addQuerySet(getQuerySet(3, 0, 3, 3, null));
        QuerySet before = getQuerySet(1, 0, 1, 1, null);
        QuerySet after = getQuerySet(1, 0, 1, 1, null);
        QuerySet beforeS = getQuerySet(1, 0, 1, 1, null);
        QuerySet afterS = getQuerySet(1, 0, 1, 1, null);
        s.setBeforeSuite(beforeS);
        s.setAfterSuite(afterS);
        s.setBeforeEach(before);
        s.setAfterEach(after);
        s.run(-1);
        Mockito.verify(after, Mockito.times(3)).runQueries();
        Mockito.verify(before, Mockito.times(3)).runQueries();
        Mockito.verify(afterQ2).runQueries();
        Mockito.verify(beforeQ1).runQueries();
        Mockito.verify(afterS).runQueries();
        Mockito.verify(beforeS).runQueries();
    }

    @Test
    public void exception1InBeforeTest() throws Exception{
        Suite s = new Suite("");
        QuerySet q = getQuerySet(2, 0, 2, 2, null);
        s.addQuerySet(q);
        s.setBeforeEach(getQuerySet(1, 1, 1, 0, ServerNotAvailableException.class));
        try{
            s.run(-1);
            Assertions.fail("No exception thrown.");
        } catch (ServerNotAvailableException ex){
        }
        Mockito.verify(q, Mockito.never()).runQueries();
    }

    @Test
    public void exception2InBeforeTest() throws Exception{
        Suite s = new Suite("");
        QuerySet q = getQuerySet(2, 0, 2, 2, null);
        s.addQuerySet(q);
        s.setBeforeEach(getQuerySet(1, 1, 1, 0, ExecutionInterruptedException.class));
        try{
            s.run(-1);
            Assertions.fail("No exception thrown.");
        } catch (ExecutionInterruptedException ex){
        }
        Mockito.verify(q, Mockito.never()).runQueries();
    }

    @Test
    public void exception3InBeforeTest() throws Exception{
        Suite s = new Suite("");
        QuerySet q = getQuerySet(2, 0, 2, 2, null);
        s.addQuerySet(q);
        s.setBeforeEach(getQuerySet(1, 1, 1, 0, DbNotAvailableException.class));
        try{
            s.run(-1);
            Assertions.fail("No exception thrown.");
        } catch (DbNotAvailableException ex){
        }
        Mockito.verify(q, Mockito.never()).runQueries();
    }

    @Test
    public void exception1InAfterTest() throws Exception{
        Suite s = new Suite("");
        QuerySet q = getQuerySet(2, 0, 2, 2, null);
        s.addQuerySet(q);
        s.setAfterEach(getQuerySet(1, 1, 1, 0, ServerNotAvailableException.class));
        try{
            s.run(-1);
            Assertions.fail("No exception thrown.");
        } catch (ServerNotAvailableException ex){
        }
        Mockito.verify(q).runQueries();
        Mockito.verify(q, Mockito.never()).beforeFailed(Mockito.any(Throwable.class), Mockito.anyString());
    }

    @Test
    public void exception2InAfterTest() throws Exception{
        Suite s = new Suite("");
        QuerySet q = getQuerySet(2, 0, 2, 2, null);
        s.addQuerySet(q);
        s.setAfterEach(getQuerySet(1, 1, 1, 0, ExecutionInterruptedException.class));
        try{
            s.run(-1);
            Assertions.fail("No exception thrown.");
        } catch (ExecutionInterruptedException ex){
        }
        Mockito.verify(q).runQueries();
        Mockito.verify(q, Mockito.never()).beforeFailed(Mockito.any(Throwable.class), Mockito.anyString());
    }

    @Test
    public void exception3InAfterTest() throws Exception{
        Suite s = new Suite("");
        QuerySet q = getQuerySet(2, 0, 2, 2, null);
        s.addQuerySet(q);
        s.setAfterEach(getQuerySet(1, 1, 1, 0, DbNotAvailableException.class));
        try{
            s.run(-1);
            Assertions.fail("No exception thrown.");
        } catch (DbNotAvailableException ex){
        }
        Mockito.verify(q).runQueries();
        Mockito.verify(q, Mockito.never()).beforeFailed(Mockito.any(Throwable.class), Mockito.anyString());
    }

    @Test
    public void notRunOnBeforeExceptionTest() throws Exception{
        Suite s = new Suite("");
        QuerySet qs = getQuerySet(0, 0, 0, 0, null);
        s.addQuerySet(qs);
        Query q = Mockito.mock(Query.class);
        QueryResult qr = Mockito.mock(QueryResult.class);
        Exception ex = new Exception();
        Mockito.doReturn(ex).when(qr).getException();
        Mockito.doReturn(qr).when(q).getResult();
        QuerySet before = getQuerySet(1, 1, 1, 0, null);
        QuerySet after = getQuerySet(1, 0, 1, 1, null);
        Mockito.doReturn(Arrays.asList(q)).when(before).getFailedQueries();
        s.setBeforeEach(before);
        s.setAfterEach(after);
        s.run(-1);
        Mockito.verify(before).runQueries();
        Mockito.verify(qs).beforeFailed(Mockito.same(ex), Mockito.contains("set"));
        Mockito.verify(qs, Mockito.never()).runQueries();
        Mockito.verify(after).runQueries();
    }

    @Test
    public void notRunOnBeforeSkippedTest() throws Exception{
        Suite s = new Suite("");
        QuerySet qs = getQuerySet(0, 0, 0, 0, null);
        s.addQuerySet(qs);
        QuerySet before = getQuerySet(2, 0, 1, 1, null);
        QuerySet after = getQuerySet(1, 0, 1, 1, null);
        s.setBeforeEach(before);
        s.setAfterEach(after);
        s.run(-1);
        Mockito.verify(before).runQueries();
        Mockito.verify(qs).beforeFailed(Mockito.any(Throwable.class), Mockito.contains("set"));
        Mockito.verify(qs, Mockito.never()).runQueries();
        Mockito.verify(after).runQueries();
    }

    @Test
    public void runBeforeMainAfterOnMaxTimeExceededTest() throws Exception{
        Suite s = new Suite("");
        QuerySet qs = getQuerySet(0, 0, 0, 0, null);
        s.addQuerySet(qs);
        QuerySet before = getQuerySet(1, 0, 1, 1, null);
        QuerySet after = getQuerySet(1, 0, 1, 1, null);
        s.setBeforeEach(before);
        s.setAfterEach(after);
        try{
            s.run(1);
            Assertions.fail("No exception thrown.");
        } catch (MaxTimeExceededException ex){
        }
        Mockito.verify(before).runQueries();
        Mockito.verify(qs).runQueries();
        Mockito.verify(after).runQueries();
    }

    @Test
    public void notRunOnBeforeSuiteExceptionTest() throws Exception{
        Suite s = new Suite("");
        QuerySet qs1 = getQuerySet(0, 0, 0, 0, null);
        QuerySet qs2 = getQuerySet(0, 0, 0, 0, null);
        s.addQuerySet(qs1);
        s.addQuerySet(qs2);
        Query q = Mockito.mock(Query.class);
        QueryResult qr = Mockito.mock(QueryResult.class);
        Exception ex = new Exception();
        Mockito.doReturn(ex).when(qr).getException();
        Mockito.doReturn(qr).when(q).getResult();
        QuerySet before = getQuerySet(1, 1, 1, 0, null);
        QuerySet after = getQuerySet(1, 0, 1, 1, null);
        Mockito.doReturn(Arrays.asList(q)).when(before).getFailedQueries();
        s.setBeforeSuite(before);
        s.setAfterSuite(after);
        s.run(-1);
        Mockito.verify(before).runQueries();
        Mockito.verify(qs1).beforeFailed(Mockito.same(ex), Mockito.contains("suite"));
        Mockito.verify(qs1, Mockito.never()).runQueries();
        Mockito.verify(qs2).beforeFailed(Mockito.same(ex), Mockito.contains("suite"));
        Mockito.verify(qs2, Mockito.never()).runQueries();
        Mockito.verify(after).runQueries();
    }

    @Test
    public void notRunOnBeforeSuiteSkippedTest() throws Exception{
        Suite s = new Suite("");
        QuerySet qs1 = getQuerySet(0, 0, 0, 0, null);
        QuerySet qs2 = getQuerySet(0, 0, 0, 0, null);
        s.addQuerySet(qs1);
        s.addQuerySet(qs2);
        QuerySet before = getQuerySet(2, 0, 1, 1, null);
        QuerySet after = getQuerySet(1, 0, 1, 1, null);
        s.setBeforeSuite(before);
        s.setAfterSuite(after);
        s.run(-1);
        Mockito.verify(before).runQueries();
        Mockito.verify(qs1).beforeFailed(Mockito.any(Throwable.class), Mockito.contains("suite"));
        Mockito.verify(qs1, Mockito.never()).runQueries();
        Mockito.verify(qs2).beforeFailed(Mockito.any(Throwable.class), Mockito.contains("suite"));
        Mockito.verify(qs2, Mockito.never()).runQueries();
        Mockito.verify(after).runQueries();
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
