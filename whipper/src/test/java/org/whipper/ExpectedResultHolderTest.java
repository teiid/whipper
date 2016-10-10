package org.whipper;

import java.math.BigDecimal;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class ExpectedResultHolderTest{

    @Test
    public void equalsNoExpResTest(){
        Assert.assertFalse("Expected result is none of update, exception, table.", new ExpectedResultHolder().equals(getMockUpdate(0), false, null));
    }

    @Test
    public void equalsNoActResTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setUpdateCount(0);
        Assert.assertFalse("Expected result is none of update, exception, table.", exp.equals(getMock(), false, null));
    }

    @Test
    public void equalsUpdateTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setUpdateCount(0);
        Assert.assertFalse("Update exp - update act - not eq", exp.equals(getMockUpdate(1), false, null));
        Assert.assertTrue("Update exp - update act - eq", exp.equals(getMockUpdate(0), false, null));
        Assert.assertFalse("Update exp - exception act", exp.equals(getMockException(new SQLException("")), false, null));
        Assert.assertFalse("Update exp - table act", exp.equals(getMockTable(Collections.<String>emptyList(),
                Collections.<String>emptyList(), Collections.<List<Object>>emptyList()), false, BigDecimal.ZERO));
    }

    @Test
    public void equalsExceptionTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setExceptionClass(SQLNonTransientException.class.getName());
        exp.setExceptionMessage("msg");
        Assert.assertFalse("Exception exp - update act", exp.equals(getMockUpdate(1), false, null));
        Assert.assertFalse("Exception exp - table act", exp.equals(getMockTable(Collections.<String>emptyList(),
                Collections.<String>emptyList(), Collections.<List<Object>>emptyList()), false, BigDecimal.ZERO));
        Assert.assertFalse("Exception exp - exception act - wrong class", exp.equals(getMockException(new SQLException("")), false, null));
        Assert.assertFalse("Exception exp - exception act - wrong class", exp.equals(getMockException(new SQLDataException("")), false, null));
        Assert.assertFalse("Exception exp - exception act - wrong message", exp.equals(getMockException(new SQLNonTransientException("")), false, null));
        Assert.assertTrue("Exception exp - exception act - ok message", exp.equals(getMockException(new SQLNonTransientException("msg")), false, null));
        exp.setExceptionMessage(null);
        exp.setExceptionRegex("[^a]*");
        Assert.assertFalse("Exception exp - exception act - wrong message regex", exp.equals(getMockException(new SQLNonTransientException("bab")), false, null));
        Assert.assertTrue("Exception exp - exception act - ok message regex", exp.equals(getMockException(new SQLNonTransientException("bbb")), false, null));
    }

    @Test
    public void equalsTableTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setColumnLabels(Arrays.asList("a"));
        exp.setColumnTypeNames(Arrays.asList("b"));
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();
        Object o4 = new Object();
        exp.setRows(Arrays.asList(Arrays.asList(o1), Arrays.asList(o2), Arrays.asList(o3)));
        Assert.assertFalse("Table exp - update act", exp.equals(getMockUpdate(1), false, null));
        Assert.assertFalse("Table exp - exception act", exp.equals(getMockException(new SQLException("")), false, null));
        Assert.assertFalse("Table exp - table act - wrong labels", exp.equals(getMockTable(
                Arrays.asList("b"), Arrays.asList("b"), Arrays.asList(Arrays.asList(o1), Arrays.asList(o2), Arrays.asList(o3))), false, BigDecimal.ZERO));
        Assert.assertFalse("Table exp - table act - wrong types", exp.equals(getMockTable(
                Arrays.asList("a"), Arrays.asList("a"), Arrays.asList(Arrays.asList(o1), Arrays.asList(o2), Arrays.asList(o3))), false, BigDecimal.ZERO));
        Assert.assertFalse("Table exp - table act - wrong column count", exp.equals(getMockTable(
                Arrays.asList("a", "b"), Arrays.asList("a", "b"), Arrays.asList(Arrays.asList(o1), Arrays.asList(o2), Arrays.asList(o3))), false, BigDecimal.ZERO));
        Assert.assertFalse("Table exp - table act - wrong row count", exp.equals(getMockTable(
                Arrays.asList("b"), Arrays.asList("a"), Arrays.asList(Arrays.asList(o1), Arrays.asList(o2))), false, BigDecimal.ZERO));
        Assert.assertFalse("Table exp - table act - wrong row", exp.equals(getMockTable(
                Arrays.asList("b"), Arrays.asList("a"), Arrays.asList(Arrays.asList(o1), Arrays.asList(o4), Arrays.asList(o3))), false, BigDecimal.ZERO));
        Assert.assertTrue("Table exp - table act - ok", exp.equals(getMockTable(
                Arrays.asList("b"), Arrays.asList("a"), Arrays.asList(Arrays.asList(o1), Arrays.asList(o2), Arrays.asList(o3))), false, BigDecimal.ZERO));
    }

    private ActualResultHolder getMockUpdate(int uc){
        ActualResultHolder arh = getMock();
        Mockito.doReturn(true).when(arh).isUpdate();
        Mockito.doReturn(uc).when(arh).getUpdateCount();
        return arh;
    }

    private ActualResultHolder getMockException(SQLException ex){
        ActualResultHolder arh = getMock();
        Mockito.doReturn(true).when(arh).isException();
        Mockito.doReturn(ex).when(arh).getOriginalException();
        Mockito.doReturn(ex.getClass()).when(arh).getOriginalExceptionClass();
        Mockito.doReturn(ex.getMessage()).when(arh).getRootCauseExceptionMessage();
        return arh;
    }

    private ActualResultHolder getMockTable(List<String> types, List<String> labels, List<List<Object>> rows){
        ActualResultHolder arh = getMock();
        Mockito.doReturn(true).when(arh).isResult();
        Mockito.doReturn(types).when(arh).getColumnTypeNames();
        Mockito.doReturn(labels).when(arh).getColumnLabels();
        Mockito.doReturn(rows).when(arh).getRows();
        return arh;
    }

    private ActualResultHolder getMock(){
        ActualResultHolder arh = Mockito.mock(ActualResultHolder.class);
        Mockito.doReturn(-1).when(arh).getUpdateCount();
        return arh;
    }
}
