package org.jboss.bqt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.jboss.bqt.Query.QueryResult;
import org.jboss.bqt.exceptions.DbNotAvailableException;
import org.jboss.bqt.exceptions.ServerNotAvailableException;
import org.jboss.bqt.resultmode.ResultHandler;
import org.jboss.bqt.resultmode.ResultMode;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class QueryTest {

    private static int count = 1;

    @Test
    public void runQueryTestTableOK() throws SQLException{
        Query q = getQuery(getEmptyTable(), getResultHandler(false, false, false, null, null), true);
        QueryResult qr = q.run();
        Assert.assertSame("Query from QueryResult.", q, qr.getQuery());

        ActualResultHandler arh = q.getActualResult();
        Assert.assertTrue("Is result.", arh.isResult());
        Assert.assertFalse("Is update.", arh.isUpdate());
        Assert.assertFalse("Is exception.", arh.isException());
        Assert.assertEquals("Types", Arrays.asList("type1"), arh.getColumnTypeNames());
        Assert.assertEquals("Lables", Arrays.asList("label1"), arh.getColumnLabels());
        Assert.assertEquals("Rows", Arrays.asList(), arh.getRows());

        Assert.assertTrue("Query result - is pass", qr.pass());
        Assert.assertFalse("Query result - is error", qr.isError());
        Assert.assertFalse("Query result - is exception", qr.isException());
        Assert.assertNull("Query result - exception", qr.getException());
        Assert.assertNull("Query result - errors", qr.getErrors());
    }

    @Test
    public void runQueryTestTableFail() throws SQLException{
        String err = "compare error";
        Query q = getQuery(getEmptyTable(), getResultHandler(true, true, false, null, err), true);
        QueryResult qr = q.run();
        Assert.assertSame("Query from QueryResult.", q, qr.getQuery());

        ActualResultHandler arh = q.getActualResult();
        Assert.assertTrue("Is result.", arh.isResult());
        Assert.assertFalse("Is update.", arh.isUpdate());
        Assert.assertFalse("Is exception.", arh.isException());
        Assert.assertEquals("Types", Arrays.asList("type1"), arh.getColumnTypeNames());
        Assert.assertEquals("Lables", Arrays.asList("label1"), arh.getColumnLabels());
        Assert.assertEquals("Rows", Arrays.asList(), arh.getRows());

        Assert.assertFalse("Query result - is pass", qr.pass());
        Assert.assertTrue("Query result - is error", qr.isError());
        Assert.assertFalse("Query result - is exception", qr.isException());
        Assert.assertNull("Query result - exception", qr.getException());
        Assert.assertEquals("Query result - errors", Arrays.asList(err), qr.getErrors());
    }

    @Test
    public void runQueryTestTableErrorHandlingResult() throws SQLException{
        IOException ex = new IOException();
        Query q = getQuery(getEmptyTable(), getResultHandler(true, false, true, ex, null), true);
        QueryResult qr = q.run();
        Assert.assertSame("Query from QueryResult.", q, qr.getQuery());

        ActualResultHandler arh = q.getActualResult();
        Assert.assertTrue("Is result.", arh.isResult());
        Assert.assertFalse("Is update.", arh.isUpdate());
        Assert.assertFalse("Is exception.", arh.isException());
        Assert.assertEquals("Types", Arrays.asList("type1"), arh.getColumnTypeNames());
        Assert.assertEquals("Lables", Arrays.asList("label1"), arh.getColumnLabels());
        Assert.assertEquals("Rows", Arrays.asList(), arh.getRows());

        Assert.assertFalse("Query result - is pass", qr.pass());
        Assert.assertFalse("Query result - is error", qr.isError());
        Assert.assertTrue("Query result - is exception", qr.isException());
        Assert.assertSame("Query result - exception", ex, qr.getException());
        Assert.assertNull("Query result - errors", qr.getErrors());
    }

    @Test
    public void runQueryTestExceptionExpected() throws SQLException{
        SQLException ex = new SQLException("ex", "00");
        Query q = getQuery(getException(ex), getResultHandler(false, false, false, null, null), true);
        QueryResult qr = q.run();
        Assert.assertSame("Query from QueryResult.", q, qr.getQuery());

        ActualResultHandler arh = q.getActualResult();
        Assert.assertFalse("Is result.", arh.isResult());
        Assert.assertFalse("Is update.", arh.isUpdate());
        Assert.assertTrue("Is exception.", arh.isException());
        Assert.assertNull("Types", arh.getColumnTypeNames());
        Assert.assertNull("Lables", arh.getColumnLabels());
        Assert.assertNull("Rows", arh.getRows());

        Assert.assertTrue("Query result - is pass", qr.pass());
        Assert.assertFalse("Query result - is error", qr.isError());
        Assert.assertTrue("Query result - is exception", qr.isException());
        Assert.assertSame("Query result - exception", ex, qr.getException());
        Assert.assertNull("Query result - errors", qr.getErrors());
    }

    @Test
    public void runQueryTestExceptionFail() throws SQLException{
        SQLException ex = new SQLException("ex", "00");
        String err = "compare error";
        Query q = getQuery(getException(ex), getResultHandler(true, true, false, null, err), true);
        QueryResult qr = q.run();
        Assert.assertSame("Query from QueryResult.", q, qr.getQuery());

        ActualResultHandler arh = q.getActualResult();
        Assert.assertFalse("Is result.", arh.isResult());
        Assert.assertFalse("Is update.", arh.isUpdate());
        Assert.assertTrue("Is exception.", arh.isException());
        Assert.assertNull("Types", arh.getColumnTypeNames());
        Assert.assertNull("Lables", arh.getColumnLabels());
        Assert.assertNull("Rows", arh.getRows());

        Assert.assertFalse("Query result - is pass", qr.pass());
        Assert.assertTrue("Query result - is error", qr.isError());
        Assert.assertTrue("Query result - is exception", qr.isException());
        Assert.assertSame("Query result - exception", ex, qr.getException());
        Assert.assertEquals("Query result - errors", Arrays.asList(err), qr.getErrors());
    }

    @Test
    public void runQueryTestExceptionDbNotAvailable() throws SQLException{
        SQLException ex = new SQLException("ex", "00");
        DbNotAvailableException exDb = new DbNotAvailableException();
        Query q = getQuery(getException(ex), getResultHandler(true, false, true, exDb, null), false);
        QueryResult qr = q.run();
        Assert.assertSame("Query from QueryResult.", q, qr.getQuery());

        ActualResultHandler arh = q.getActualResult();
        Assert.assertFalse("Is result.", arh.isResult());
        Assert.assertFalse("Is update.", arh.isUpdate());
        Assert.assertTrue("Is exception.", arh.isException());
        Assert.assertNull("Types", arh.getColumnTypeNames());
        Assert.assertNull("Lables", arh.getColumnLabels());
        Assert.assertNull("Rows", arh.getRows());

        Assert.assertFalse("Query result - is pass", qr.pass());
        Assert.assertFalse("Query result - is error", qr.isError());
        Assert.assertTrue("Query result - is exception", qr.isException());
        Assert.assertNotNull("Query result - exception", qr.getException());
        Assert.assertSame("Query result - exception", exDb.getClass(), qr.getException().getClass());
        Assert.assertNull("Query result - errors", qr.getErrors());
    }

    @Test
    public void runQueryTestExceptionServerNotAvailable() throws SQLException{
        SQLException ex = new SQLException("ex", "08");
        ServerNotAvailableException exSrv = new ServerNotAvailableException();
        Query q = getQuery(getException(ex), getResultHandler(true, false, true, exSrv, null), false);
        QueryResult qr = q.run();
        Assert.assertSame("Query from QueryResult.", q, qr.getQuery());

        ActualResultHandler arh = q.getActualResult();
        Assert.assertFalse("Is result.", arh.isResult());
        Assert.assertFalse("Is update.", arh.isUpdate());
        Assert.assertTrue("Is exception.", arh.isException());
        Assert.assertNull("Types", arh.getColumnTypeNames());
        Assert.assertNull("Lables", arh.getColumnLabels());
        Assert.assertNull("Rows", arh.getRows());

        Assert.assertFalse("Query result - is pass", qr.pass());
        Assert.assertFalse("Query result - is error", qr.isError());
        Assert.assertTrue("Query result - is exception", qr.isException());
        Assert.assertNotNull("Query result - exception", qr.getException());
        Assert.assertSame("Query result - exception", exSrv.getClass(), qr.getException().getClass());
        Assert.assertNull("Query result - errors", qr.getErrors());
    }

    private Query getQuery(Connection con, ResultHandler rh, boolean valid){
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

    private ResultHandler getResultHandler(boolean fail, boolean error, boolean exception, Throwable ex, String err){
        ResultHandler rh = Mockito.mock(ResultHandler.class);
        Mockito.doReturn(fail).when(rh).isFail();
        Mockito.doReturn(error).when(rh).isError();
        Mockito.doReturn(exception).when(rh).isException();
        Mockito.doReturn(ex).when(rh).getException();
        if(err == null){
            Mockito.doReturn(Arrays.asList()).when(rh).getErrors();
        } else {
            Mockito.doReturn(Arrays.asList(err)).when(rh).getErrors();
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
