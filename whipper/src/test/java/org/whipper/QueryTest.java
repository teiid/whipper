package org.whipper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.whipper.Query.QueryResult;
import org.whipper.exceptions.DbNotAvailableException;
import org.whipper.exceptions.ServerNotAvailableException;
import org.whipper.resultmode.ResultHolder;
import org.whipper.resultmode.ResultMode;

public class QueryTest{

    private static int count = 1;

    @Test
    public void runQueryTestTableOK() throws SQLException{
        Query q = getQuery(getEmptyTable(), getResultHandler(false, false, false, null, null), true);
        Assertions.assertFalse(q.isExecuted(), "Query has not been executed yet.");
        q.run();
        Assertions.assertTrue(q.isExecuted(), "Query has been already executed.");
        QueryResult qr = q.getResult();
        Assertions.assertSame(q, qr.getQuery(), "Query from QueryResult.");

        ActualResultHolder arh = q.getActualResult();
        Assertions.assertAll(() -> Assertions.assertTrue(arh.isResult(), "Is result."),
                () -> Assertions.assertFalse(arh.isUpdate(), "Is update."),
                () -> Assertions.assertFalse(arh.isException(), "Is exception."),
                () -> Assertions.assertEquals(Collections.singletonList("type1"), arh.getColumnTypeNames(), "Types"),
                () -> Assertions.assertEquals(Collections.singletonList("label1"), arh.getColumnLabels(), "Labels"),
                () -> Assertions.assertEquals(Collections.emptyList(), arh.getRows(), "Rows"),
                () -> Assertions.assertTrue(qr.pass(), "Query result - is pass"),
                () -> Assertions.assertFalse(qr.isError(), "Query result - is error"),
                () -> Assertions.assertFalse(qr.isException(), "Query result - is exception"),
                () -> Assertions.assertNull(qr.getException(), "Query result - exception"),
                () -> Assertions.assertNull(qr.getErrors(), "Query result - errors"));
    }

    @Test
    public void runQueryTestTableFail() throws SQLException{
        String err = "compare error";
        Query q = getQuery(getEmptyTable(), getResultHandler(true, true, false, null, err), true);
        Assertions.assertFalse(q.isExecuted(), "Query has not been executed yet.");
        q.run();
        Assertions.assertTrue(q.isExecuted(), "Query has been already executed.");
        QueryResult qr = q.getResult();
        Assertions.assertSame(q, qr.getQuery(), "Query from QueryResult.");

        ActualResultHolder arh = q.getActualResult();
        Assertions.assertAll(() -> Assertions.assertTrue(arh.isResult(), "Is result."),
                () -> Assertions.assertFalse(arh.isUpdate(), "Is update."),
                () -> Assertions.assertFalse(arh.isException(), "Is exception."),
                () -> Assertions.assertEquals(Collections.singletonList("type1"), arh.getColumnTypeNames(), "Types"),
                () -> Assertions.assertEquals(Collections.singletonList("label1"), arh.getColumnLabels(), "Labels"),
                () -> Assertions.assertEquals(Collections.emptyList(), arh.getRows(), "Rows"),
                () -> Assertions.assertFalse(qr.pass(), "Query result - is pass"),
                () -> Assertions.assertTrue(qr.isError(), "Query result - is error"),
                () -> Assertions.assertFalse(qr.isException(), "Query result - is exception"),
                () -> Assertions.assertNull(qr.getException(), "Query result - exception"),
                () -> Assertions.assertEquals(Collections.singletonList(err), qr.getErrors(), "Query result - errors"));
    }

    @Test
    public void runQueryTestTableErrorHandlingResult() throws SQLException{
        IOException ex = new IOException();
        Query q = getQuery(getEmptyTable(), getResultHandler(true, false, true, ex, null), true);
        Assertions.assertFalse(q.isExecuted(), "Query has not been executed yet.");
        q.run();
        Assertions.assertTrue(q.isExecuted(), "Query has been already executed.");
        QueryResult qr = q.getResult();
        Assertions.assertSame(q, qr.getQuery(), "Query from QueryResult.");

        ActualResultHolder arh = q.getActualResult();
        Assertions.assertAll(() -> Assertions.assertTrue(arh.isResult(), "Is result."),
                () -> Assertions.assertFalse(arh.isUpdate(), "Is update."),
                () -> Assertions.assertFalse(arh.isException(), "Is exception."),
                () -> Assertions.assertEquals(Collections.singletonList("type1"), arh.getColumnTypeNames(), "Types"),
                () -> Assertions.assertEquals(Collections.singletonList("label1"), arh.getColumnLabels(), "Labels"),
                () -> Assertions.assertEquals(Collections.emptyList(), arh.getRows(), "Rows"),
                () -> Assertions.assertFalse(qr.pass(), "Query result - is pass"),
                () -> Assertions.assertFalse(qr.isError(), "Query result - is error"),
                () -> Assertions.assertTrue(qr.isException(), "Query result - is exception"),
                () -> Assertions.assertSame(ex, qr.getException(), "Query result - exception"),
                () -> Assertions.assertNull(qr.getErrors(), "Query result - errors"));
    }

