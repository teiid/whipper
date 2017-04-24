package org.whipper;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.codec.binary.Base64;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ActualResultHolderTest{

    private ActualResultHolder holder;

    @BeforeEach
    public void newInstance(){
        holder = new ActualResultHolder();
    }

    @Test
    public void emptyHandlerTest(){
        Assertions.assertAll(() -> Assertions.assertFalse(holder.isException(), "Is exception."),
                () -> Assertions.assertFalse(holder.isResult(), "Is result."),
                () -> Assertions.assertFalse(holder.isUpdate(), "Is update."),
                () -> Assertions.assertNull(holder.getColumnLabels(), "Handler is an exception [labels]."),
                () -> Assertions.assertNull(holder.getColumnTypeNames(), "Handler is an exception [types]."),
                () -> Assertions.assertNull(holder.getRows(), "Handler is an exception [rows]."),
                () -> Assertions.assertEquals(-1, holder.getUpdateCount(), "Update count."),
                () -> Assertions.assertNull(holder.getOriginalException(), "Original exception."),
                () -> Assertions.assertNull(holder.getOriginalExceptionClass(), "Original exception class."),
                () -> Assertions.assertNull(holder.getOriginalExceptionMessage(), "Original exception message."),
                () -> Assertions.assertNull(holder.getRootCauseException(), "Root cause exception."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionClass(), "Root cause exception class."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionMessage(), "Root cause exception message."));
    }

    @Test
    public void buildExceptionTestNull(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> holder.buildResult((SQLException)null));
    }

    @Test
    public void buildExceptionTest(){
        Exception root = new Exception("Root message.");
        SQLException ex1 = new SQLException("Message 1", root);
        SQLException ex2 = new SQLException("Message 2", ex1);
        SQLException ex3 = new SQLException("Message 3", ex2);
        SQLException ex4 = new SQLException("Message 4", ex3);
        SQLWarning ex5 = new SQLWarning("Message 5", ex4);
        holder.buildResult(ex5);

        Assertions.assertAll(() -> Assertions.assertTrue(holder.isException(), "Is exception."),
                () -> Assertions.assertFalse(holder.isResult(), "Is result."),
                () -> Assertions.assertFalse(holder.isUpdate(), "Is update."),
                () -> Assertions.assertNull(holder.getColumnLabels(), "Labels."),
                () -> Assertions.assertNull(holder.getColumnTypeNames(), "Types."),
                () -> Assertions.assertNull(holder.getRows(), "Rows."),
                () -> Assertions.assertEquals(-1, holder.getUpdateCount(), "Update count."),
                () -> Assertions.assertSame(ex5, holder.getOriginalException(), "Original exception."),
                () -> Assertions.assertEquals(SQLWarning.class, holder.getOriginalExceptionClass(), "Original exception class."),
                () -> Assertions.assertEquals(ex5.getMessage(), holder.getOriginalExceptionMessage(), "Original exception message."),
                () -> Assertions.assertSame(root, holder.getRootCauseException(), "Root cause exception."),
                () -> Assertions.assertEquals(Exception.class, holder.getRootCauseExceptionClass(), "Root cause exception class."),
                () -> Assertions.assertEquals(root.getMessage(), holder.getRootCauseExceptionMessage(), "Root cause exception message."))
        ;
    }

    @Test
    public void buildStatementNullTest() throws SQLException{
        Assertions.assertThrows(IllegalArgumentException.class, () -> holder.buildResult((Statement)null));
    }

    @Test
    public void buildStatementClosedTest() throws SQLException{
        Statement s = Mockito.mock(Statement.class);
        Mockito.doReturn(Boolean.TRUE).when(s).isClosed();
        Assertions.assertThrows(IllegalArgumentException.class, () -> holder.buildResult(s));
    }

    @Test
    public void buildUpdateCountTest() throws SQLException{
        int updCount = 5;
        Statement s = Mockito.mock(Statement.class);
        Mockito.doReturn(Boolean.FALSE).when(s).isClosed();
        Mockito.doReturn(null).when(s).getResultSet();
        Mockito.doReturn(updCount).when(s).getUpdateCount();

        holder.buildResult(s);

        Assertions.assertAll(() -> Assertions.assertFalse(holder.isException(), "Is exception."),
                () -> Assertions.assertFalse(holder.isResult(), "Is result."),
                () -> Assertions.assertTrue(holder.isUpdate(), "Is update."),
                () -> Assertions.assertNull(holder.getColumnLabels(), "Labels."),
                () -> Assertions.assertNull(holder.getColumnTypeNames(), "Types."),
                () -> Assertions.assertNull(holder.getRows(), "Rows."),
                () -> Assertions.assertNull(holder.getOriginalException(), "Original exception."),
                () -> Assertions.assertNull(holder.getOriginalExceptionClass(), "Original exception class."),
                () -> Assertions.assertNull(holder.getOriginalExceptionMessage(), "Original exception message."),
                () -> Assertions.assertNull(holder.getRootCauseException(), "Root cause exception."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionClass(), "Root cause exception class."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionMessage(), "Root cause exception message."),
                () -> Assertions.assertEquals(updCount, holder.getUpdateCount(), "Update count."));
    }

    @Test
    public void buildEmptyTableTest() throws Exception{
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
        Mockito.doReturn(Boolean.FALSE).when(s).isClosed();
        Mockito.doReturn(rs).when(s).getResultSet();

        holder.buildResult(s);

        Assertions.assertAll(() -> Assertions.assertFalse(holder.isException(), "Is exception."),
                () -> Assertions.assertTrue(holder.isResult(), "Is result."),
                () -> Assertions.assertFalse(holder.isUpdate(), "Is update."),
                () -> Assertions.assertEquals(Collections.singletonList("label1"), holder.getColumnLabels(), "Labels."),
                () -> Assertions.assertEquals(Collections.singletonList("type1"), holder.getColumnTypeNames(), "Types."),
                () -> Assertions.assertEquals(Collections.emptyList(), holder.getRows(), "Rows."),
                () -> Assertions.assertEquals(-1, holder.getUpdateCount(), "Update count."),
                () -> Assertions.assertNull(holder.getOriginalException(), "Original exception."),
                () -> Assertions.assertNull(holder.getOriginalExceptionClass(), "Original exception class."),
                () -> Assertions.assertNull(holder.getOriginalExceptionMessage(), "Original exception message."),
                () -> Assertions.assertNull(holder.getRootCauseException(), "Root cause exception."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionClass(), "Root cause exception class."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionMessage(), "Root cause exception message."));
    }

    @Test
    public void buildTableTest() throws SQLException{
        ResultSetMetaData md = Mockito.mock(ResultSetMetaData.class);
        Mockito.doReturn(7).when(md).getColumnCount();
        Mockito.doThrow(SQLException.class).when(md).getColumnTypeName(Mockito.anyInt());
        Mockito.doReturn("type1").when(md).getColumnTypeName(Mockito.eq(1));
        Mockito.doReturn("type2").when(md).getColumnTypeName(Mockito.eq(2));
        Mockito.doReturn("type3").when(md).getColumnTypeName(Mockito.eq(3));
        Mockito.doReturn("type4").when(md).getColumnTypeName(Mockito.eq(4));
        Mockito.doReturn("type5").when(md).getColumnTypeName(Mockito.eq(5));
        Mockito.doReturn("type6").when(md).getColumnTypeName(Mockito.eq(6));
        Mockito.doReturn("type7").when(md).getColumnTypeName(Mockito.eq(7));

        Mockito.doThrow(SQLException.class).when(md).getColumnLabel(Mockito.anyInt());
        Mockito.doReturn("label1").when(md).getColumnLabel(Mockito.eq(1));
        Mockito.doReturn("label2").when(md).getColumnLabel(Mockito.eq(2));
        Mockito.doReturn("label3").when(md).getColumnLabel(Mockito.eq(3));
        Mockito.doReturn("label4").when(md).getColumnLabel(Mockito.eq(4));
        Mockito.doReturn("label5").when(md).getColumnLabel(Mockito.eq(5));
        Mockito.doReturn("label6").when(md).getColumnLabel(Mockito.eq(6));
        Mockito.doReturn("label7").when(md).getColumnLabel(Mockito.eq(7));

        byte[] arBlob = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        byte[] arB = new byte[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        Byte[] arByte = new Byte[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101};
        byte[] arBytePrim = new byte[]{11, 21, 31, 41, 51, 61, 71, 81, 91, 101};

        Blob blob = Mockito.mock(Blob.class);
        Mockito.doReturn(10L).when(blob).length();
        Mockito.doReturn(arBlob).when(blob).getBytes(Mockito.eq(1L), Mockito.eq(10));
        Clob clob = Mockito.mock(Clob.class);
        Mockito.doReturn(10L).when(clob).length();
        String clobValue = "0123456789";
        Mockito.doReturn(clobValue).when(clob).getSubString(Mockito.eq(1L), Mockito.eq(10));
        SQLXML xml = Mockito.mock(SQLXML.class);
        String xmlValue = "<a>b</a>";
        Mockito.doReturn(xmlValue).when(xml).getString();

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.doReturn(md).when(rs).getMetaData();
        Mockito.when(rs.next()).thenReturn(true, false);

        Mockito.when(rs.getObject(Mockito.eq(1))).thenReturn(blob).thenThrow(new SQLException());
        Mockito.when(rs.getObject(Mockito.eq(2))).thenReturn(clob).thenThrow(new SQLException());
        Mockito.when(rs.getObject(Mockito.eq(3))).thenReturn(xml).thenThrow(new SQLException());
        Mockito.when(rs.getObject(Mockito.eq(4))).thenReturn(arB).thenThrow(new SQLException());
        Mockito.when(rs.getObject(Mockito.eq(5))).thenReturn(arByte).thenThrow(new SQLException());
        Mockito.when(rs.getObject(Mockito.eq(6))).thenReturn(50).thenThrow(new SQLException());
        Mockito.when(rs.getObject(Mockito.eq(7))).thenReturn(null).thenThrow(new SQLException());
        Mockito.when(rs.getObject(Mockito.intThat(new IntGtMaxLe0(7)))).thenThrow(new SQLException());

        Statement s = Mockito.mock(Statement.class);
        Mockito.doReturn(Boolean.FALSE).when(s).isClosed();
        Mockito.doReturn(rs).when(s).getResultSet();

        holder.buildResult(s);

        Assertions.assertAll(() -> Assertions.assertFalse(holder.isException(), "Is exception."),
                () -> Assertions.assertTrue(holder.isResult(), "Is result."),
                () -> Assertions.assertFalse(holder.isUpdate(), "Is update."),
                () -> Assertions.assertEquals(Arrays.asList("label1", "label2", "label3", "label4", "label5", "label6", "label7"), holder.getColumnLabels(), "Labels."),
                () -> Assertions.assertEquals(Arrays.asList("type1", "type2", "type3", "type4", "type5", "type6", "type7"), holder.getColumnTypeNames(), "Types."),
                () -> Assertions.assertEquals(Collections.singletonList(Arrays.asList(
                        Base64.encodeBase64String(arBlob), clobValue, xmlValue,
                        Base64.encodeBase64String(arB), Base64.encodeBase64String(arBytePrim),
                        50, null)), holder.getRows(), "Rows."),
                () -> Assertions.assertEquals(-1, holder.getUpdateCount(), "Update count."),
                () -> Assertions.assertNull(holder.getOriginalException(), "Original exception."),
                () -> Assertions.assertNull(holder.getOriginalExceptionClass(), "Original exception class."),
                () -> Assertions.assertNull(holder.getOriginalExceptionMessage(), "Original exception message."),
                () -> Assertions.assertNull(holder.getRootCauseException(), "Root cause exception."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionClass(), "Root cause exception class."),
                () -> Assertions.assertNull(holder.getRootCauseExceptionMessage(), "Root cause exception message."));
    }

    @Test
    public void buildNoResult() throws SQLException{
        int updCount = -1;
        Statement s = Mockito.mock(Statement.class);
        Mockito.doReturn(Boolean.FALSE).when(s).isClosed();
        Mockito.doReturn(null).when(s).getResultSet();
        Mockito.doReturn(updCount).when(s).getUpdateCount();

        holder.buildResult(s);
        Assertions.assertAll(() -> Assertions.assertFalse(holder.isException(), "Is exception."),
                () -> Assertions.assertFalse(holder.isResult(), "Is result."),
                () -> Assertions.assertFalse(holder.isUpdate(), "Is update."),
                () -> Assertions.assertTrue(holder.isNoResult(), "Is no-result."));
    }

    private class IntGtMaxLe0 extends BaseMatcher<Integer>{
        private int max;

        private IntGtMaxLe0(int max){
            this.max = max;
        }

        @Override
        public boolean matches(Object item){
            int i = (Integer)item;
            return i > max || i <= 0;
        }

        @Override
        public void describeTo(Description description){
            description.appendText("gt " + max + " or le 0");
        }
    }
}
