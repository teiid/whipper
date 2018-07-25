package org.whipper;

import java.math.BigDecimal;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ExpectedResultHolderTest{

    @Test
    public void equalsNoExpResTest(){
        Assertions.assertFalse(new ExpectedResultHolder().equals(getMockUpdate(0), false, null), "Expected result is none of update, exception, table.");
    }

    @Test
    public void equalsNoActResTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setUpdateCount(0);
        Assertions.assertFalse(exp.equals(getMock(), false, null), "Actual result is none of update, exception, table.");
    }

    @Test
    public void equalsNoResTest(){
        Assertions.assertTrue(new ExpectedResultHolder().equals(getMock(), false, null), "Actual and expected result are no-result.");
    }

    @Test
    public void equalsUpdateTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setUpdateCount(0);
        Assertions.assertAll(() -> Assertions.assertFalse(exp.equals(getMockUpdate(1), false, null), "Update exp - update act - not eq"),
                () -> Assertions.assertTrue(exp.equals(getMockUpdate(0), false, null), "Update exp - update act - eq"),
                () -> Assertions.assertFalse(exp.equals(getMockException(new SQLException("")), false, null), "Update exp - exception act"),
                () -> Assertions.assertFalse(exp.equals(getMockTable(Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList()), false, BigDecimal.ZERO), "Update exp - table act"));
    }

    @Test
    public void equalsExceptionTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setExceptionClass(SQLNonTransientException.class.getName());
        exp.setExceptionMessage("msg");
        Assertions.assertAll(() -> Assertions.assertFalse(exp.equals(getMockUpdate(1), false, null), "Exception exp - update act"),
                () -> Assertions.assertFalse(exp.equals(getMockTable(Collections.emptyList(),
                        Collections.emptyList(), Collections.emptyList()), false, BigDecimal.ZERO), "Exception exp - table act"),
                () -> Assertions.assertFalse(exp.equals(getMockException(new SQLException("")), false, null), "Exception exp - exception act - wrong class"),
                () -> Assertions.assertFalse(exp.equals(getMockException(new SQLDataException("")), false, null), "Exception exp - exception act - wrong class"),
                () -> Assertions.assertFalse(exp.equals(getMockException(new SQLNonTransientException("")), false, null), "Exception exp - exception act - wrong message"),
                () -> Assertions.assertTrue(exp.equals(getMockException(new SQLNonTransientException("msg")), false, null), "Exception exp - exception act - ok message"));
        exp.setExceptionMessage(null);
        exp.setExceptionRegex("[^a]*");
        Assertions.assertAll(() -> Assertions.assertFalse(exp.equals(getMockException(new SQLNonTransientException("bab")), false, null), "Exception exp - exception act - wrong message regex"),
                () -> Assertions.assertTrue(exp.equals(getMockException(new SQLNonTransientException("bbb")), false, null), "Exception exp - exception act - ok message regex"));
    }

    @Test
    public void equalsTableTest(){
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setColumnLabels(Collections.singletonList("a"));
        exp.setColumnTypeNames(Collections.singletonList("b"));
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();
        Object o4 = new Object();
        exp.setRows(Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o2), Collections.singletonList(o3)));
        Assertions.assertAll(() -> Assertions.assertFalse(exp.equals(getMockUpdate(1), false, null), "Table exp - update act"),
                () -> Assertions.assertFalse(exp.equals(getMockException(new SQLException("")), false, null), "Table exp - exception act"),
                () -> Assertions.assertFalse(exp.equals(getMockTable(
                        Collections.singletonList("b"), Collections.singletonList("b"), Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o2), Collections.singletonList(o3))), false, BigDecimal.ZERO), "Table exp - table act - wrong labels"),
                () -> Assertions.assertFalse(exp.equals(getMockTable(
                        Collections.singletonList("a"), Collections.singletonList("a"), Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o2), Collections.singletonList(o3))), false, BigDecimal.ZERO), "Table exp - table act - wrong types"),
                () -> Assertions.assertFalse(exp.equals(getMockTable(
                        Arrays.asList("a", "b"), Arrays.asList("a", "b"), Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o2), Collections.singletonList(o3))), false, BigDecimal.ZERO), "Table exp - table act - wrong column count"),
                () -> Assertions.assertFalse(exp.equals(getMockTable(
                        Collections.singletonList("b"), Collections.singletonList("a"), Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o2))), false, BigDecimal.ZERO), "Table exp - table act - wrong row count"),
                () -> Assertions.assertFalse(exp.equals(getMockTable(
                        Collections.singletonList("b"), Collections.singletonList("a"), Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o4), Collections.singletonList(o3))), false, BigDecimal.ZERO), "Table exp - table act - wrong row"),
                () -> Assertions.assertTrue(exp.equals(getMockTable(
                        Collections.singletonList("b"), Collections.singletonList("a"), Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o2), Collections.singletonList(o3))), false, BigDecimal.ZERO), "Table exp - table act - ok"));
    }
    
    @Test
    public void regexTypeTest() {
        ExpectedResultHolder exp = new ExpectedResultHolder();
        exp.setColumnLabels(Collections.singletonList("regex"));
        exp.setColumnTypeNames(Collections.singletonList("regex"));
        Object o1 = Pattern.compile("24[.]0+");
        Object o2 = Pattern.compile("true", Pattern.CASE_INSENSITIVE);
        exp.setRows(Arrays.asList(Collections.singletonList(o1), Collections.singletonList(o2)));

        Assertions
            .assertAll(
                () -> Assertions.assertTrue(exp.equals(
                    getMockTable(Collections.singletonList("regex"), Collections.singletonList("regex"),
                        Arrays.asList(Collections.singletonList(24.00000000), Collections.singletonList("TRUE"))),
                    false, BigDecimal.ZERO), "Table exp - table act - regex should match"),
                () -> Assertions
                    .assertFalse(
                        exp.equals(
                            getMockTable(Collections.singletonList("regex"), Collections.singletonList("regex"),
                                Arrays.asList(Collections.singletonList(24.00000000),
                                    Collections.singletonList("FALSE"))),
                            false, BigDecimal.ZERO),
                        "Table exp - table act - regex should not match"),
                () -> Assertions.assertFalse(exp.equals(
                    getMockTable(Collections.singletonList("regex"), Collections.singletonList("regex"),
                        Arrays.asList(Collections.singletonList(24.00000001), Collections.singletonList("TRUE"))),
                    false, BigDecimal.ZERO), "Table exp - table act - regex should not match"));
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
        Mockito.doCallRealMethod().when(arh).isNoResult();
        Mockito.doReturn(-1).when(arh).getUpdateCount();
        return arh;
    }
}