    @Test
    public void runQueryTestExceptionExpected() throws SQLException{
        SQLException ex = new SQLException("ex", "00");
        Query q = getQuery(getException(ex), getResultHandler(false, false, false, null, null), true);
        Assertions.assertFalse(q.isExecuted(), "Query has not been executed yet.");
        q.run();
        Assertions.assertTrue(q.isExecuted(), "Query has been already executed.");
        QueryResult qr = q.getResult();
        Assertions.assertSame(q, qr.getQuery(), "Query from QueryResult.");

        ActualResultHolder arh = q.getActualResult();
        Assertions.assertAll(() -> Assertions.assertFalse(arh.isResult(), "Is result."),
                () -> Assertions.assertFalse(arh.isUpdate(), "Is update."),
                () -> Assertions.assertTrue(arh.isException(), "Is exception."),
                () -> Assertions.assertNull(arh.getColumnTypeNames(), "Types"),
                () -> Assertions.assertNull(arh.getColumnLabels(), "Labels"),
                () -> Assertions.assertNull(arh.getRows(), "Rows"),
                () -> Assertions.assertTrue(qr.pass(), "Query result - is pass"),
                () -> Assertions.assertFalse(qr.isError(), "Query result - is error"),
                () -> Assertions.assertTrue(qr.isException(), "Query result - is exception"),
                () -> Assertions.assertSame(ex, qr.getException(), "Query result - exception"),
                () -> Assertions.assertNull(qr.getErrors(), "Query result - errors"));
    }

    @Test
    public void runQueryTestExceptionFail() throws SQLException{
        SQLException ex = new SQLException("ex", "00");
        String err = "compare error";
        Query q = getQuery(getException(ex), getResultHandler(true, true, false, null, err), true);
        Assertions.assertFalse(q.isExecuted(), "Query has not been executed yet.");
        q.run();
        Assertions.assertTrue(q.isExecuted(), "Query has been already executed.");
        QueryResult qr = q.getResult();
        Assertions.assertSame(q, qr.getQuery(), "Query from QueryResult.");

        ActualResultHolder arh = q.getActualResult();
        Assertions.assertAll(() -> Assertions.assertFalse(arh.isResult(), "Is result."),
                () -> Assertions.assertFalse(arh.isUpdate(), "Is update."),
                () -> Assertions.assertTrue(arh.isException(), "Is exception."),
                () -> Assertions.assertNull(arh.getColumnTypeNames(), "Types"),
                () -> Assertions.assertNull(arh.getColumnLabels(), "Labels"),
                () -> Assertions.assertNull(arh.getRows(), "Rows"),
                () -> Assertions.assertFalse(qr.pass(), "Query result - is pass"),
                () -> Assertions.assertTrue(qr.isError(), "Query result - is error"),
                () -> Assertions.assertTrue(qr.isException(), "Query result - is exception"),
                () -> Assertions.assertSame(ex, qr.getException(), "Query result - exception"),
                () -> Assertions.assertEquals(Collections.singletonList(err), qr.getErrors(), "Query result - errors"));
    }

    @Test
    public void runQueryTestExceptionDbNotAvailable() throws SQLException{
        SQLException ex = new SQLException("ex", "00");
        DbNotAvailableException exDb = new DbNotAvailableException("Thrown in test.");
        Query q = getQuery(getException(ex), getResultHandler(true, false, true, exDb, null), false);
        Assertions.assertFalse(q.isExecuted(), "Query has not been executed yet.");
        q.run();
        Assertions.assertTrue(q.isExecuted(), "Query has been already executed.");
        QueryResult qr = q.getResult();
        Assertions.assertSame(q, qr.getQuery(), "Query from QueryResult.");

        ActualResultHolder arh = q.getActualResult();
        Assertions.assertAll(() -> Assertions.assertFalse(arh.isResult(), "Is result."),
                () -> Assertions.assertFalse(arh.isUpdate(), "Is update."),
                () -> Assertions.assertTrue(arh.isException(), "Is exception."),
                () -> Assertions.assertNull(arh.getColumnTypeNames(), "Types"),
                () -> Assertions.assertNull(arh.getColumnLabels(), "Labels"),
                () -> Assertions.assertNull(arh.getRows(), "Rows"),
                () -> Assertions.assertFalse(qr.pass(), "Query result - is pass"),
                () -> Assertions.assertFalse(qr.isError(), "Query result - is error"),
                () -> Assertions.assertTrue(qr.isException(), "Query result - is exception"),
                () -> Assertions.assertNotNull(qr.getException(), "Query result - exception"),
                () -> Assertions.assertSame(exDb.getClass(), qr.getException().getClass(), "Query result - exception"),
                () -> Assertions.assertNull(qr.getErrors(), "Query result - errors"));
    }

    @Test
    public void runQueryTestExceptionServerNotAvailable() throws SQLException{
        SQLException ex = new SQLException("ex", "08");
        ServerNotAvailableException exSrv = new ServerNotAvailableException("Thrown in test.");
        Query q = getQuery(getException(ex), getResultHandler(true, false, true, exSrv, null), false);
        Assertions.assertFalse(q.isExecuted(), "Query has not been executed yet.");
        q.run();
        Assertions.assertTrue(q.isExecuted(), "Query has been already executed.");
        QueryResult qr = q.getResult();
        Assertions.assertSame(q, qr.getQuery(), "Query from QueryResult.");

        ActualResultHolder arh = q.getActualResult();
        Assertions.assertAll(() -> Assertions.assertFalse(arh.isResult(), "Is result."),
                () -> Assertions.assertFalse(arh.isUpdate(), "Is update."),
                () -> Assertions.assertTrue(arh.isException(), "Is exception."),
                () -> Assertions.assertNull(arh.getColumnTypeNames(), "Types"),
                () -> Assertions.assertNull(arh.getColumnLabels(), "Labels"),
                () -> Assertions.assertNull(arh.getRows(), "Rows"),
                () -> Assertions.assertFalse(qr.pass(), "Query result - is pass"),
                () -> Assertions.assertFalse(qr.isError(), "Query result - is error"),
                () -> Assertions.assertTrue(qr.isException(), "Query result - is exception"),
                () -> Assertions.assertNotNull(qr.getException(), "Query result - exception"),
                () -> Assertions.assertSame(exSrv.getClass(), qr.getException().getClass(), "Query result - exception"),
                () -> Assertions.assertNull(qr.getErrors(), "Query result - errors"));
    }

    private Query getQuery(Connection con, ResultHolder rh, boolean valid){
        Scenario sc = Mockito.mock(Scenario.class);
        Mockito.doReturn(con).when(sc).getConnection();
        Mockito.doReturn(valid).when(sc).isConnectionValid();
        Suite su = Mockito.mock(Suite.class);
        Mockito.doReturn("suite" + count++).when(su).getId();
        QuerySet qs = Mockito.mock(QuerySet.class);
        Mockito.doReturn("queryset" + count++).when(qs).getId();
        ResultMode rm = Mockito.mock(ResultMode.class);
        Mockito.doReturn(rh).when(rm).handleResult(Mockito.any(Query.class));
        return new Query(sc, su, qs, "query" + count++, "SELECT 1", rm);
    }

    private ResultHolder getResultHandler(boolean fail, boolean error, boolean exception, Throwable ex, String err){
        ResultHolder rh = Mockito.mock(ResultHolder.class);
        Mockito.doReturn(fail).when(rh).isFail();
        Mockito.doReturn(error).when(rh).isError();
        Mockito.doReturn(exception).when(rh).isException();
        Mockito.doReturn(ex).when(rh).getException();
        if(err == null){
            Mockito.doReturn(Collections.emptyList()).when(rh).getErrors();
        } else {
            Mockito.doReturn(Collections.singletonList(err)).when(rh).getErrors();
        }
        return rh;
    }

    private Connection getEmptyTable() throws SQLException{
        ResultSetMetaData md = Mockito.mock(ResultSetMetaData.class);
        Mockito.doReturn(1).when(md).getColumnCount();
        Mockito.doThrow(SQLException.class).when(md).getColumnTypeName(Mockito.anyInt());
        Mockito.doReturn("type1").when(md).getColumnTypeName(Mockito.eq(1));

        Mockito.doThrow(SQLException.class).when(md).getColumnLabel(Mockito.anyInt());
        Mockito.doReturn("label1").when(md).getColumnLabel(Mockito.eq(1));

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.doReturn(md).when(rs).getMetaData();
        Mockito.when(rs.next()).thenReturn(false);

        Mockito.doThrow(SQLException.class).when(rs).getObject(Mockito.anyInt());

        Statement s = Mockito.mock(Statement.class);
        Mockito.doReturn(Boolean.TRUE).when(s).execute(Mockito.anyString());
        Mockito.doReturn(Boolean.FALSE).when(s).isClosed();
        Mockito.doReturn(rs).when(s).getResultSet();

        Connection c = Mockito.mock(Connection.class);
        Mockito.doReturn(s).when(c).createStatement();
        return c;
    }

    private Connection getException(SQLException ex) throws SQLException{
        Statement s = Mockito.mock(Statement.class);
        Mockito.doThrow(ex).when(s).execute(Mockito.anyString());
        Connection c = Mockito.mock(Connection.class);
        Mockito.doReturn(s).when(c).createStatement();
        return c;
    }
}
